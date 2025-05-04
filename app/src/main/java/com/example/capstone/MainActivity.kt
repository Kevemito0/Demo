package com.example.capstone

import Gaser
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTesting
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CapstoneTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        AuthScreens(PaddingValues(), navController)
                    }
                    composable("main") {
                        TopAndBottomBars()
                    }
                    composable("room") {
                        RoomScreen(navController = navController, paddingValues = PaddingValues())
                    }
                    /*composable("device/{roomName}") { backStackEntry ->
                        val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
                        DeviceScreen(paddingValues = paddingValues,roomName, navController = navController)
                    }*/
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAndBottomBars() {

    val navController = rememberNavController()

    val bottomNavItem: List<BottomNavItem> = listOf(
        BottomNavItem(
            title = "Rooms",
            selectedIcon = Icons.Filled.MeetingRoom,
            unselectedIcon = Icons.Outlined.MeetingRoom
        ),
        BottomNavItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        ),
        BottomNavItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    var selectedIndex by remember {
        mutableStateOf(0)
    }


    Scaffold(
        topBar = {

            TopAppBar(title = {
                Text(text = "HOMSEC")
            },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        Log.d("Message", "Clicked")
                    }) {
                        Icon(imageVector = Icons.Filled.Message, contentDescription = "")
                    }
                }
            )


        },
        bottomBar = {
            NavigationBar {
                bottomNavItem.forEachIndexed { index, bottomNavItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            when (selectedIndex) {
                                0 -> navController.navigate("room")
                                1 -> navController.navigate("settings")
                                2 -> navController.navigate("profile")
                            }
                        },
                        icon = {
                            if (selectedIndex == index) {
                                Icon(
                                    imageVector = bottomNavItem.selectedIcon,
                                    contentDescription = ""
                                )
                            } else {
                                Icon(
                                    imageVector = bottomNavItem.unselectedIcon,
                                    contentDescription = ""
                                )
                            }
                        },
                        label = {
                            Text(text = bottomNavItem.title)
                        }
                    )
                }
            }
        },
        content = { paddingValues ->
            NavHost(navController = navController, startDestination = "room") {
                composable("room") {
                    RoomScreen(
                        navController,
                        paddingValues
                    ) // NavController parametre olarak gönderiliyor
                }
               /* composable("device") {
                    selectedIndex = 99
                    DeviceScreen(paddingValues)
                }*/

                composable("device/{roomName}") { backStackEntry ->
                    val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
                    DeviceScreen(paddingValues = paddingValues,roomName, navController = navController)
                }

                composable("settings") {

                 /*   val user = hashMapOf(
                        "first" to "Kerem",
                        "last" to "Cakilli",
                        "born" to 2001
                    )

// Add a new document with a generated ID
                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }*/

//                    Kerem(paddingValues) // Settings sayfası
                }
                composable("profile") {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        Log.d("FCM", "Token: $token")
                    }
                    //addData()
                    Gaser(paddingValues) // Profile sayfası
                }
            }
        }

    )
}


fun addData(){
    val db = Firebase.firestore

    val user = hashMapOf(
        "first" to "Hasan",
        "middle" to "Furkan",
        "last" to "Gaser",
        "born" to 2002
    )

// Add a new document with a generated ID
    db.collection("users")
        .add(user)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ")//${documentReference.id}
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CapstoneTheme {
        TopAndBottomBars()
    }
}