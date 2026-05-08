package com.example.eventradar.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventradar.model.Festival
import com.example.eventradar.model.LatLng

@Composable
fun OrganizerEventsScreen(
    organizerName: String,
    festivals: List<Festival>,
    viewModel: FestivalViewModel,
    onBackClick: () -> Unit,
    onFestivalClick: (Festival) -> Unit
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF044474)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Veranstalter",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = organizerName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(festivals) { festival ->
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
