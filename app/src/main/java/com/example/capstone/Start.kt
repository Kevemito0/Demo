package com.example.capstone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun Start(navController: NavHostController) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(onClick = {
            navController.navigate("Kerem")
        }) {
            Text(text = "Kerem")
        }

        Button(onClick = {
            navController.navigate("Poyraz")
        }) {
            Text(text = "Poyraz")
        }

        Button(onClick = {
            navController.navigate("Gaser")
        }) {
            Text(text = "Gaser")
        }
    }
}
