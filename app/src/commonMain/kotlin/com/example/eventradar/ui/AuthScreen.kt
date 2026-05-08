package com.example.eventradar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: FestivalViewModel,
    onBackClick: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var keepLoggedIn by remember { mutableStateOf(true) }
    
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }
    
    var registrationSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiState = viewModel.uiState

    // If user is logged in, move back or to success
    LaunchedEffect(uiState.currentUser) {
        if (uiState.currentUser != null) {
            onAuthSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "Anmelden" else "Registrieren") },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isLoginMode) "Willkommen zurück!" else "Account erstellen",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF044474)
            )

            Text(
                text = if (isLoginMode) "Melde dich an, um fortzufahren" else "Werde Teil der Harder Styles Community",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (registrationSuccess) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Registrierung erfolgreich! Bitte bestätige deine E-Mail in deinem Postfach, bevor du dich einloggst.",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (!isLoginMode) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Benutzername") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Mail") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Passwort") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Keep me logged in checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { keepLoggedIn = !keepLoggedIn },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = keepLoggedIn,
                    onCheckedChange = { keepLoggedIn = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF044474))
                )
                Text(
                    text = "Angemeldet bleiben",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoginMode) {
                TextButton(
                    onClick = { 
                        resetEmail = email
                        showResetPasswordDialog = true 
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Passwort vergessen?", color = Color(0xFF044474))
                }
            }

            Button(
                onClick = {
                    errorMessage = null
                    val scope = kotlinx.coroutines.MainScope()
                    scope.launch {
                        try {
                            if (isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.register(username, email, password)
                                registrationSuccess = true
                            }
                        } catch (e: Exception) {
                            errorMessage = when {
                                e.message?.contains("Email not confirmed") == true -> "Bitte bestätige erst deine E-Mail-Adresse."
                                e.message?.contains("Invalid login credentials") == true -> "E-Mail oder Passwort falsch."
                                else -> e.message ?: "Ein Fehler ist aufgetreten."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF044474))
            ) {
                Text(if (isLoginMode) "Anmelden" else "Registrieren")
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(
                    text = if (isLoginMode) "Noch kein Konto? Registrieren" else "Bereits ein Konto? Anmelden",
                    color = Color(0xFF044474)
                )
            }
        }

        if (showResetPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showResetPasswordDialog = false },
                title = { Text("Passwort vergessen?") },
                text = {
                    Column {
                        Text("Gib deine E-Mail-Adresse ein. Wir senden dir einen Link, um dein Passwort zurückzusetzen.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("E-Mail") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (resetSent) {
                            Text(
                                text = "E-Mail wurde gesendet! Bitte prüfe dein Postfach.",
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val scope = kotlinx.coroutines.MainScope()
                            scope.launch {
                                try {
                                    viewModel.resetPassword(resetEmail)
                                    resetSent = true
                                    // Dialog schließt nach 3 Sekunden automatisch
                                    kotlinx.coroutines.delay(3000)
                                    showResetPasswordDialog = false
                                    resetSent = false
                                } catch (e: Exception) {
                                    errorMessage = "Fehler: ${e.message}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF044474))
                    ) {
                        Text("Link senden")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetPasswordDialog = false }) {
                        Text("Abbrechen")
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}
