package com.example.capstone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Room
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CapstoneTheme {
                TopAndBottomBars()
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
                    containerColor = colorResource(R.color.topBarColor),
                    titleContentColor = androidx.compose.ui.graphics.Color.White
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
                    RoomScreen(navController, paddingValues) // NavController parametre olarak gönderiliyor
                }
                composable("device") {
                    selectedIndex = 99
                    DeviceScreen(paddingValues)
                }
                composable("settings") {
                    Kerem(paddingValues) // Settings sayfası
                }
                composable("profile") {
                    Gaser(paddingValues) // Profile sayfası
                }
            }
        }

    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CapstoneTheme {
        TopAndBottomBars()
    }
}