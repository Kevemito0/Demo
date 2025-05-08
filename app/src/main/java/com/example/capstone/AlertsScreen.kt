package com.example.capstone

import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AlertsScreen(paddingValues: PaddingValues, navController: NavController) {
    val firestore = Firebase.firestore
    var alerts by remember { mutableStateOf<List<SensorAlert>>(emptyList()) }

    Log.d("AlertsScreen", "Sayfa açıldı")
    LaunchedEffect(Unit) {
        firestore.collection("sensorAlerts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val alertList = snapshot.documents.mapNotNull { doc ->
                    val doorLock = doc.getBoolean("doorLock")
                    // val gas = doc.getBoolean("gas")
                    val motion = doc.getBoolean("motion")
                    val temperature = doc.getLong("temperature")?.toInt()
                    val timestamp = doc.getDate("timestamp")


                    val gasValue = doc.get("gas")
                    val gas = when (gasValue) {
                        is Boolean -> gasValue
                        is Long -> gasValue != 0L
                        is Int -> gasValue != 0
                        else -> false
                    }

                    if (gas != null && motion != null && doorLock != null && temperature != null && timestamp != null) {
                        SensorAlert(doorLock, gas, motion, temperature, timestamp)
                    } else null
                }
                alerts = alertList
            }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Firestore'dan tüm alert'leri sil
                    firestore.collection("Test")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            snapshot.documents.forEach { it.reference.delete() }
                            alerts = emptyList() // UI'dan da silinsin
                        }
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
                        .wrapContentHeight()//.height(120.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {


                        val formatter = SimpleDateFormat("HH:mm   dd/MM/yyyy", Locale.getDefault())
                        val formattedTime = formatter.format(alert.timestamp)
                        Text(
                            text = formattedTime,
                            fontSize = 12.sp
                        )


                        if (alert.gas == true) {
                            Text(
                                text = "Sensors detected a gas leak in the house!! ",
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }


                        if (alert.motion == true) {
                            Text(
                                text = "Sensors detected unauthorized movement in the house!! ",
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                        if (alert.doorLock == true) {
                            Text(
                                text = "The door was unlocked. ",
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                        if (alert.temperature != 0) {
                            Text(
                                text = "The temperature in the house has risen above standard!! ",
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }


                    }
                }
            }
        }
    }
}
