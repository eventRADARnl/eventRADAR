package com.example.eventradar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.eventradar.model.Festival
import com.example.eventradar.ui.components.EventMarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: FestivalViewModel,
    onFestivalClick: (Festival) -> Unit
) {
    val uiState = viewModel.uiState
    val netherlands = LatLng(52.1326, 5.2913)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(netherlands, 7f)
    }

    // Focus on selected festival
    LaunchedEffect(uiState.selectedFestival) {
        val selected = uiState.selectedFestival
        if (selected != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(selected.position.latitude, selected.position.longitude),
                    12f
                ),
                durationMs = 1000
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false
            )
        ) {
            uiState.filteredFestivals.forEach { festival ->
                EventMarker(
                    festival = festival,
                    onClick = { viewModel.onFestivalSelected(festival) }
                )
            }
        }

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = { /* Done via filter */ },
                active = false,
                onActiveChange = { },
                placeholder = { Text("Event oder Stadt suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = SearchBarDefaults.colors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                content = {}
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Genre Filter
                FilterChip(
                    selected = uiState.filterGenre != null,
                    onClick = { 
                        // Logic to cycle or open dialog
                    },
                    label = { Text(uiState.filterGenre ?: "Alle Genres") },
                    leadingIcon = { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        selectedContainerColor = Color(0xFF044474),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )

                // Type Filter
                FilterChip(
                    selected = uiState.filterWeekend != null,
                    onClick = {
                        val next = when(uiState.filterWeekend) {
                            null -> false
                            false -> true
                            true -> null
                        }
                        viewModel.onTypeFilterChanged(next)
                    },
                    label = { 
                        Text(when(uiState.filterWeekend) {
                            true -> "Wochenende"
                            false -> "Tag"
                            else -> "Alle Typen"
                        })
                    },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.9f))
                )

                // Reset Filter
                if (uiState.filterGenre != null || uiState.filterWeekend != null || uiState.filterMinAge != null) {
                    IconButton(
                        onClick = {
                            viewModel.onGenreFilterChanged(null)
                            viewModel.onTypeFilterChanged(null)
                            viewModel.onMinAgeFilterChanged(null)
                        },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.9f), CircleShape).size(32.dp)
                    ) {
                        Icon(Icons.Default.FilterListOff, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Floating Action Buttons
        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (uiState.selectedFestival != null) 180.dp else 32.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.requestLocation() },
                containerColor = Color.White,
                contentColor = Color(0xFF044474),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Standort")
            }

            FloatingActionButton(
                onClick = {
                    val builder = LatLngBounds.Builder()
                    uiState.filteredFestivals.forEach { festival ->
                        builder.include(LatLng(festival.position.latitude, festival.position.longitude))
                    }
                    try {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
                        }
                    } catch (e: Exception) {}
                },
                containerColor = Color.White,
                contentColor = Color(0xFF044474),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Übersicht")
            }
        }

        // Bottom Preview Card
        uiState.selectedFestival?.let { festival ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                onClick = { onFestivalClick(festival) }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(0.4f)) {
                        AsyncImage(
                            model = festival.listImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Hot badge if applicable
                        if ((uiState.festivalHotCounts[festival.id] ?: 0) >= 2) {
                            Surface(
                                color = Color.Red,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                            ) {
                                Text("HOT", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .padding(16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(festival.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(festival.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(festival.locationName, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                        }
                        
                        Text(
                            text = "Details ansehen",
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color(0xFF044474),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            // Close button for preview
            IconButton(
                onClick = { viewModel.onFestivalSelected(null) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 350.dp, end = 24.dp) // Just a rough alignment check, better to anchor to card
                    .background(Color.White, CircleShape)
                    .size(32.dp)
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
