package com.example.capstone

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("AuthListener", "Kullanıcı giriş yaptı: ${user.uid}")
                // Kullanıcı giriş yaptı, burada ek bir işlem yapabiliriz.
            } else {
                Log.d("AuthListener", "Kullanıcı çıkış yaptı!")
                // Kullanıcı çıkış yaptıysa direkt auth ekranına git
            }
        }
        auth.addAuthStateListener(authStateListener)

        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().subscribeToTopic("alerts")
            .addOnSuccessListener {
                Log.d("FCM", "alerts konusuna abone olundu")
            }
            .addOnFailureListener {
                Log.e("FCM", "Abonelik başarısız: ${it.message}")
            }
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                auth.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        if (navController.currentDestination?.route != "main") {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    } else {
                        if (navController.currentDestination?.route != "auth") {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }
                }
            }
            CapstoneTheme (dynamicColor = false) {
                val startDestination = if (auth.currentUser != null) "main" else "auth"
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("auth") {
                        AuthScreens(PaddingValues(), navController)
                    }
                    composable("main") {
                        TopAndBottomBars(navController)
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authStateListener)
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAndBottomBars(outerNavController: NavHostController) {

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
            title = "Family",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = {
                        Log.d("Message", "Clicked")
                        navController.navigate("alertScreen")
                    }) {
                        Icon(imageVector = Icons.Filled.AddAlert, contentDescription = "")
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
                                    contentDescription = "",
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
                        },
                        colors = NavigationBarItemColors(
                            selectedIndicatorColor = MaterialTheme.colorScheme.secondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledIconColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer

                        )
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
                composable("device/{roomName}") { backStackEntry ->
                    val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
                    DeviceScreen(
                        paddingValues = paddingValues,
                        roomName = roomName,
                        navController = navController
                    )
                }
//
                composable("alertScreen") {
                    AlertsScreen(paddingValues, navController)
                }
                composable("settings") {
                    SettingsScreen(paddingValues,
                        navController,
                        outerNavController)
//                    Kerem(paddingValues) // Settings sayfası
                }
                composable("profile") {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        Log.d("FCM", "Token: $token")
                    }
                    //addData()
                    Gaser(paddingValues, outerNavController) // Profile sayfası
                }
                composable("profileEdit") {
                    ProfileEditScreen(paddingValues, navController)
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
        val navController = rememberNavController()
        TopAndBottomBars(navController)
    }
}