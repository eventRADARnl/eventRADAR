package com.example.eventradar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.eventradar.model.Festival

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    viewModel: FestivalViewModel,
    onBackClick: () -> Unit,
    onFestivalClick: (Festival) -> Unit
) {
    val uiState = viewModel.uiState
    val myFestivals = uiState.allFestivals.filter { 
        uiState.attendanceIds.contains(it.id) || uiState.favoriteIds.contains(it.id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meine Events", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBackClick != {}) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF044474),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (myFestivals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Noch keine Events geplant.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Markiere Festivals als Favorit oder klicke auf 'Dabei'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                items(myFestivals) { festival ->
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
