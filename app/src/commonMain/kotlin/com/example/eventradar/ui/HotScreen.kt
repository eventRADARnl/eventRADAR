package com.example.eventradar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventradar.model.Festival

@Composable
fun HotScreen(
    viewModel: FestivalViewModel,
    onFestivalClick: (Festival) -> Unit
) {
    val uiState = viewModel.uiState
    val hotFestivals = uiState.allFestivals.filter { 
        (uiState.festivalHotCounts[it.id] ?: 0) >= 2 
    }.sortedByDescending { uiState.festivalHotCounts[it.id] ?: 0 }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
            ) {
                Text(
                    text = "HOT Events",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Diese Festivals sind gerade besonders beliebt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { innerPadding ->
        if (hotFestivals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Momentan keine HOT Events verfügbar.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                items(hotFestivals) { festival ->
                    FestivalListItem(
                        festival = festival,
                        userLocation = uiState.userLocation,
                        isFavorite = uiState.favoriteIds.contains(festival.id),
                        onFavoriteToggle = { viewModel.toggleFavorite(festival.id) },
                        isAttending = uiState.attendanceIds.contains(festival.id),
                        hotCount = uiState.festivalHotCounts[festival.id] ?: 0,
                        onClick = { onFestivalClick(festival) }
                    )
                }
            }
        }
    }
}
