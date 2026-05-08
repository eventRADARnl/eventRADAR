package com.example.eventradar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: FestivalViewModel,
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text("Einstellungen", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Only show back button if not in a tab context (we can use an empty callback to hide it)
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SettingsCategory(title = "Design")
            
            SettingsToggleItem(
                title = "Dunkles Design",
                subtitle = "Aktiviere den Dark Mode",
                icon = Icons.Default.Settings,
                checked = uiState.isDarkMode == true,
                onCheckedChange = { viewModel.onDarkModeChanged(it) }
            )

            HorizontalDivider()
            
            SettingsCategory(title = "Plattform-Spezifisch")
            
            // Placeholder for platform-specific settings
            // In a real KMP app, this would use expect/actual or check platform info
            val platformName = "Android" // Default for now
            SettingsItem(
                title = "Plattform: $platformName",
                subtitle = "Optimiert für dein Gerät",
                icon = Icons.Default.Info,
                onClick = { /* Info */ }
            )

            HorizontalDivider()
            
            SettingsCategory(title = "Benachrichtigungen")
            SettingsItem(
                title = "Push-Benachrichtigungen",
                subtitle = "Verwalte deine Benachrichtigungen",
                icon = Icons.Default.Notifications,
                onClick = { /* TODO */ }
            )

            HorizontalDivider()

            SettingsCategory(title = "Account")
            if (uiState.currentUser != null) {
                SettingsItem(
                    title = "Abmelden",
                    subtitle = "Als ${uiState.currentUser.username} angemeldet",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { 
                        viewModel.logout()
                    }
                )
            } else {
                SettingsItem(
                    title = "Anmelden / Registrieren",
                    subtitle = "Erstelle ein Konto, um Daten zu synchronisieren",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = { /* TODO: Navigate to Login */ }
                )
            }

            HorizontalDivider()

            SettingsCategory(title = "Info")
            SettingsItem(
                title = "Über eventRADAR",
                subtitle = "Version 1.0.2 (Modern UI Update)",
                icon = Icons.Default.Info,
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (textColor == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.onSurfaceVariant else textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = textColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
