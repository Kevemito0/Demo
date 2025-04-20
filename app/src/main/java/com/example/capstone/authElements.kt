package com.example.capstone

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import java.security.MessageDigest


@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: @Composable (() -> Unit)? = null

) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        supportingText = supportingText
    )
}

@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: @Composable (() -> Unit)? = null,
    label: String
) {
    AuthTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = label,
        leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier,
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            Icon(
                imageVector = image,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                modifier = Modifier.clickable { onPasswordVisibilityChange() }
            )
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        supportingText = supportingText
    )
}

@Composable
fun LoginScreen(
    paddingValues: PaddingValues,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val label = "password"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Login",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username field
        AuthTextField(
            value = userName,
            onValueChange = { userName = it },
            label = "Username",
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Focus on password */ })
        )

        // Password field
        PasswordField(
            password = password,
            label = label,
            onPasswordChange = { password = it },
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            onDone = { onLoginClick(userName, password) },
            supportingText = {
                Text(
                    text = "Forgot Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Handle password reset */ },
                    textAlign = TextAlign.End,
                    textDecoration = TextDecoration.Underline
                )
            }
        )

        // Login button
        Button(
            onClick = { onLoginClick(userName, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Login")
        }

        // Register option
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?")
            Text(
                text = "Register",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onRegisterClick() },
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
fun RegisterScreen(
    paddingValues: PaddingValues,
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val label = "Password"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Register",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username field
        AuthTextField(
            value = userName,
            onValueChange = { userName = it },
            label = "Username",
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { /* Focus on email */ })
        )

        // Email field
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null
                )
            }, // Replace with email icon
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Focus on password */ })
        )

        // Password field
        PasswordField(
            password = password,
            label = label,
            onPasswordChange = { password = it },
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            onDone = { /* Focus on confirm password */ }
        )

        // Confirm password field
        PasswordField(
            password = confirmPassword,
            label = "Confirm your password",
            onPasswordChange = { confirmPassword = it },
            passwordVisible = confirmPasswordVisible,
            onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
            onDone = { onRegisterClick(userName, email, password) },
            supportingText = {
                if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        )

        // Register button
        Button(
            onClick = { onRegisterClick(userName, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = password.isNotEmpty() && password == confirmPassword
        ) {
            Text("Register")
        }

        // Login option
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?")
            Text(
                text = "Login",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onLoginClick() },
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
fun AuthScreens(paddingValues: PaddingValues, navController : NavHostController) {
    var currentScreen by remember { mutableStateOf("login") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var onDialogDismiss by remember { mutableStateOf({}) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDialogDismiss()
            },
            title = { Text("Notification") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    onDialogDismiss()
                }) {
                    Text("OK")
                }
            }
        )
    }

    when (currentScreen) {
        "login" -> LoginScreen(
            paddingValues = paddingValues,
            onLoginClick = { username, password ->
                loginUser(
                    username = username,
                    password = password,
                    onSuccess = {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onFailure = { errorMessage ->
                        // Show error dialog
                        dialogMessage = errorMessage
                        onDialogDismiss = {}
                        showDialog = true
                    }
                )
            },
            onRegisterClick = {
                currentScreen = "register"
            }
        )

        "register" -> RegisterScreen(
            paddingValues = paddingValues,
            onRegisterClick = { username, email, password ->
                registerUser(
                    username = username,
                    email = email,
                    password = password,
                    onSuccess = {
                        dialogMessage = "Kayıt başarılı!"
                        onDialogDismiss = { currentScreen = "login" }
                        showDialog = true
                    },
                    onFailure = { errorMessage ->
                        dialogMessage = errorMessage
                        showDialog = true
                    }
                )
            },

                    onLoginClick = {
                currentScreen = "login"
            }
        )
    }
}

fun registerUser(
    username: String,
    email: String,
    password: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val randomFamilyId = db.collection("Families").document().id

    db.collection("UsersTest")
        .whereEqualTo("User Name", username)
        .get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                onFailure("Bu kullanıcı adı zaten var")
                return@addOnSuccessListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid ?: return@addOnSuccessListener
                    val hashedPassword = hashPassword(password)

                    val user = hashMapOf(
                        "User Name" to username,
                        "E-Mail" to email,
                        "Password" to hashedPassword,
                        "userId" to userId,
                        "familyId" to randomFamilyId
                    )

                    db.collection("UsersTest")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            onSuccess(userId)
                        }
                        .addOnFailureListener { e ->
                            onFailure("Kullanıcı Firestore'a kaydedilemedi: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    onFailure("FirebaseAuth kaydı başarısız: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            onFailure("Kullanıcı adı kontrolü başarısız: ${e.message}")
        }
}


// Very simple password hashing function for demonstration
// In a real app, use a proper security library
private fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

fun loginUser(
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = Firebase.firestore

    // Query Firestore for documents where "User Name" matches the provided username
    db.collection("UsersTest")
        .whereEqualTo("User Name", username)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // No user found with this username
                onFailure("User not found")
                return@addOnSuccessListener
            }

            // Since usernames should be unique, we can take the first document
            val userDoc = documents.documents[0]
            val storedPassword = userDoc.getString("Password")

            val hashedInputPassword = hashPassword(password)

            if (storedPassword == hashedInputPassword) {
                // Password matches, login successful
                onSuccess()
            } else {
                // Password doesn't match
                onFailure("Incorrect password")
            }
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error during login", e)
            onFailure("Login error: ${e.message}")
        }
}

@Preview(showBackground = true)
@Composable
fun OpenAuthScreens() {
    val navController = rememberNavController()
    AuthScreens(PaddingValues(), navController)
}
