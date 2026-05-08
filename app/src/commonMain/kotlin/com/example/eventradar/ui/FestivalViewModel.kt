package com.example.eventradar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventradar.data.FestivalRepository
import com.example.eventradar.data.UserRepository
import com.example.eventradar.data.database.EventRadarDatabase
import com.example.eventradar.data.database.UserEntity
import com.example.eventradar.model.Artist
import com.example.eventradar.model.Festival
import com.example.eventradar.model.LatLng
import com.example.eventradar.notifications.NotificationManager
import kotlin.math.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import kotlinx.datetime.*

class FestivalViewModel(
    database: EventRadarDatabase,
    private val notificationManager: NotificationManager? = null
) : ViewModel() {
    private val GUEST_USER_ID = "guest"
    private val userRepository = UserRepository(
        database.userDao(),
        database.favoriteDao(),
        database.attendanceDao(),
        database.followedArtistDao()
    )

    private val _locationRequestSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val locationRequestSignal = _locationRequestSignal.asSharedFlow()

    private var favoritesJob: kotlinx.coroutines.Job? = null
    private var attendanceJob: kotlinx.coroutines.Job? = null
    private var followedArtistsJob: kotlinx.coroutines.Job? = null
    private var selectedSetsJob: kotlinx.coroutines.Job? = null

    var uiState by mutableStateOf(FestivalUiState())
        private set

    init {
        // Initialize with all festivals
        uiState = uiState.copy(allFestivals = FestivalRepository.harderStylesRadar)
        updateFilteredFestivals()
        updateAllEventCounts()
        
        // Restore session from Supabase
        viewModelScope.launch {
            try {
                userRepository.loadSession()
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    uiState = uiState.copy(currentUser = user)
                    observeFavorites(user.id)
                    observeAttendance(user.id)
                    observeFollowedArtists(user.id)
                    observeSelectedSets(user.id)
                } else {
                    observeFavorites(GUEST_USER_ID)
                    observeAttendance(GUEST_USER_ID)
                    observeFollowedArtists(GUEST_USER_ID)
                    observeSelectedSets(GUEST_USER_ID)
                }
            } catch (e: Exception) {
                observeFavorites(GUEST_USER_ID)
                observeAttendance(GUEST_USER_ID)
                observeFollowedArtists(GUEST_USER_ID)
                observeSelectedSets(GUEST_USER_ID)
            }
        }

        // Seed some mock data for HOT feature demonstration
        seedMockData()
    }

    private fun seedMockData() {
        // Mock data seeding for Supabase would be different, skipping for now
    }

    private fun updateAllEventCounts() {
        uiState.allFestivals.forEach { festival ->
            viewModelScope.launch {
                try {
                    userRepository.getAttendanceCount(festival.id).collectLatest { count ->
                        uiState = uiState.copy(
                            festivalAttendanceCounts = uiState.festivalAttendanceCounts + (festival.id to count)
                        )
                        updateHotCount(festival.id)
                    }
                } catch (e: Exception) {
                    // Ignoriere Fehler beim Zählen
                }
            }
            viewModelScope.launch {
                try {
                    userRepository.getFavoriteCount(festival.id).collectLatest { count ->
                        uiState = uiState.copy(
                            festivalFavoriteCounts = uiState.festivalFavoriteCounts + (festival.id to count)
                        )
                        updateHotCount(festival.id)
                    }
                } catch (e: Exception) {
                    // Ignoriere Fehler beim Zählen
                }
            }
        }
    }

    private fun updateHotCount(festivalId: String) {
        val attendanceCount = uiState.festivalAttendanceCounts[festivalId] ?: 0
        val favoriteCount = uiState.festivalFavoriteCounts[festivalId] ?: 0
        uiState = uiState.copy(
            festivalHotCounts = uiState.festivalHotCounts + (festivalId to (attendanceCount + favoriteCount))
        )
    }

    fun onFestivalSelected(festival: Festival?) {
        uiState = uiState.copy(selectedFestival = festival, selectedSetEntries = emptyList(), communityAttendees = emptyList())
        if (festival != null) {
            val userId = uiState.currentUser?.id ?: GUEST_USER_ID
            viewModelScope.launch {
                userRepository.getSelectedSets(userId, festival.id).collectLatest { sets ->
                    uiState = uiState.copy(selectedSetEntries = sets)
                }
            }
            // Lade Community-Teilnehmer
            viewModelScope.launch {
                val attendees = userRepository.getAttendeesForFestival(festival.id)
                uiState = uiState.copy(communityAttendees = attendees)
            }
        }
    }

    fun onOrganizerSelected(organizerName: String) {
        val organizerFestivals = uiState.allFestivals.filter { it.organizer == organizerName }
        uiState = uiState.copy(selectedOrganizer = organizerName, selectedOrganizerFestivals = organizerFestivals)
    }

    fun onArtistSelected(artistName: String) {
        val artist = FestivalRepository.artists.find { it.name == artistName } 
            ?: Artist(artistName, "https://via.placeholder.com/300?text=${artistName.replace(" ", "+")}")
        val artistFestivals = uiState.allFestivals.filter { festival ->
            festival.lineUp.any { it.contains(artistName, ignoreCase = true) } ||
            festival.lineUpByDay.values.any { stageMap ->
                stageMap.values.any { lineupList ->
                    lineupList.any { it.contains(artistName, ignoreCase = true) }
                }
            }
        }
        uiState = uiState.copy(selectedArtist = artist, selectedArtistFestivals = artistFestivals)
    }

    fun onTypeFilterChanged(isWeekend: Boolean?) {
        uiState = uiState.copy(filterWeekend = isWeekend)
        updateFilteredFestivals()
    }

    fun onMinAgeFilterChanged(age: Int?) {
        uiState = uiState.copy(filterMinAge = age)
        updateFilteredFestivals()
    }

    fun onGenreFilterChanged(genre: String?) {
        uiState = uiState.copy(filterGenre = genre)
        updateFilteredFestivals()
    }

    fun onOutdoorFilterChanged(isOutdoor: Boolean?) {
        uiState = uiState.copy(filterOutdoor = isOutdoor)
        updateFilteredFestivals()
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
        updateFilteredFestivals()
        if (query.length >= 2) {
            viewModelScope.launch {
                val users = userRepository.searchUsers(query)
                uiState = uiState.copy(userSearchResults = users)
            }
        } else {
            uiState = uiState.copy(userSearchResults = emptyList())
        }
    }

    fun onSortOrderChanged(sortByDistance: Boolean) {
        uiState = uiState.copy(sortByDistance = sortByDistance)
        if (sortByDistance && uiState.userLocation == null) {
            requestLocation()
        }
        updateFilteredFestivals()
    }

    fun requestLocation() {
        viewModelScope.launch {
            _locationRequestSignal.emit(Unit)
        }
    }

    fun onDarkModeChanged(enabled: Boolean?) {
        uiState = uiState.copy(isDarkMode = enabled)
    }

    fun onUserLocationUpdated(location: LatLng) {
        uiState = uiState.copy(userLocation = location)
        updateFilteredFestivals()
    }

    private fun updateFilteredFestivals() {
        var filtered = uiState.allFestivals.filter { festival ->
            val matchesWeekend = uiState.filterWeekend?.let { festival.isWeekend == it } ?: true
            val matchesAge = uiState.filterMinAge?.let { festival.minAge >= it } ?: true
            val matchesGenre = uiState.filterGenre?.let { festival.genre.contains(it, ignoreCase = true) } ?: true
            val matchesOutdoor = uiState.filterOutdoor?.let { festival.isOutdoor == it } ?: true
            val matchesSearch = if (uiState.searchQuery.isBlank()) {
                true
            } else {
                festival.name.contains(uiState.searchQuery, ignoreCase = true) ||
                        festival.locationName.contains(uiState.searchQuery, ignoreCase = true)
            }
            matchesWeekend && matchesAge && matchesGenre && matchesOutdoor && matchesSearch
        }

        if (uiState.sortByDistance && uiState.userLocation != null) {
            filtered = filtered.sortedBy { festival ->
                calculateDistance(uiState.userLocation!!, festival.position)
            }
        }

        uiState = uiState.copy(filteredFestivals = filtered)
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val r = 6371.0 // Radius of the earth in km
        val dLat = (end.latitude - start.latitude) * PI / 180.0
        val dLon = (end.longitude - start.longitude) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(start.latitude * PI / 180.0) * cos(end.latitude * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }

    // --- User & Favorites Logic ---

    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            try {
                userRepository.login(email, passwordHash)
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    uiState = uiState.copy(currentUser = user)
                    observeFavorites(user.id)
                    observeAttendance(user.id)
                    observeFollowedArtists(user.id)
                    observeSelectedSets(user.id)
                }
            } catch (e: Exception) {
                // TODO: Handle error (e.g. Email not confirmed)
            }
        }
    }

    fun register(username: String, email: String, passwordHash: String) {
        viewModelScope.launch {
            try {
                userRepository.registerUser(username, email, passwordHash)
                // signUp starts, user needs to confirm email
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            uiState = uiState.copy(
                currentUser = null,
                favoriteIds = emptySet(),
                attendanceIds = emptySet(),
                followedArtistNames = emptySet(),
                viewingProfile = null,
                isFollowingViewingUser = false
            )
            observeFavorites(GUEST_USER_ID)
            observeAttendance(GUEST_USER_ID)
            observeFollowedArtists(GUEST_USER_ID)
        }
    }

    suspend fun resetPassword(email: String) {
        userRepository.resetPassword(email)
    }

    suspend fun updatePassword(newPassword: String) {
        userRepository.updatePassword(newPassword)
        // Refresh state
        val user = userRepository.getCurrentUser()
        if (user != null) {
            uiState = uiState.copy(currentUser = user)
        }
    }

    fun uploadAvatar(byteArray: ByteArray) {
        val userId = uiState.currentUser?.id ?: return
        viewModelScope.launch {
            try {
                val url = userRepository.uploadAvatar(userId, byteArray)
                val updatedUser = uiState.currentUser?.copy(avatarUrl = url)
                uiState = uiState.copy(currentUser = updatedUser)
            } catch (e: Exception) {
                // TODO: Handle upload error
            }
        }
    }

    fun updateCurrentUserProfile(bio: String?, age: Int?) {
        val userId = uiState.currentUser?.id ?: return
        viewModelScope.launch {
            try {
                userRepository.updateProfile(userId, bio, age)
                val updatedUser = uiState.currentUser?.copy(bio = bio, age = age)
                uiState = uiState.copy(currentUser = updatedUser)
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun loadPublicProfile(userId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(viewingProfile = null, isFollowingViewingUser = false)
            val profile = userRepository.getProfile(userId)
            if (profile != null) {
                val currentUserId = uiState.currentUser?.id
                val isFollowing = if (currentUserId != null) userRepository.isFollowing(currentUserId, userId) else false
                val followers = userRepository.getFollowerCount(userId)
                val following = userRepository.getFollowingCount(userId)
                
                uiState = uiState.copy(
                    viewingProfile = profile,
                    isFollowingViewingUser = isFollowing,
                    viewerFollowersCount = followers,
                    viewerFollowingCount = following
                )
            }
        }
    }

    fun toggleFollowUser(targetUserId: String) {
        val currentUserId = uiState.currentUser?.id ?: return
        val isFollowing = uiState.isFollowingViewingUser
        viewModelScope.launch {
            try {
                userRepository.toggleFollowUser(currentUserId, targetUserId, isFollowing)
                uiState = uiState.copy(
                    isFollowingViewingUser = !isFollowing,
                    viewerFollowersCount = if (isFollowing) uiState.viewerFollowersCount - 1 else uiState.viewerFollowersCount + 1
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun observeFavorites(userId: String) {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            userRepository.getFavorites(userId).collectLatest { favorites ->
                uiState = uiState.copy(favoriteIds = favorites.toSet())
            }
        }
    }

    fun toggleFavorite(festivalId: String) {
        val userId = uiState.currentUser?.id ?: return
        val isCurrentlyFav = uiState.favoriteIds.contains(festivalId)
        
        // Optimistic Update
        val newFavoriteIds = if (isCurrentlyFav) {
            uiState.favoriteIds - festivalId
        } else {
            uiState.favoriteIds + festivalId
        }
        uiState = uiState.copy(favoriteIds = newFavoriteIds)

        viewModelScope.launch {
            try {
                userRepository.toggleFavorite(userId, festivalId, isCurrentlyFav)
                updateHotCount(festivalId)
            } catch (e: Exception) {
                if (e.message?.contains("duplicate key") != true) {
                    // Rollback on non-duplicate errors
                    val rollbackIds = if (!isCurrentlyFav) {
                        uiState.favoriteIds - festivalId
                    } else {
                        uiState.favoriteIds + festivalId
                    }
                    uiState = uiState.copy(favoriteIds = rollbackIds)
                }
            }
        }
    }

    fun toggleAttendance(festivalId: String) {
        val userId = uiState.currentUser?.id ?: return
        val isAttending = uiState.attendanceIds.contains(festivalId)
        viewModelScope.launch {
            try {
                userRepository.toggleAttendance(userId, festivalId, isAttending)
                
                // Update local list of attendance IDs immediately for UI state
                val newAttendanceIds = if (isAttending) {
                    uiState.attendanceIds - festivalId
                } else {
                    uiState.attendanceIds + festivalId
                }
                uiState = uiState.copy(attendanceIds = newAttendanceIds)

                // Refresh community list from Supabase
                val attendees = userRepository.getAttendeesForFestival(festivalId)
                uiState = uiState.copy(communityAttendees = attendees)
                
                // Update overall counts (this also updates HOT logic)
                updateHotCount(festivalId)
            } catch (e: Exception) {
                // Handle error gracefully
            }
        }
    }

    private fun observeAttendance(userId: String) {
        attendanceJob?.cancel()
        attendanceJob = viewModelScope.launch {
            userRepository.getAttendance(userId).collectLatest { attendance ->
                uiState = uiState.copy(attendanceIds = attendance.toSet())
            }
        }
    }

    private fun observeFollowedArtists(userId: String) {
        followedArtistsJob?.cancel()
        followedArtistsJob = viewModelScope.launch {
            userRepository.getFollowedArtists(userId).collectLatest { artists ->
                uiState = uiState.copy(followedArtistNames = artists.toSet())
            }
        }
    }

    fun toggleFollowArtist(artistName: String) {
        val userId = uiState.currentUser?.id ?: GUEST_USER_ID
        val isFollowed = uiState.followedArtistNames.contains(artistName)
        viewModelScope.launch {
            userRepository.toggleFollowArtist(userId, artistName, isFollowed)
        }
    }

    private fun observeSelectedSets(userId: String) {
        selectedSetsJob?.cancel()
        selectedSetsJob = viewModelScope.launch {
            // We need the current festival ID to observe specific sets
            // For now, let's observe all and filter in UI state or updated repo
            // Simple version: observe when festival is selected or globally
        }
    }

    fun toggleSelectedSet(entry: com.example.eventradar.model.TimetableEntry) {
        val userId = uiState.currentUser?.id ?: GUEST_USER_ID
        val festival = uiState.selectedFestival ?: return
        val isSelected = uiState.selectedSetEntries.any { 
            it.artist == entry.artist && it.startTime == entry.startTime && it.stage == entry.stage 
        }
        viewModelScope.launch {
            userRepository.toggleSelectedSet(userId, festival.id, entry, isSelected)
            
            // Notification handling
            if (!isSelected) {
                scheduleNotificationsForSet(festival, entry)
            } else {
                cancelNotificationsForSet(festival, entry)
            }

            // Refresh observation
            userRepository.getSelectedSets(userId, festival.id).collectLatest { sets ->
                uiState = uiState.copy(selectedSetEntries = sets)
            }
        }
    }

    private fun scheduleNotificationsForSet(festival: Festival, entry: com.example.eventradar.model.TimetableEntry) {
        if (notificationManager == null) return
        
        try {
            // Parse Date: "23.05.2026"
            val dateParts = festival.date.split(".")
            if (dateParts.size != 3) return
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val year = dateParts[2].toInt()

            // Parse Time: "13:00"
            val timeParts = entry.startTime.split(":")
            if (timeParts.size != 2) return
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val localDateTime = LocalDateTime(year, month, day, hour, minute)
            val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
            val startTimeMillis = instant.toEpochMilliseconds()

            // 1. Notification at start
            val idStart = (festival.id + entry.artist + entry.startTime + "start").hashCode()
            notificationManager.scheduleNotification(
                id = idStart,
                title = "Event Start!",
                message = "${entry.artist} fängt jetzt auf der ${entry.stage} Stage an.",
                timeInMillis = startTimeMillis
            )

            // 2. Notification 20 minutes before
            val idPre = (festival.id + entry.artist + entry.startTime + "pre").hashCode()
            notificationManager.scheduleNotification(
                id = idPre,
                title = "Gleich geht's los!",
                message = "${entry.artist} spielt in 20 Minuten auf der ${entry.stage} Stage.",
                timeInMillis = startTimeMillis - (20 * 60 * 1000)
            )
        } catch (e: Exception) {
            // Log error
        }
    }

    private fun cancelNotificationsForSet(festival: Festival, entry: com.example.eventradar.model.TimetableEntry) {
        if (notificationManager == null) return
        val idStart = (festival.id + entry.artist + entry.startTime + "start").hashCode()
        val idPre = (festival.id + entry.artist + entry.startTime + "pre").hashCode()
        notificationManager.cancelNotification(idStart)
        notificationManager.cancelNotification(idPre)
    }

    fun onTimetableTabChanged(isPersonal: Boolean) {
        uiState = uiState.copy(showPersonalTimetable = isPersonal)
    }
}

data class FestivalUiState(
    val allFestivals: List<Festival> = emptyList(),
    val filteredFestivals: List<Festival> = emptyList(),
    val selectedFestival: Festival? = null,
    val filterWeekend: Boolean? = null,
    val filterMinAge: Int? = null,
    val filterGenre: String? = null,
    val filterOutdoor: Boolean? = null,
    val searchQuery: String = "",
    val userLocation: LatLng? = null,
    val sortByDistance: Boolean = false,
    val isDarkMode: Boolean? = null,
    val currentUser: UserEntity? = null,
    val favoriteIds: Set<String> = emptySet(),
    val attendanceIds: Set<String> = emptySet(),
    val festivalAttendanceCounts: Map<String, Int> = emptyMap(),
    val festivalFavoriteCounts: Map<String, Int> = emptyMap(),
    val festivalHotCounts: Map<String, Int> = emptyMap(),
    val followedArtistNames: Set<String> = emptySet(),
    val selectedArtist: Artist? = null,
    val selectedArtistFestivals: List<Festival> = emptyList(),
    val selectedOrganizer: String? = null,
    val selectedOrganizerFestivals: List<Festival> = emptyList(),
    val selectedSetEntries: List<com.example.eventradar.model.TimetableEntry> = emptyList(),
    val showPersonalTimetable: Boolean = false,
    val communityAttendees: List<com.example.eventradar.data.ProfileRow> = emptyList(),
    val viewingProfile: com.example.eventradar.data.ProfileRow? = null,
    val isFollowingViewingUser: Boolean = false,
    val viewerFollowersCount: Int = 0,
    val viewerFollowingCount: Int = 0,
    val userSearchResults: List<com.example.eventradar.data.ProfileRow> = emptyList()
)
