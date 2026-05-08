package com.example.eventradar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.eventradar.model.Artist
import com.example.eventradar.model.Festival

@Composable
fun ArtistProfileScreen(
    artist: Artist,
    festivals: List<Festival>,
    attendanceCounts: Map<String, Int> = emptyMap(),
    onBackClick: () -> Unit,
    onFestivalClick: (Festival) -> Unit,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    isLoggedIn: Boolean
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Profile Photo
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (artist.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enabled for all users as guest favoriting is supported locally
                Button(
                    onClick = onFollowClick,
                    colors = if (isFollowed) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isFollowed) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.following))
                    } else {
                        Text(stringResource(R.string.follow))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Upcoming Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (festivals.isEmpty()) {
                item {
                    Text(
                        "Keine geplanten Auftritte in der App gefunden.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(festivals) { festival ->
                    FestivalListItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        festival = festival,
                        attendanceCount = attendanceCounts[festival.id] ?: 0,
                        onClick = { onFestivalClick(festival) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FestivalListItem(
    modifier: Modifier = Modifier,
    festival: Festival,
    attendanceCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = festival.listImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (attendanceCount >= 2) {
                    Surface(
                        modifier = Modifier.padding(4.dp).align(Alignment.TopStart),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "HOT",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = festival.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = festival.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = festival.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}
