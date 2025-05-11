package com.example.capstone

import android.content.res.Configuration
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.waterfallPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.Date

fun logoutUser(onLoggedOut: () -> Unit) {
    FirebaseAuth.getInstance().signOut()
    onLoggedOut()
}

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues, navController: NavController, outerNavController: NavController
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var ListItemColors = ListItemColors(
        containerColor = Color(0x00000000),
        leadingIconColor = MaterialTheme.colorScheme.onBackground,
        disabledHeadlineColor = MaterialTheme.colorScheme.onSecondary,
        supportingTextColor = MaterialTheme.colorScheme.onBackground,
        trailingIconColor = MaterialTheme.colorScheme.onBackground,
        headlineColor = MaterialTheme.colorScheme.onBackground,
        overlineColor = MaterialTheme.colorScheme.onBackground,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledTrailingIconColor = MaterialTheme.colorScheme.secondary
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
//                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.secondary)
                ) {
                    // Profile
                    ListItem(modifier = Modifier
                        .clickable { navController.navigate("profileEdit") }
                        .padding(horizontal = 16.dp),
                        leadingContent = {
                            Icon(
                                Icons.Default.Person, contentDescription = "Profile"
                            )
                        },
                        headlineContent = { Text("Profile") },
                        supportingContent = { Text("Edit your profile information") },
                        colors = ListItemColors
                    )

                    Divider(color = Color(0xFA313131))

                    // Notifications toggle
                    ListItem(modifier = Modifier.padding(horizontal = 16.dp), leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }, headlineContent = {
                        Text("Notifications")
                    }, trailingContent = {
                        MySwitch()
                    }, colors = ListItemColors
                    )

                    Divider(
                        color = Color(0xFA313131)
                    )

                    // About
                    ListItem(modifier = Modifier
                        .clickable { /* navController.navigate("about") */ }
                        .padding(horizontal = 16.dp), leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info, contentDescription = "About"
                        )
                    }, headlineContent = {
                        Text("About")
                    }, supportingContent = {
                        Text("Version 1.0.0")
                    }, colors = ListItemColors
                    )
                }
            }
        }


        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val firestore = Firebase.firestore

        var familyId by remember { mutableStateOf<String?>(null) }
        var inFamily by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            userId?.let {
                firestore.collection("UsersTest").document(it).get()
                    .addOnSuccessListener { document ->
                        familyId = document.getString("familyId")
                        inFamily = document.getBoolean("inFamily") == true
                        Log.d("RoomScreen", "FamilyID: $familyId")
                    }
            }
        }


        var familyName by remember { mutableStateOf("") }
        if (!inFamily) {
            OutlinedTextField(value = familyName,
                onValueChange = { familyName = it },
                label = { Text("Enter Your Family Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Button(
                onClick = {
                    //Rooms Ekranını ayarlıyor
                    familyId?.let { fid ->
                        val roomsMap = mapOf(
                            "0" to "Bedroom",
                            "1" to "Livingroom",
                            "2" to "Balcony",
                            "3" to "Diningroom"
                        )

                        firestore.collection("Rooms").document(fid).set(roomsMap)
                            .addOnSuccessListener {
                                Log.d(
                                    "SettingsScreen",
                                    "Room document created successfully for familyId: $fid"
                                )
                            }.addOnFailureListener { e ->
                                Log.e("SettingsScreen", "Error creating room document", e)
                            }

                        val roomsRef = firestore.collection("Rooms").document(fid)

                        val roomDevicesMap = mapOf(
                            "Bedroom" to listOf("Motion Sensor"),
                            "Livingroom" to listOf("Gas Sensor", "Heat Sensor")
                        )

                        roomDevicesMap.forEach { (roomName, devices) ->
                            val roomCollection = roomsRef.collection(roomName)

                            devices.forEach { deviceName ->
                                val deviceData = hashMapOf("Device" to deviceName)
                                roomCollection.add(deviceData)
                            }
                        }


                        //Families Ekranını Ayarlıyor
                        val currentTime = Date()

                        userId?.let { uid ->
                            firestore.collection("UsersTest").document(uid).get()
                                .addOnSuccessListener { document ->
                                    val userIdFromDoc = document.getString("userId") ?: ""

                                    val userInfo = mapOf(
                                        "createdAt" to currentTime,
                                        "familyName" to familyName,
                                        "ownerId" to userIdFromDoc
                                    )

                                    firestore.collection("Families").document(fid).set(userInfo)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "SettingsScreen",
                                                "Family document created successfully for familyId: $fid"
                                            )
                                        }.addOnFailureListener { e ->
                                            Log.e(
                                                "SettingsScreen",
                                                "Error creating family document",
                                                e
                                            )
                                        }


                                }

                            userId?.let { uid ->
                                firestore.collection("UsersTest").document(uid).get()
                                    .addOnSuccessListener { document ->
                                        val email = document.getString("E-Mail") ?: ""
                                        val name = document.getString("User Name") ?: ""
                                        val userIdFromDoc = document.getString("userId") ?: ""

                                        val membersInfos = mapOf(
                                            "email" to email,
                                            "joinedAt" to currentTime,
                                            "name" to name,
                                            "role" to "Admin",
                                            "userId" to userIdFromDoc
                                        )

                                        firestore.collection("UsersTest").document(userId)
                                            .update("inFamily", true)


                                        firestore.collection("Families").document(fid)
                                            .collection("members").document(uid).set(membersInfos)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "Firestore",
                                                    "Kullanıcı üyeler koleksiyonuna başarıyla eklendi."
                                                )
                                            }.addOnFailureListener { e ->
                                                Log.e("Firestore", "Üye eklenirken hata oluştu", e)
                                            }
                                    }.addOnFailureListener { e ->
                                        Log.e("UserInfo", "Kullanıcı bilgileri alınamadı", e)
                                    }
                            }
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
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create Family", style = MaterialTheme.typography.titleMedium)
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
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun MySwitch() {
    var isChecked by remember { mutableStateOf(true) }

    Switch(checked = isChecked, onCheckedChange = { isChecked = it }, thumbContent = {
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                modifier = Modifier.size(SwitchDefaults.IconSize)
            )
        }
    },
        // optional: you can tweak the track/thumb colors too
        colors = SwitchDefaults.colors(
            checkedTrackColor = Color(0xFF198817),
            uncheckedTrackColor = Color(0xFF424242),
            checkedThumbColor = Color(0xFF10B90E),
            uncheckedThumbColor = Color(0x8D000000)
        )
    )
}

@Preview(
    name = "Settings • Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun PreviewSettingsLight() {
    CapstoneTheme(dynamicColor = false) {
        SettingsScreen(
            paddingValues = PaddingValues(),
            navController = rememberNavController(),
            outerNavController = rememberNavController()
        )
    }
}

@Preview(
    name = "Settings • Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewSettingsDark() {
    CapstoneTheme(dynamicColor = false) {
        SettingsScreen(
            paddingValues = PaddingValues(),
            navController = rememberNavController(),
            outerNavController = rememberNavController()
        )
    }
}