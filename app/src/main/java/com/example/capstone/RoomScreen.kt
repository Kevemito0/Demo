package com.example.capstone

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(navController: NavController, paddingValues: PaddingValues) {


    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    val firestore = Firebase.firestore

    var familyId by remember { mutableStateOf<String?>(null) }
    var rooms by remember { mutableStateOf<List<RoomItem>>(emptyList()) }


    // Firestore'dan familyId'yi al

    LaunchedEffect(Unit) {
        userId?.let {
            firestore.collection("UsersTest")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    familyId = document.getString("familyId")
                    Log.d("RoomScreen", "FamilyID: $familyId")

                    familyId?.let { fid ->
                        firestore.collection("Rooms")
                            .document(fid)
                            .get()
                            .addOnSuccessListener { roomDoc ->
                                val fields = roomDoc.data
                                if (fields != null) {
                                    val roomList = fields.mapNotNull { entry ->
                                        val name = entry.value as? String
                                        if (name != null) RoomItem(name, entry.key) else null
                                    }
                                    rooms = roomList
                                    Log.d("RoomScreen", "Rooms: $rooms")
                                }
                            }
                            .addOnFailureListener {
                                Log.e("RoomScreen", "Rooms belgesi alınamadı", it)
                            }
                    }
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
    {
        items(rooms) { room ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("device/${room.name}")
                    },
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = room.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val buttons = listOf("Open the Door", "Close Alarm", "Open Gas Valve", "Close Gas Valve")

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(2f) // kare şeklinde yapar
                        .padding(end = 8.dp)
                        .clickable {
                            // BURAYA FONKSİYON GELECEK
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = buttons[0],
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(2f)
                        .padding(start = 8.dp)
                        .clickable {
                            // BURAYA FONKSİYON GELECEK
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = buttons[1],
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }



            // "+" Butonu
            /*  item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.navigate("device/Livingroom")
                        Log.d("RoomScreen", "Artı Butonuna tıklandı")
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp)
                ) {
                    Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }*/
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(2f) // kare şeklinde yapar
                        .padding(end = 8.dp)
                        .clickable {
                            // BURAYA FONKSİYON GELECEK
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = buttons[2],
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(2f)
                        .padding(start = 8.dp)
                        .clickable {
                            // BURAYA FONKSİYON GELECEK
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = buttons[3],
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }



            // "+" Butonu
            /*  item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.navigate("device/Livingroom")
                        Log.d("RoomScreen", "Artı Butonuna tıklandı")
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp)
                ) {
                    Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }*/
        }

    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoomScreenPreview() {
    CapstoneTheme {
        RoomScreen(
            navController = rememberNavController(),
            paddingValues = PaddingValues(0.dp)
        )
    }
}
