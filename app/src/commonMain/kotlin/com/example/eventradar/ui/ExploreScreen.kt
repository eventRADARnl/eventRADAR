package com.example.eventradar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.eventradar.model.Festival
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: FestivalViewModel,
    onFestivalClick: (Festival) -> Unit,
    onGenreClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    adBanner: @Composable () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    // Dynamic rotation of featured event
    var featuredIndex by remember { mutableIntStateOf(0) }
    val hotFestivals = remember(uiState.allFestivals) {
        // Prefer festivals with higher attendance, but shuffle for variety
        uiState.allFestivals.filter { (uiState.festivalHotCounts[it.id] ?: 0) >= 1 }
            .shuffled()
            .ifEmpty { uiState.allFestivals.shuffled() }
    }

    LaunchedEffect(hotFestivals) {
        while (hotFestivals.isNotEmpty()) {
            delay(6000)
            featuredIndex = (featuredIndex + 1) % hotFestivals.size
        }
    }

    val featuredFestival = hotFestivals.getOrNull(featuredIndex)
    
    // Genres updated as per user request
    val popularGenres = listOf(
        "Hardstyle", "Hardstyle Classics", 
        "Rawstyle", "Rawstyle Classics", 
        "Hardcore", "Hardcore Classics", 
        "Hard Techno", "EDM"
    )
    
    // Upcoming events (sorted by date, excluding featured)
    val upcomingFestivals = uiState.allFestivals
        .filter { it.id != featuredFestival?.id }
        .take(5) // Just take some for now

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Modern Header with #044474 Background
        Surface(
            color = Color(0xFF044474),
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Entdecken",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Finde dein nächstes Event",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ad Banner
        adBanner()

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Event Card
        featuredFestival?.let { festival ->
            FeaturedEventCard(
                festival = festival,
                isHot = (uiState.festivalHotCounts[festival.id] ?: 0) >= 2,
                onClick = { onFestivalClick(festival) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Popular Genres Section
        Text(
            text = "Nach Genre",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            popularGenres.forEach { genre ->
                Surface(
                    onClick = { onGenreClick(genre) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Coming Soon Section
        Text(
            text = "Kommende Events",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.height(240.dp)
        ) {
            items(upcomingFestivals) { festival ->
                SmallEventCard(
                    festival = festival,
                    onClick = { onFestivalClick(festival) }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        // Beta Version Text
        Text(
            text = "BETA V.0.0.0.1",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp)
        )
    }
}

@Composable
fun FeaturedEventCard(
    festival: Festival,
    isHot: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = festival.imageUrl.ifEmpty { festival.listImageUrl },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.LightGray)
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 400f
                        )
                    )
            )

            if (isHot) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    color = Color(0xFFD32F2F), // Strong red for HOT
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "HOT",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = festival.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = festival.date,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SmallEventCard(
    festival: Festival,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = festival.listImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.LightGray)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = festival.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = festival.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
