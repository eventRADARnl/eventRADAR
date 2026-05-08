package com.example.eventradar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: FestivalViewModel,
    onMyEventsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val uiState = viewModel.uiState
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profile Header
        Surface(
            modifier = Modifier.size(100.dp)
                .clip(CircleShape)
                .clickable { 
                    if (uiState.currentUser != null) showEditDialog = true
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (uiState.currentUser?.avatarUrl != null) {
                    AsyncImage(
                        model = uiState.currentUser.avatarUrl,
                        contentDescription = "Profilbild",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (uiState.currentUser != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(28.dp),
                        shape = CircleShape,
                        color = Color(0xFF044474)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Profilbild ändern",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp).size(16.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = uiState.currentUser?.username ?: "Gast-Nutzer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = uiState.currentUser?.email ?: "Melde dich an, um mehr zu sehen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.currentUser != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.currentUser.age != null) {
                    Text(
                        text = "${uiState.currentUser.age} Jahre",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF044474)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                TextButton(onClick = { showEditDialog = true }) {
                    Text("Profil bearbeiten")
                }
            }
            
            if (!uiState.currentUser.bio.isNullOrBlank()) {
                Text(
                    text = uiState.currentUser.bio,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu Items
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column {
                AccountMenuItem(
                    title = "Meine Events",
                    icon = Icons.Default.DateRange,
                    onClick = onMyEventsClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AccountMenuItem(
                    title = "Einstellungen",
                    icon = Icons.Default.Settings,
                    onClick = onSettingsClick
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (uiState.currentUser == null) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF044474))
            ) {
                Text("Anmelden / Registrieren")
            }
        } else {
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Abmelden")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showEditDialog && uiState.currentUser != null) {
        EditProfileDialog(
            currentBio = uiState.currentUser.bio ?: "",
            currentAge = uiState.currentUser.age,
            onDismiss = { showEditDialog = false },
            onSave = { bio, age ->
                viewModel.updateCurrentUserProfile(bio, age)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    currentBio: String,
    currentAge: Int?,
    onDismiss: () -> Unit,
    onSave: (String, Int?) -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }
    var ageStr by remember { mutableStateOf(currentAge?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil bearbeiten") },
        text = {
            Column {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { if (it.length <= 2) ageStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Alter") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(bio, ageStr.toIntOrNull()) }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun AccountMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
