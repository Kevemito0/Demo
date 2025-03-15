package com.example.capstone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone.ui.theme.CapstoneTheme


var userName = ""

@Composable
fun Kerem(paddingValues: PaddingValues) {
//    MainScreen(paddingValues)
    LoginScreen(paddingValues)
}

@Composable
fun MainScreen(paddingValues: PaddingValues) {
    var number by remember { mutableStateOf(0) }

    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Text(
                text = "Kerem",
                fontStyle = FontStyle.Italic,
            )


            Text(
                text = "AAAAA",
                fontSize = 30.sp,
                modifier = Modifier.clickable {
                    number++;
                    println(number)
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = number.toString(),
                fontSize = 30.sp
            )
        }
    }
}

@Composable
fun LoginScreen(paddingValues: PaddingValues) {
    var userName by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    fun onEnterPressed() {
        println("Entered text: $userName") // Get the written text
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 150.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        TextField(value = userName,
            onValueChange = { userName = it },
            supportingText = {
                Text(
                    text = "",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },

            label = {
                Text(text = "User Name")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onNext = { }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = "")
            }
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            supportingText = {
                Text(
                    text = "Forgot Password",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    textDecoration = TextDecoration.Underline,
                    color = Color.Black
                )
            },
            label = {
                Text(text = "Password")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onEnterPressed() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            leadingIcon = {
                Icon(Icons.Filled.Password, contentDescription = "")
            },
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.VisibilityOff
                else
                    Icons.Filled.Visibility

                // This is the clickable icon to toggle password visibility
                Icon(
                    imageVector = image,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    modifier = Modifier.clickable {
                        passwordVisible = !passwordVisible
                    }
                )
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation()
        )

        Button(
            onClick = { onEnterPressed() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KeremPreview() {
    CapstoneTheme {
        LoginScreen(PaddingValues());
    }
}

