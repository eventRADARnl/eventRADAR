package com.example.eventradar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    viewModel: FestivalViewModel,
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState
    val profile = uiState.viewingProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF044474),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(120.dp),
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
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name and Age
                Text(
                    text = "${profile.username ?: "Anonym"}${if (profile.age != null) ", ${profile.age}" else ""}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(count = uiState.viewerFollowersCount, label = "Follower")
                    ProfileStatItem(count = uiState.viewerFollowingCount, label = "Folgt")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Follow Button
                if (uiState.currentUser != null && uiState.currentUser.id != profile.id) {
                    Button(
                        onClick = { viewModel.toggleFollowUser(profile.id) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (uiState.isFollowingViewingUser) 
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            else ButtonDefaults.buttonColors(containerColor = Color(0xFF044474))
                    ) {
                        Text(if (uiState.isFollowingViewingUser) "Entfolgen" else "Folgen")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bio Section
                if (!profile.bio.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(profile.bio, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
