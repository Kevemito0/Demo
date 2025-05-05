package com.example.capstone

import android.webkit.WebSettings.TextSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun AlertsScreen(paddingValues: PaddingValues, navController: NavController) {
    val firestore = Firebase.firestore
    var alerts by remember { mutableStateOf<List<SensorAlert>>(emptyList()) }

    LaunchedEffect(Unit) {
        firestore.collection("sensorAlerts")
            .get()
            .addOnSuccessListener { snapshot ->
                val alertList = snapshot.documents.mapNotNull { doc ->
                    val gas = doc.getBoolean("gas")
                    val motion = doc.getBoolean("motion")
                    val temperature = doc.getLong("temperature")?.toInt()
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()
                    val notified = doc.getBoolean("notified")

                    if (gas != null && motion != null && temperature != null && timestamp != null && notified != null) {
                        SensorAlert(gas, motion, temperature, timestamp.toString(), notified)
                    } else null
                }
                alerts = alertList
            }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // val collectionRef = firestore.collection("sensorAlerts")
                    //
                    //    collectionRef.get()
                    //        .addOnSuccessListener { snapshot ->
                    //            for (document in snapshot.documents) {
                    //                document.reference.delete()
                    //            }
                    //        }
                },
                modifier = Modifier
                    .padding(end = 32.dp, bottom = 72.dp) // Profile butonun biraz üstüne gelsin

            ) {
                Text(
                    text = "Clear",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

        ) {
            items(alerts) { alert ->
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
                            text = "Gas: ${alert.gas}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = alert.timestamp,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
