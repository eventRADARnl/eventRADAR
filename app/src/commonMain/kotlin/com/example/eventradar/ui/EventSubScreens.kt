package com.example.eventradar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.eventradar.R
import com.example.eventradar.data.FestivalRepository
import com.example.eventradar.model.Artist
import com.example.eventradar.model.Festival
import com.example.eventradar.model.TimetableEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenHeader(
    title: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF044474),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
fun TicketsScreen(
    festival: Festival,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { SubScreenHeader(stringResource(R.string.tickets_title), onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tickets für ${festival.name}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Hier kannst du Tickets für das Festival erwerben.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* Externen Link öffnen */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tickets kaufen")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineUpScreen(
    festival: Festival,
    onBackClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    followedArtistNames: Set<String> = emptySet(),
    onToggleFollowArtist: (String) -> Unit = {}
) {
    var showSelectionDialog by remember { mutableStateOf<List<Artist>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Tag-Auswahl (z.B. Freitag, Samstag, Sonntag)
    val days = festival.lineUpByDay.keys.toList()
    var selectedDay by remember(festival.id) { 
        mutableStateOf(if (days.isNotEmpty()) days.first() else null) 
    }
    
    // Stage-Auswahl für den gewählten Tag
    val stages = selectedDay?.let { festival.lineUpByDay[it]?.keys?.toList() } ?: emptyList()
    var selectedStage by remember(selectedDay) { 
        mutableStateOf(if (stages.isNotEmpty()) stages.first() else null) 
    }
    
    val currentLineup = if ((selectedDay != null) && (selectedStage != null)) {
        festival.lineUpByDay[selectedDay]!![selectedStage] ?: emptyList()
    } else {
        festival.lineUp
    }

    val filteredLineup = if (searchQuery.isEmpty()) {
        currentLineup
    } else {
        currentLineup.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = { SubScreenHeader(stringResource(R.string.lineup_title), onBackClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Day Tabs
            if (days.size > 1) {
                TabRow(
                    selectedTabIndex = days.indexOf(selectedDay),
                    containerColor = Color(0xFF044474),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[days.indexOf(selectedDay)]),
                            color = Color.White
                        )
                    }
                ) {
                    days.forEach { day ->
                        Tab(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            text = { Text(day) }
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Künstler suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                } else null,
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
            
            // Stage Selection (ScrollableTabRow für viele Stages)
            if (stages.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = stages.indexOf(selectedStage),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[stages.indexOf(selectedStage)]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    stages.forEach { stage ->
                        Tab(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            text = { 
                                Text(
                                    text = stage, 
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelLarge
                                ) 
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLineup) { lineupEntry ->
                        val artistsInEntry = FestivalRepository.findArtistsInLineup(lineupEntry)
                        
                        ArtistLineupItem(
                            lineupEntry = lineupEntry,
                            artists = artistsInEntry,
                            isFollowed = artistsInEntry.isNotEmpty() && artistsInEntry.any { followedArtistNames.contains(it.name) },
                            onArtistClick = { onArtistClick(it) },
                            onFollowClick = {
                                if (artistsInEntry.size == 1) {
                                    onToggleFollowArtist(artistsInEntry.first().name)
                                } else if (artistsInEntry.size > 1) {
                                    // Wenn mindestens einer gefolgt wird, alle entfolgen. Sonst alle folgen.
                                    val anyFollowed = artistsInEntry.any { followedArtistNames.contains(it.name) }
                                    artistsInEntry.forEach { artist ->
                                        val isThisArtistFollowed = followedArtistNames.contains(artist.name)
                                        if (anyFollowed && isThisArtistFollowed) {
                                            onToggleFollowArtist(artist.name)
                                        } else if (!anyFollowed) {
                                            onToggleFollowArtist(artist.name)
                                        }
                                    }
                                }
                            },
                            onMultipleArtistsClick = { showSelectionDialog = it }
                        )
                    }

                    if (filteredLineup.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp), 
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (searchQuery.isEmpty()) "Kein Line-up für diese Auswahl verfügbar." 
                                    else "Keine Künstler gefunden.", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Auswahl-Dialog bei mehreren Künstlern
                showSelectionDialog?.let { artists ->
                    AlertDialog(
                        onDismissRequest = { showSelectionDialog = null },
                        title = { 
                            Text(
                                text = "Künstler auswählen", 
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                artists.forEach { artist ->
                                    val isArtistFollowed = followedArtistNames.contains(artist.name)
                                    Surface(
                                        onClick = {
                                            showSelectionDialog = null
                                            onArtistClick(artist.name)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(40.dp)) {
                                                AsyncImage(
                                                    model = artist.imageUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = artist.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { onToggleFollowArtist(artist.name) }
                                            ) {
                                                Icon(
                                                    imageVector = if (isArtistFollowed) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Follow",
                                                    tint = if (isArtistFollowed) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSelectionDialog = null }) {
                                Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistLineupItem(
    lineupEntry: String,
    artists: List<Artist>,
    isFollowed: Boolean,
    onArtistClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    onMultipleArtistsClick: (List<Artist>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (artists.size == 1) {
                onArtistClick(artists.first().name)
            } else if (artists.size > 1) {
                onMultipleArtistsClick(artists)
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist Image(s)
            Box(modifier = Modifier.size(48.dp)) {
                if (artists.isNotEmpty()) {
                    AsyncImage(
                        model = artists.first().imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lineupEntry,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (artists.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                )
                if (artists.size > 1) {
                    Text(
                        text = "${artists.size} Künstler",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (artists.isNotEmpty()) {
                IconButton(onClick = onFollowClick) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Follow",
                        tint = if (isFollowed) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TimetableScreen(
    festival: Festival,
    viewModel: FestivalViewModel,
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState
    val days = festival.timetable.keys.toList()
    var selectedDay by remember(festival.id) { 
        mutableStateOf(if (days.isNotEmpty()) days.first() else null) 
    }
    
    val stages = selectedDay?.let { day -> 
        festival.timetable[day]?.map { it.stage }?.distinct() 
    } ?: emptyList()
    
    var selectedStage by remember(selectedDay) { 
        mutableStateOf(if (stages.isNotEmpty()) stages.first() else null) 
    }
    
    val currentTimetable = if (selectedDay != null) {
        val baseList = festival.timetable[selectedDay] ?: emptyList()
        val filteredList = if (uiState.showPersonalTimetable) {
            baseList.filter { entry ->
                uiState.selectedSetEntries.any { 
                    it.artist == entry.artist && it.startTime == entry.startTime && it.stage == entry.stage 
                }
            }
        } else {
            if (selectedStage != null) baseList.filter { it.stage == selectedStage } else baseList
        }
        filteredList.sortedBy { it.startTime }
    } else {
        emptyList()
    }

    Scaffold(
        topBar = { SubScreenHeader("Timetable", onBackClick) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // View Switcher (Full vs Personal)
            TabRow(
                selectedTabIndex = if (uiState.showPersonalTimetable) 1 else 0,
                containerColor = Color.White,
                contentColor = Color(0xFF044474),
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (uiState.showPersonalTimetable) 1 else 0]),
                        color = Color(0xFF044474)
                    )
                }
            ) {
                Tab(
                    selected = !uiState.showPersonalTimetable,
                    onClick = { viewModel.onTimetableTabChanged(false) },
                    text = { Text("Alle Stages") }
                )
                Tab(
                    selected = uiState.showPersonalTimetable,
                    onClick = { viewModel.onTimetableTabChanged(true) },
                    text = { Text("Mein Timetable") }
                )
            }

            // Day Selection
            if (days.size > 1) {
                TabRow(
                    selectedTabIndex = days.indexOf(selectedDay),
                    containerColor = Color(0xFF044474),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[days.indexOf(selectedDay)]),
                            color = Color.White
                        )
                    }
                ) {
                    days.forEach { day ->
                        Tab(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            text = { Text(day) }
                        )
                    }
                }
            }

            // Stage Selection (Horizontal Scrollable) - Hidden in Personal View
            if (!uiState.showPersonalTimetable && stages.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = stages.indexOf(selectedStage),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[stages.indexOf(selectedStage)]),
                            color = Color(0xFF044474)
                        )
                    }
                ) {
                    stages.forEach { stage ->
                        Tab(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            text = { 
                                Text(
                                    text = stage, 
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedStage == stage) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }

            // List of Sets
            if (currentTimetable.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentTimetable) { entry ->
                        val isSelected = uiState.selectedSetEntries.any { 
                            it.artist == entry.artist && it.startTime == entry.startTime && it.stage == entry.stage 
                        }
                        
                        // Clash Detection: Check if another selected set starts at the same time
                        val isClashing = if (isSelected && uiState.showPersonalTimetable) {
                            uiState.selectedSetEntries.any { other ->
                                (entry.artist != other.artist || entry.stage != other.stage) && 
                                entry.startTime == other.startTime
                            }
                        } else false

                        TimetableItem(
                            entry = entry, 
                            isSelected = isSelected,
                            onToggle = { viewModel.toggleSelectedSet(entry) },
                            showStage = uiState.showPersonalTimetable,
                            isClashing = isClashing
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.showPersonalTimetable) "Noch keine Acts markiert." else "Kein Timetable verfügbar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TimetableItem(
    entry: TimetableEntry,
    isSelected: Boolean,
    onToggle: () -> Unit,
    showStage: Boolean = false,
    isClashing: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isClashing -> Color(0xFFFFEBEE)
                isSelected -> Color(0xFF044474).copy(alpha = 0.05f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onToggle,
        border = if (isClashing) BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Column
            Column(
                modifier = Modifier.width(70.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = entry.startTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isClashing) Color.Red else Color(0xFF044474)
                )
                Text(
                    text = entry.endTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            
            // Divider Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(if (isClashing) Color.Red.copy(alpha = 0.3f) else Color(0xFF044474).copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Artist Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.artist,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isClashing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Clash",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (showStage) {
                    Text(
                        text = entry.stage,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF044474)
                    )
                }
            }

            // Selection Icon
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                contentDescription = null,
                tint = when {
                    isClashing -> Color.Red
                    isSelected -> Color(0xFF2E7D32)
                    else -> Color(0xFF044474).copy(alpha = 0.4f)
                }
            )
        }
    }
}

@Composable
fun FloorplanScreen(
    festival: Festival,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { SubScreenHeader(stringResource(R.string.floorplan_title), onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Floorplan für ${festival.name}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Hier findest du den Geländeplan des Festivals.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
