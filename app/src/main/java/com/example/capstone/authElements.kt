package com.example.capstone

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
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
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor          = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor   = Color.Transparent,
        ),
        leadingIcon      = leadingIcon,
        trailingIcon     = trailingIcon,
        visualTransformation = visualTransformation,
        supportingText   = supportingText,
        keyboardOptions  = keyboardOptions,
        keyboardActions  = keyboardActions
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
        value               = password,
        onValueChange       = onPasswordChange,
        label               = label,
        leadingIcon         = { Icon(Icons.Filled.Password, contentDescription = null) },
        trailingIcon        = {
            val icon = if (passwordVisible)
                Icons.Filled.VisibilityOff
            else
                Icons.Filled.Visibility
            Icon(
                imageVector   = icon,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                modifier      = Modifier.clickable { onPasswordVisibilityChange() }
            )
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        supportingText      = supportingText,
        keyboardOptions     = KeyboardOptions.Default.copy(
            capitalization     = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType       = KeyboardType.Password,
            imeAction          = ImeAction.Done
        ),
        keyboardActions     = KeyboardActions(onDone = { onDone() }),
        modifier            = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    paddingValues: PaddingValues,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var userName        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        color = Color(0xFF1A1A40)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = "Login",
                style    = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp),
                color = Color(0xFFFFFFFF)
            )

            AuthTextField(
                value           = userName,
                onValueChange   = { userName = it },
                label           = "Username",
                leadingIcon     = { Icon(Icons.Filled.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { /* focus password */ })
            )

            PasswordField(
                password                   = password,
                label                      = "Password",
                onPasswordChange           = { password = it },
                passwordVisible            = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                onDone                     = { onLoginClick(userName, password) },
                supportingText             = {
                    Text(
                        text          = "Forgot Password",
                        style         = MaterialTheme.typography.bodySmall.copy(
                            fontWeight     = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        ),
                        color         = MaterialTheme.colorScheme.primary,
                        modifier      = Modifier
                            .fillMaxWidth()
                            .clickable { onForgotPasswordClick() }
                            .padding(end = 16.dp),
                        textAlign     = TextAlign.End
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick  = { onLoginClick(userName, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF252424))
            ) {
                Text(
                    text  = "Login",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                )
            }

            Row(
                modifier              = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text     = "Register",
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    paddingValues: PaddingValues,
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var userName               by remember { mutableStateOf("") }
    var email                  by remember { mutableStateOf("") }
    var password               by remember { mutableStateOf("") }
    var confirmPassword        by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError          by remember { mutableStateOf<String?>(null) }

    fun validatePassword() {
        if (password.isNotEmpty()) {
            val (secure, msg) = checkPasswordSecurity(password)
            passwordError = if (!secure) msg else null
        } else passwordError = null
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = "Register",
                style    = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Username
            AuthTextField(
                value          = userName,
                onValueChange  = { userName = it },
                label          = "Username",
                leadingIcon    = { Icon(Icons.Filled.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { /* focus email */ })
            )

            // Email
            AuthTextField(
                value          = email,
                onValueChange  = { email = it },
                label          = "Email",
                leadingIcon    = { Icon(Icons.Filled.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { /* focus password */ })
            )

            // Password
            PasswordField(
                password                   = password,
                label                      = "Password",
                onPasswordChange           = {
                    password = it
                    validatePassword()
                },
                passwordVisible            = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                onDone                     = { /* focus confirm */ },
                supportingText             = {
                    passwordError?.let {
                        Text(it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp))
                    }
                }
            )

            // Confirm
            PasswordField(
                password                   = confirmPassword,
                label                      = "Confirm Password",
                onPasswordChange           = { confirmPassword = it },
                passwordVisible            = confirmPasswordVisible,
                onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                onDone                     = {
                    if (password == confirmPassword && passwordError == null) {
                        onRegisterClick(userName, email, password)
                    }
                },
                supportingText             = {
                    if (password.isNotEmpty()
                        && confirmPassword.isNotEmpty()
                        && password != confirmPassword) {
                        Text("Passwords do not match",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            textAlign = TextAlign.End)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick  = { onRegisterClick(userName, email, password) },
                enabled  = password.isNotEmpty() && password == confirmPassword && passwordError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text  = "Register",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                )
            }

            Row(
                modifier            = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text("Already have an account?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text           = "Login",
                    style          = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color          = MaterialTheme.colorScheme.primary,
                    modifier       = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    paddingValues: PaddingValues,
    onReset: (String) -> Unit,
    onBack: () -> Unit
) {
    var email       by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = "Reset Password",
                style    = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            TextField(
                value          = email,
                onValueChange  = { email = it },
                label          = { Text("Email") },
                singleLine     = true,
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape          = RoundedCornerShape(12.dp),
                colors         = TextFieldDefaults.textFieldColors(
                    containerColor          = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions {
                    /* same as pressing the button below */
                    if (email.isBlank()) {
                        infoMessage = "Please enter your email"
                    } else {
                        onReset(email)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick    = {
                    if (email.isBlank()) {
                        infoMessage = "Please enter your email"
                    } else {
                        onReset(email)
                    }
                },
                enabled    = email.isNotBlank(),
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape      = RoundedCornerShape(12.dp),
                colors     = ButtonDefaults.buttonColors(containerColor = Color(0xFF13C001))
            ) {
                Text(
                    text  = "Send Reset Link",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                )
            }

            infoMessage?.let {
                Text(
                    text     = it,
                    color    = if (it.startsWith("Please")) Color.Red else Color.Green,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text           = "← Back to login",
                style          = MaterialTheme.typography.bodySmall.copy(
                    fontWeight     = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline
                ),
                color          = MaterialTheme.colorScheme.primary,
                modifier       = Modifier
                    .padding(top = 24.dp)
                    .clickable { onBack() }
            )
        }
    }
}

@Composable
fun AuthScreens(paddingValues: PaddingValues, navController: NavHostController) {
    var currentScreen by remember { mutableStateOf("login") }
    var showDialog    by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var onDialogDismiss by remember { mutableStateOf({}) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDialogDismiss()
            },
            title   = { Text("Notification") },
            text    = { Text(dialogMessage) },
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
            paddingValues         = paddingValues,
            onLoginClick          = { u, p ->
                loginUser(u, p,
                    onSuccess = {
                        navController.navigate("main") { popUpTo("auth") { inclusive = true } }
                    },
                    onFailure = { msg ->
                        dialogMessage = msg
                        onDialogDismiss = {}
                        showDialog = true
                    }
                )
            },
            onRegisterClick       = { currentScreen = "register" },
            onForgotPasswordClick = { currentScreen = "reset" }
        )

        "register" -> RegisterScreen(
            paddingValues   = paddingValues,
            onRegisterClick = { u, e, p ->
                registerUser(u, e, p,
                    onSuccess = {
                        dialogMessage = "Registration successful!"
                        onDialogDismiss = { currentScreen = "login" }
                        showDialog = true
                    },
                    onFailure = { msg ->
                        dialogMessage = msg
                        onDialogDismiss = {}
                        showDialog = true
                    }
                )
            },
            onLoginClick     = { currentScreen = "login" }
        )

        "reset" -> ResetPasswordScreen(
            paddingValues = paddingValues,
            onReset       = { email ->
                resetPassword(email,
                    onSuccess = {
                        dialogMessage = "Password reset email sent!"
                        onDialogDismiss = { currentScreen = "login" }
                        showDialog = true
                    },
                    onFailure = { msg ->
                        dialogMessage = "Error: $msg"
                        onDialogDismiss = {}
                        showDialog = true
                    }
                )
            },
            onBack        = { currentScreen = "login" }
        )
    }
}

fun resetPassword(
    email: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    FirebaseAuth.getInstance()
        .sendPasswordResetEmail(email)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e.message ?: "Unknown error") }
}

fun registerUser(
    username: String,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db   = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val randomFamilyId = db.collection("Families").document().id

    db.collection("UsersTest")
        .whereEqualTo("User Name", username)
        .get()
        .addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                onFailure("This username is already taken.")
                return@addOnSuccessListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener
                    val user = hashMapOf(
                        "User Name" to username,
                        "E-Mail"    to email,
                        "Password"  to hashPassword(password),
                        "userId"    to userId,
                        "familyId"  to randomFamilyId
                    )
                    db.collection("UsersTest").document(userId)
                        .set(user)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure("Firestore error: ${e.message}") }
                }
                .addOnFailureListener { e -> onFailure("Auth error: ${e.message}") }
        }
        .addOnFailureListener { e -> onFailure("Firestore lookup error: ${e.message}") }
}

fun loginUser(
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db   = Firebase.firestore
    val auth = FirebaseAuth.getInstance()

    db.collection("UsersTest")
        .whereEqualTo("User Name", username)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                onFailure("User not found.")
                return@addOnSuccessListener
            }
            val email = docs.documents[0].getString("E-Mail") ?: return@addOnSuccessListener
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure("Login failed: ${e.message}") }
        }
        .addOnFailureListener { e -> onFailure("Firestore lookup error: ${e.message}") }
}

private fun hashPassword(password: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .joinToString("") { "%02x".format(it) }

fun checkPasswordSecurity(password: String): Pair<Boolean, String> {
    if (password.length < 8) return false to "Password must be at least 8 characters."
    if (!password.any { it.isUpperCase() }) return false to "Include at least one uppercase letter."
    if (!password.any { it.isLowerCase() }) return false to "Include at least one lowercase letter."
    if (!password.any { it.isDigit() }) return false to "Include at least one digit."
    val specials = "!@#$%^&*()_-+=<>?/[]{}|."
    if (!password.any { it in specials }) return false to "Include at least one special character."
    val common = listOf("password","123456","qwerty","admin","welcome")
    if (password.lowercase() in common) return false to "Too common; choose another password."
    if (password.groupBy { it }.any { it.value.size > 3 }) return false to "Too many repeated characters."
    val sequences = listOf("abcdef","123456","qwerty")
    if (sequences.any { seq -> seq.windowed(3).any { password.lowercase().contains(it) } }) {
        return false to "Avoid sequential characters."
    }
    return true to "Strong password!"
}

@Preview(showBackground = true)
@Composable
fun OpenAuthScreensPreview() {
    AuthScreens(PaddingValues(), rememberNavController())
}
