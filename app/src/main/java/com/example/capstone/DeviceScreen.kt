package com.example.capstone

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(paddingValues: PaddingValues, roomName: String, navController: NavController) {
//
    val firestore = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser


    var familyId by remember { mutableStateOf<String?>(null) }
    var devices by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            firestore.collection("UsersTest").document(uid).get()
                .addOnSuccessListener { userDoc ->
                    val fid = userDoc.getString("familyId")
                    familyId = fid

                    if (fid != null) {
                        firestore.collection("Rooms")
                            .document(fid)
                            .collection(roomName)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val deviceList = querySnapshot.documents.mapNotNull { doc ->
                                    doc.getString("Device")
                                }
                                devices = deviceList
                                Log.d("DeviceScreen", "Devices: $devices")
                            }
                    }
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        items(devices) { device ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = device,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // "+" Butonu
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        Log.d("DeviceScreen", "Artı Butonuna tıklandı")
                        navController.navigate("room")
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp)
                ) {
                    Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DeviceScreenPreview() {
    CapstoneTheme {
        DeviceScreen(
            paddingValues = PaddingValues(),
            roomName = "Livingroom",
            navController = rememberNavController()
        )
    }
}