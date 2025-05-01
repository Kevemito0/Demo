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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    // Firestore'dan familyId'yi al
    LaunchedEffect(Unit) {
        userId?.let {
            firestore.collection("UsersTest")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    familyId = document.getString("familyId")
                    Log.d("RoomScreen", "FamilyID: $familyId")
                }
                .addOnFailureListener {
                    Log.e("RoomScreen", "FamilyId alınamadı", it)
                }
        }
    }



    val number = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        items(number) { number ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("device")
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Item :${number}")
                }
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun RoomScreenPreview() {
    CapstoneTheme {
        RoomScreen(paddingValues = PaddingValues(), navController = rememberNavController())
    }
}