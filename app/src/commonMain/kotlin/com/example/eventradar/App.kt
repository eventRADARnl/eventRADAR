package com.example.eventradar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.eventradar.model.Festival
import com.example.eventradar.ui.*
import com.example.eventradar.ui.theme.EventRADARTheme
import kotlinx.coroutines.launch

@Composable
fun App(
    viewModel: FestivalViewModel,
    mapScreen: @Composable (viewModel: FestivalViewModel, onFestivalClick: (Festival) -> Unit) -> Unit = { _, _ -> 
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Map not available") } 
    },
    adBanner: @Composable () -> Unit = {}
) {
    val darkTheme = viewModel.uiState.isDarkMode ?: isSystemInDarkTheme()
    EventRADARTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 7 })
        val coroutineScope = rememberCoroutineScope()
        
        // Sync Pager with Navigation
        LaunchedEffect(currentDestination) {
            val targetPage = when {
                currentDestination?.hierarchy?.any { it.route == "explore" } == true -> 0
                currentDestination?.hierarchy?.any { it.route == "hot" } == true -> 1
                currentDestination?.hierarchy?.any { it.route == "map" } == true -> 2
                currentDestination?.hierarchy?.any { it.route == "landing" } == true -> 3
                currentDestination?.hierarchy?.any { it.route == "my_events" } == true -> 4
                currentDestination?.hierarchy?.any { it.route == "settings" } == true -> 5
                currentDestination?.hierarchy?.any { it.route == "account" } == true -> 6
                else -> -1
            }
            if (targetPage != -1 && targetPage != pagerState.currentPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
        
        // Sync Navigation with Pager
        LaunchedEffect(pagerState.currentPage) {
            val route = when(pagerState.currentPage) {
                0 -> "explore"
                1 -> "hot"
                2 -> "map"
                3 -> "landing"
                4 -> "my_events"
                5 -> "settings"
                6 -> "account"
                else -> null
            }
            if (route != null && currentDestination?.route != route) {
                navController.navigate(route) {
                    popUpTo("explore") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Column {
                        // Kleines Werbebanner über der Navigationsleiste
                        val showBanner = currentDestination?.route !in listOf("detail", "auth")
                        if (showBanner) {
                            adBanner()
                        }
                        
                        val showMainBottomBar = currentDestination?.route !in listOf(
                            "detail", "detail_tickets", "detail_lineup", "detail_timetable", "detail_floorplan", "artist", "organizer"
                        )
                        if (showMainBottomBar) {
                            ScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = Color(0xFF044474),
                                contentColor = Color.White,
                                edgePadding = 0.dp,
                                divider = {},
                                indicator = {}
                            ) {
                                val items = listOf(
                                    Triple("explore", Icons.Default.Home, "Entdecken"),
                                    Triple("hot", Icons.Default.Favorite, "HOT"),
                                    Triple("map", Icons.Default.LocationOn, "Karte"),
                                    Triple("landing", Icons.AutoMirrored.Filled.List, "Liste"),
                                    Triple("my_events", Icons.Default.FavoriteBorder, "Meine Events"),
                                    Triple("settings", Icons.Default.Settings, "Einstellungen"),
                                    Triple("account", Icons.Default.Person, "Profil")
                                )
                                
                                val mainRoutes = listOf("explore", "hot", "map", "landing", "my_events", "settings", "account")
                                val isMainRoute = currentDestination?.route in mainRoutes || currentDestination?.route?.startsWith("profile/") == true
                                
                                items.forEachIndexed { index, item ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            if (index == 2) {
                                                viewModel.onFestivalSelected(null)
                                            }
                                            coroutineScope.launch {
                                                pagerState.scrollToPage(index)
                                            }
                                        },
                                        text = { Text(item.third, style = MaterialTheme.typography.labelSmall) },
                                        icon = { Icon(item.second, contentDescription = item.third) },
                                        selectedContentColor = Color.White,
                                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "explore",
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                val mainPager = @Composable {
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false
                    ) { page ->
                        when(page) {
                            0 -> ExploreScreen(
                                viewModel = viewModel,
                                onFestivalClick = { festival ->
                                    viewModel.onFestivalSelected(festival)
                                    navController.navigate("detail")
                                },
                                onGenreClick = { genre ->
                                    viewModel.onGenreFilterChanged(genre)
                                    navController.navigate("landing")
                                },
                                onProfileClick = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(6)
                                    }
                                },
                                adBanner = adBanner
                            )
                            1 -> HotScreen(
                                viewModel = viewModel,
                                onFestivalClick = { festival ->
                                    viewModel.onFestivalSelected(festival)
                                    navController.navigate("detail")
                                }
                            )
                            2 -> mapScreen(viewModel) { festival ->
                                viewModel.onFestivalSelected(festival)
                                navController.navigate("detail")
                            }
                            3 -> FestivalListScreen(
                                viewModel = viewModel,
                                navController = navController,
                                onFestivalClick = { festival ->
                                    viewModel.onFestivalSelected(festival)
                                    navController.navigate("detail")
                                }
                            )
                            4 -> MyEventsScreen(
                                viewModel = viewModel,
                                onBackClick = { /* Tab doesn't need back */ },
                                onFestivalClick = { festival ->
                                    viewModel.onFestivalSelected(festival)
                                    navController.navigate("detail")
                                }
                            )
                            5 -> SettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { 
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(0)
                                    }
                                }
                            )
                            6 -> AccountScreen(
                                viewModel = viewModel,
                                onMyEventsClick = { navController.navigate("my_events") },
                                onSettingsClick = { navController.navigate("settings") },
                                onLoginClick = { navController.navigate("auth") }
                            )
                        }
                    }
                }
                composable("explore") { mainPager() }
                composable("hot") { mainPager() }
                composable("map") { mainPager() }
                composable("landing") { mainPager() }
                composable("my_events") { mainPager() }
                composable("settings") { mainPager() }
                composable("account") { mainPager() }
                composable("profile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    LaunchedEffect(userId) {
                        viewModel.loadPublicProfile(userId)
                    }
                    PublicProfileScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("auth") {
                    AuthScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onAuthSuccess = { navController.popBackStack() }
                    )
                }
                composable("detail") {
                    val uiState = viewModel.uiState
                    uiState.selectedFestival?.let { festival ->
                        EventDetailScreen(
                            festival = festival,
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            isFavorite = uiState.favoriteIds.contains(festival.id),
                            onFavoriteClick = { 
                                if (uiState.currentUser != null) {
                                    viewModel.toggleFavorite(festival.id)
                                } else {
                                    navController.navigate("auth")
                                }
                            },
                            isAttending = uiState.attendanceIds.contains(festival.id),
                            onAttendanceClick = { 
                                if (uiState.currentUser != null) {
                                    viewModel.toggleAttendance(festival.id)
                                } else {
                                    navController.navigate("auth")
                                }
                            },
                            attendanceCount = uiState.festivalAttendanceCounts[festival.id] ?: 0,
                            onFollowArtistClick = { viewModel.toggleFollowArtist(it) },
                            onOrganizerClick = { organizerName ->
                                viewModel.onOrganizerSelected(organizerName)
                                navController.navigate("organizer")
                            },
                            onLocationClick = {
                                navController.navigate("map") {
                                    popUpTo("explore") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onTicketsClick = { navController.navigate("detail_tickets") },
                            onLineUpClick = { navController.navigate("detail_lineup") },
                            onTimetableClick = { navController.navigate("detail_timetable") },
                            onFloorplanClick = { navController.navigate("detail_floorplan") },
                            onProfileClick = { userId ->
                                navController.navigate("profile/$userId")
                            }
                        )
                    }
                }
                composable("detail_tickets") {
                    viewModel.uiState.selectedFestival?.let { festival ->
                        TicketsScreen(festival = festival, onBackClick = { navController.popBackStack() })
                    }
                }
                composable("detail_lineup") {
                    viewModel.uiState.selectedFestival?.let { festival ->
                        LineUpScreen(
                            festival = festival,
                            onBackClick = { navController.popBackStack() },
                            onArtistClick = { artistName ->
                                viewModel.onArtistSelected(artistName)
                                navController.navigate("artist")
                            },
                            followedArtistNames = viewModel.uiState.followedArtistNames,
                            onToggleFollowArtist = { viewModel.toggleFollowArtist(it) }
                        )
                    }
                }
                composable("detail_timetable") {
                    viewModel.uiState.selectedFestival?.let { festival ->
                        TimetableScreen(
                            festival = festival, 
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
                composable("detail_floorplan") {
                    viewModel.uiState.selectedFestival?.let { festival ->
                        FloorplanScreen(festival = festival, onBackClick = { navController.popBackStack() })
                    }
                }
                composable("artist") {
                    val uiState = viewModel.uiState
                    uiState.selectedArtist?.let { artist ->
                        ArtistProfileScreen(
                            artist = artist,
                            festivals = uiState.selectedArtistFestivals,
                            attendanceCounts = uiState.festivalAttendanceCounts,
                            isFollowed = uiState.followedArtistNames.contains(artist.name),
                            onFollowClick = { viewModel.toggleFollowArtist(artist.name) },
                            onBackClick = { navController.popBackStack() },
                            onFestivalClick = { festival ->
                                viewModel.onFestivalSelected(festival)
                                navController.navigate("detail")
                            },
                            isLoggedIn = uiState.currentUser != null
                        )
                    }
                }
                composable("organizer") {
                    val uiState = viewModel.uiState
                    uiState.selectedOrganizer?.let { organizerName ->
                        OrganizerEventsScreen(
                            organizerName = organizerName,
                            festivals = uiState.selectedOrganizerFestivals,
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            onFestivalClick = { festival ->
                                viewModel.onFestivalSelected(festival)
                                navController.navigate("detail")
                            }
                        )
                    }
                }
            }
        }
    }
}
}
