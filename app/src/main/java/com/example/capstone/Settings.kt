package com.example.capstone

import android.provider.ContactsContract.Profile
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.auth.FirebaseAuth

fun logoutUser(onLoggedOut: () -> Unit) {
    FirebaseAuth.getInstance().signOut()
    onLoggedOut()
}

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    outerNavController: NavController
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    // Profile
                    ListItem(
                        modifier = Modifier
                            .clickable { navController.navigate("profileEdit") }
                            .padding(horizontal = 16.dp),
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        headlineContent = { Text("Profile") },
                        supportingContent = { Text("Edit your profile information") }
                    )

                    Divider()

                    // Notifications toggle
                    ListItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        },
                        headlineContent = {
                            Text("Notifications")
                        },
                        trailingContent = {
                           MySwitch()
                        }
                    )

                    Divider()

                    // About
                    ListItem(
                        modifier = Modifier
                            .clickable { /* navController.navigate("about") */ }
                            .padding(horizontal = 16.dp),
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About"
                            )
                        },
                        headlineContent = {
                            Text("About")
                        },
                        supportingContent = {
                            Text("Version 1.0.0")
                        }
                    )
                }
            }
        }

        // Logout button
        Button(
            onClick = {
                logoutUser {
                    outerNavController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Logout", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun MySwitch() {
    var isChecked by remember { mutableStateOf(true) }

    Switch(
        checked         = isChecked,
        onCheckedChange = { isChecked = it },
        thumbContent    = {
            if (isChecked) {
                Icon(
                    imageVector     = Icons.Default.Check,
                    contentDescription = "Checked",
                    modifier        = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        },
        // optional: you can tweak the track/thumb colors too
        colors = SwitchDefaults.colors(
            checkedTrackColor   = Color(0xFF198817),
            uncheckedTrackColor = Color(0xFF424242),
            checkedThumbColor   = Color(0xFF10B90E),
            uncheckedThumbColor = Color(0x8D000000)
        )
    )
}