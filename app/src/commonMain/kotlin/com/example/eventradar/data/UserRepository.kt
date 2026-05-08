package com.example.eventradar.data

import com.example.eventradar.data.database.AttendanceDao
import com.example.eventradar.data.database.FavoriteDao
import com.example.eventradar.data.database.FollowedArtistDao
import com.example.eventradar.data.database.UserDao
import com.example.eventradar.data.database.UserEntity
import com.example.eventradar.model.TimetableEntry
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class FavoriteRow(val user_id: String? = null, val festival_id: String)

@Serializable
data class AttendanceRow(val user_id: String? = null, val festival_id: String)

@Serializable
data class FollowedArtistRow(val user_id: String, val artist_name: String)

@Serializable
data class SelectedSetRow(val user_id: String, val festival_id: String, val artist: String, val start_time: String, val stage: String)

@Serializable
data class ProfileRow(
    val id: String,
    val username: String? = null,
    val avatar_url: String? = null,
    val bio: String? = null,
    val age: Int? = null
)

@Serializable
data class UserFollowRow(val follower_id: String, val followed_id: String)

class UserRepository(
    private val userDao: UserDao,
    private val favoriteDao: FavoriteDao,
    private val attendanceDao: AttendanceDao,
    private val followedArtistDao: FollowedArtistDao
) {
    private val supabase = SupabaseConfig.client

    suspend fun registerUser(username: String, email: String, passwordHash: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = passwordHash
            data = buildJsonObject {
                put("username", JsonPrimitive(username))
            }
        }
    }

    suspend fun login(email: String, passwordHash: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = passwordHash
        }
    }

    suspend fun loadSession() {
        supabase.auth.loadFromStorage()
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun resetPassword(email: String) {
        supabase.auth.resetPasswordForEmail(email)
    }

    suspend fun updatePassword(newPassword: String) {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    fun getCurrentUser(): UserEntity? {
        val user = supabase.auth.currentUserOrNull() ?: return null
        return UserEntity(
            id = user.id,
            username = user.userMetadata?.get("username")?.toString()?.replace("\"", "") ?: "Nutzer",
            email = user.email ?: "",
            avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", ""),
            passwordHash = ""
        )
    }

    suspend fun uploadAvatar(userId: String, byteArray: ByteArray): String {
        // Pfad angepasst auf avatars/userId/avatar.jpg für die Sicherheits-Policy
        val fileName = "$userId/avatar.jpg"
        val bucket = supabase.storage.from("avatars")
        
        // Upload durchführen
        bucket.upload(path = fileName, data = byteArray)
        
        val url = bucket.publicUrl(fileName)
        
        // 1. Update user metadata in Supabase Auth (privat)
        supabase.auth.updateUser {
            data = buildJsonObject {
                put("avatar_url", JsonPrimitive(url))
            }
        }

        // 2. Update öffentliche Profile-Tabelle
        try {
            supabase.postgrest["profiles"].update(
                buildJsonObject {
                    put("avatar_url", JsonPrimitive(url))
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
        } catch (e: Exception) {
            // Falls das Profil noch nicht existiert
            supabase.postgrest["profiles"].upsert(
                ProfileRow(id = userId, avatar_url = url)
            )
        }

        return url
    }

    suspend fun getProfile(userId: String): ProfileRow? {
        return try {
            supabase.postgrest["profiles"].select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<ProfileRow>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(userId: String, bio: String?, age: Int?) {
        try {
            supabase.postgrest["profiles"].update(
                buildJsonObject {
                    put("bio", JsonPrimitive(bio))
                    put("age", JsonPrimitive(age))
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
        } catch (e: Exception) {
            // Falls das Profil noch nicht existiert
            supabase.postgrest["profiles"].upsert(
                ProfileRow(id = userId, bio = bio, age = age)
            )
        }
    }

    suspend fun searchUsers(query: String): List<ProfileRow> {
        return try {
            supabase.postgrest["profiles"].select {
                filter {
                    ilike("username", "%$query%")
                }
            }.decodeList<ProfileRow>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleFollowUser(followerId: String, followedId: String, isFollowing: Boolean) {
        if (isFollowing) {
            supabase.postgrest["user_follows"].delete {
                filter {
                    eq("follower_id", followerId)
                    eq("followed_id", followedId)
                }
            }
        } else {
            supabase.postgrest["user_follows"].insert(UserFollowRow(followerId, followedId))
        }
    }

    suspend fun isFollowing(followerId: String, followedId: String): Boolean {
        return try {
            val response = supabase.postgrest["user_follows"].select {
                filter {
                    eq("follower_id", followerId)
                    eq("followed_id", followedId)
                }
            }.decodeList<UserFollowRow>()
            response.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFollowerCount(userId: String): Int {
        return try {
            val response = supabase.postgrest["user_follows"].select(columns = Columns.list("follower_id")) {
                filter {
                    eq("followed_id", userId)
                }
            }.decodeList<UserFollowRow>()
            response.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getFollowingCount(userId: String): Int {
        return try {
            val response = supabase.postgrest["user_follows"].select(columns = Columns.list("followed_id")) {
                filter {
                    eq("follower_id", userId)
                }
            }.decodeList<UserFollowRow>()
            response.size
        } catch (e: Exception) {
            0
        }
    }

    // --- Supabase Cloud Sync Methods ---

    suspend fun getAttendeesForFestival(festivalId: String): List<ProfileRow> {
        return try {
            val attendance = supabase.postgrest["attendance"]
                .select(columns = Columns.list("user_id")) {
                    filter {
                        eq("festival_id", festivalId)
                    }
                }
                .decodeList<AttendanceRow>()
            
            val userIds = attendance.map { it.user_id }.filterNotNull()
            if (userIds.isEmpty()) return emptyList()

            supabase.postgrest["profiles"]
                .select {
                    filter {
                        isIn("id", userIds)
                    }
                }
                .decodeList<ProfileRow>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getFavorites(userId: String): Flow<List<String>> = flow {
        try {
            val response = supabase.postgrest["favorites"]
                .select(columns = Columns.list("festival_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FavoriteRow>()
            emit(response.map { it.festival_id })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun toggleFavorite(userId: String, festivalId: String, isFavorite: Boolean) {
        if (isFavorite) {
            supabase.postgrest["favorites"].delete {
                filter {
                    eq("user_id", userId)
                    eq("festival_id", festivalId)
                }
            }
        } else {
            supabase.postgrest["favorites"].insert(FavoriteRow(userId, festivalId))
        }
    }

    fun getAttendance(userId: String): Flow<List<String>> = flow {
        try {
            val response = supabase.postgrest["attendance"]
                .select(columns = Columns.list("festival_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<AttendanceRow>()
            emit(response.map { it.festival_id })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun toggleAttendance(userId: String, festivalId: String, isAttending: Boolean) {
        try {
            if (isAttending) {
                supabase.postgrest["attendance"].delete {
                    filter {
                        eq("user_id", userId)
                        eq("festival_id", festivalId)
                    }
                }
            } else {
                // Nutze upsert statt insert, um "duplicate key" Fehler zu vermeiden
                supabase.postgrest["attendance"].upsert(AttendanceRow(userId, festivalId))
            }
        } catch (e: Exception) {
            // Log error or handle gracefully
        }
    }

    fun getFollowedArtists(userId: String): Flow<List<String>> = flow {
        try {
            val response = supabase.postgrest["followed_artists"]
                .select(columns = Columns.list("artist_name")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FollowedArtistRow>()
            emit(response.map { it.artist_name })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun toggleFollowArtist(userId: String, artistName: String, isFollowed: Boolean) {
        if (isFollowed) {
            supabase.postgrest["followed_artists"].delete {
                filter {
                    eq("user_id", userId)
                    eq("artist_name", artistName)
                }
            }
        } else {
            supabase.postgrest["followed_artists"].insert(FollowedArtistRow(userId, artistName))
        }
    }

    // --- Timetable Selection Methods ---

    fun getSelectedSets(userId: String, festivalId: String): Flow<List<TimetableEntry>> = flow {
        try {
            val response = supabase.postgrest["selected_sets"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("festival_id", festivalId)
                    }
                }
                .decodeList<SelectedSetRow>()
            emit(response.map { TimetableEntry(it.start_time, "", it.artist, it.stage) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun toggleSelectedSet(userId: String, festivalId: String, entry: TimetableEntry, isSelected: Boolean) {
        if (isSelected) {
            supabase.postgrest["selected_sets"].delete {
                filter {
                    eq("user_id", userId)
                    eq("festival_id", festivalId)
                    eq("artist", entry.artist)
                    eq("start_time", entry.startTime)
                    eq("stage", entry.stage)
                }
            }
        } else {
            supabase.postgrest["selected_sets"].insert(SelectedSetRow(userId, festivalId, entry.artist, entry.startTime, entry.stage))
        }
    }

    // Hot Counts (Temporary: use local count for UI if needed, or query Supabase)
    fun getAttendanceCount(festivalId: String): Flow<Int> = attendanceDao.getAttendanceCountForFestival(festivalId)
    fun getFavoriteCount(festivalId: String): Flow<Int> = favoriteDao.getFavoriteCountForFestival(festivalId)
}
