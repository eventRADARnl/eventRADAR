package com.example.eventradar.ui

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.eventradar.R
import com.example.eventradar.model.Festival
import com.example.eventradar.model.LatLng
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalListScreen(
    viewModel: FestivalViewModel,
    navController: NavController,
    onFestivalClick: (Festival) -> Unit
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            Column {
                Surface(
                    color = Color(0xFF044474),
                    contentColor = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.events),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                placeholder = { 
                                    Text(
                                        "Suchen...", 
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    ) 
                                },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Search, 
                                        contentDescription = null, 
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    ) 
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(26.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                                    disabledContainerColor = Color.White.copy(alpha = 0.15f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        FilterSection(viewModel)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Sorting and Summary Row (outside the blue surface for better contrast)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.events_found, uiState.filteredFestivals.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.sort_by), style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = !uiState.sortByDistance,
                            onClick = { viewModel.onSortOrderChanged(false) },
                            label = { Text(stringResource(R.string.date), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(28.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = uiState.sortByDistance,
                            onClick = { viewModel.onSortOrderChanged(true) },
                            label = { Text(stringResource(R.string.distance), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.userSearchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Nutzer gefunden",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF044474)
                    )
                }
                items(uiState.userSearchResults) { profile ->
                    ListItem(
                        headlineContent = { Text(profile.username ?: "Anonym") },
                        supportingContent = { if (profile.bio != null) Text(profile.bio, maxLines = 1) },
                        leadingContent = {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (profile.avatar_url != null) {
                                        AsyncImage(
                                            model = profile.avatar_url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.clickable { 
                            navController.navigate("profile/${profile.id}")
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Events",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF044474)
                    )
                }
            }

            items(uiState.filteredFestivals) { festival ->
                val isFavorite = uiState.favoriteIds.contains(festival.id)
                FestivalListItem(
                    festival = festival,
                    userLocation = uiState.userLocation,
                    isFavorite = isFavorite,
                    onFavoriteToggle = { 
                        if (uiState.currentUser != null) {
                            viewModel.toggleFavorite(festival.id)
                        } else {
                            navController.navigate("auth")
                        }
                    },
                    isAttending = uiState.attendanceIds.contains(festival.id),
                    hotCount = uiState.festivalHotCounts[festival.id] ?: 0,
                    onClick = { onFestivalClick(festival) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(viewModel: FestivalViewModel) {
    val uiState = viewModel.uiState
    val genres = listOf("Alle", "Hardstyle", "Hardstyle Classics", "Rawstyle", "Rawstyle Classics", "Hardcore", "Hardcore Classics", "Hard Techno")
    val allLabel = stringResource(R.string.all)

    Column {
        // Secondary Filters (Genre, Type, Location, Age)
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group: Genre (Dropdown)
            FilterGroup(
                label = stringResource(R.string.filter_genre),
                selectedLabel = uiState.filterGenre ?: allLabel,
                options = genres.map { if (it == "Alle") allLabel else it },
                onOptionSelected = { selected ->
                    viewModel.onGenreFilterChanged(if (selected == allLabel) null else selected)
                }
            )

            // Group: Type
            FilterGroup(
                label = stringResource(R.string.filter_type),
                selectedLabel = when(uiState.filterWeekend) {
                    true -> stringResource(R.string.weekend)
                    false -> stringResource(R.string.day)
                    else -> stringResource(R.string.all)
                },
                options = listOf(stringResource(R.string.all), stringResource(R.string.weekend), stringResource(R.string.day)),
                onOptionSelected = { 
                    val value = when(it) {
                        "Weekend" -> true
                        "Tag", "Day" -> false
                        else -> null
                    }
                    viewModel.onTypeFilterChanged(value)
                }
            )

            // Group: Location
            FilterGroup(
                label = stringResource(R.string.filter_location),
                selectedLabel = when(uiState.filterOutdoor) {
                    true -> stringResource(R.string.outdoor)
                    false -> stringResource(R.string.indoor)
                    else -> stringResource(R.string.all)
                },
                options = listOf(stringResource(R.string.all), stringResource(R.string.outdoor), stringResource(R.string.indoor)),
                onOptionSelected = { 
                    val value = when(it) {
                        "Outdoor" -> true
                        "Indoor" -> false
                        else -> null
                    }
                    viewModel.onOutdoorFilterChanged(value)
                }
            )

            // Group: Age
            FilterGroup(
                label = stringResource(R.string.filter_age),
                selectedLabel = when(uiState.filterMinAge) {
                    16 -> "16+"
                    18 -> "18+"
                    else -> stringResource(R.string.all)
                },
                options = listOf(stringResource(R.string.all), "16+", "18+"),
                onOptionSelected = { 
                    val value = when(it) {
                        "16+" -> 16
                        "18+" -> 18
                        else -> null
                    }
                    viewModel.onMinAgeFilterChanged(value)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterGroup(
    label: String,
    selectedLabel: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selectedLabel != stringResource(R.string.all),
            onClick = { expanded = true },
            label = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$label $selectedLabel", style = MaterialTheme.typography.labelSmall)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                labelColor = Color.White.copy(alpha = 0.7f),
                selectedLabelColor = Color.White,
                iconColor = Color.White.copy(alpha = 0.7f),
                selectedLeadingIconColor = Color.White
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedLabel != stringResource(R.string.all),
                borderColor = Color.White.copy(alpha = 0.3f),
                selectedBorderColor = Color.White,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.dp
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FestivalListItem(
    festival: Festival,
    userLocation: LatLng?,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    isAttending: Boolean = false,
    hotCount: Int = 0,
    onClick: () -> Unit
) {
    val distance = remember(userLocation, festival.position) {
        if (userLocation != null) {
            val r = 6371.0
            val dLat = (festival.position.latitude - userLocation.latitude) * PI / 180.0
            val dLon = (festival.position.longitude - userLocation.longitude) * PI / 180.0
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(userLocation.latitude * PI / 180.0) * cos(festival.position.latitude * PI / 180.0) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            (r * c).toInt().toString()
        } else {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            AsyncImage(
                model = festival.imageUrl.ifEmpty { festival.listImageUrl },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.LightGray),
                error = ColorPainter(Color.Gray)
            )
            
            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = festival.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = festival.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = " • ",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = festival.locationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1
                    )
                }
                if (distance != null) {
                    Text(
                        text = stringResource(R.string.km_away, distance),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Status Badges (Top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Genre Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = festival.genre,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    if (hotCount >= 2) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "HOT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Favorite Toggle
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Attendance Indicator (Bottom End)
            if (isAttending) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dabei",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
