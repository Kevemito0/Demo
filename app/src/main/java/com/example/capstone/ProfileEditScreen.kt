package com.example.capstone

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.capstone.checkPasswordSecurity

// Re-auth helper
private fun reAuthUser(
    currentEmail: String,
    currentPassword: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (currentPassword.isBlank()) {
        onFailure(IllegalArgumentException("Current password must not be empty"))
        return
    }
    FirebaseAuth.getInstance().currentUser?.let { user ->
        val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    } ?: onFailure(Exception("No authenticated user"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    paddingValues: PaddingValues,
    navController: NavController
) {
    val ctx = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser ?: return
    val db = Firebase.firestore

    // UI state
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(currentUser.email.orEmpty()) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // visibility toggles
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    // Load display name once
    LaunchedEffect(currentUser.uid) {
        db.collection("UsersTest").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                name = doc.getString("User Name").orEmpty()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileEdit", "Error loading name", e)
            }
    }

    fun validateNewPassword(pw: String) {
        val (isSecure, message) = checkPasswordSecurity(pw)
        passwordError = if (pw.isNotBlank() && !isSecure) message else null
    }

    fun updateFirestoreProfile(uid: String) {
        db.collection("UsersTest").document(uid)
            .update("User Name", name, "E-Mail", email)
            .addOnSuccessListener {
                Toast.makeText(ctx, "Profile updated!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileEdit", "Firestore update failed", e)
                Toast.makeText(ctx, "Firestore update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                loading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Current Password
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password") },
            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                    Icon(
                        imageVector = if (showCurrentPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // New Password
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                validateNewPassword(it)
            },
            label = { Text("New Password") },
            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = {
                passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            trailingIcon = {
                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                    Icon(
                        imageVector = if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Confirm New Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm New Password") },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = {
                if (newPassword.isNotBlank() &&
                    confirmPassword.isNotBlank() &&
                    newPassword != confirmPassword
                ) {
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        // Save button: reAuth, update Email immediately, then Password, then Firestore
        Button(
            onClick = {
                val oldEmail = currentUser.email.orEmpty()

                // If only changing name
                if (email == oldEmail && newPassword.isBlank()) {
                    loading = true
                    updateFirestoreProfile(currentUser.uid)
                    return@Button
                }

                // Require current password if email or password change
                if ((email != oldEmail || newPassword.isNotBlank()) && currentPassword.isBlank()) {
                    Toast.makeText(ctx, "Please enter your current password", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Block insecure password
                if (passwordError != null) {
                    Toast.makeText(ctx, passwordError, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Block mismatch
                if (newPassword.isNotBlank() && newPassword != confirmPassword) {
                    Toast.makeText(ctx, "New passwords don’t match", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                reAuthUser(
                    currentEmail = oldEmail,
                    currentPassword = currentPassword,
                    onSuccess = {
                        // --- EMAIL UPDATE ---
                        if (email != oldEmail) {
                            Log.d("ProfileEdit", "→ Calling updateEmail($email)…")
                            currentUser.updateEmail(email)
                                .addOnSuccessListener {
                                    Log.d("ProfileEdit", "✓ updateEmail succeeded; new email = ${currentUser.email}")
                                    Toast.makeText(ctx, "Email changed to ${currentUser.email}", Toast.LENGTH_SHORT).show()
                                    currentUser.sendEmailVerification()
                                    updateFirestoreProfile(currentUser.uid)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ProfileEdit", "✗ updateEmail failed", e)
                                    Toast.makeText(ctx, "Email update failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    loading = false
                                }
                        }

                        // --- PASSWORD UPDATE ---
                        if (newPassword.isNotBlank()) {
                            Log.d("ProfileEdit", "→ Calling updatePassword…")
                            currentUser.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    Log.d("ProfileEdit", "✓ updatePassword succeeded")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ProfileEdit", "✗ updatePassword failed", e)
                                    Toast.makeText(ctx, "Password update failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                                .addOnCompleteListener {
                                    // If email was left unchanged, sync Firestore now
                                    if (email == oldEmail) {
                                        updateFirestoreProfile(currentUser.uid)
                                    }
                                }
                        }
                    },
                    onFailure = { e ->
                        Log.e("ProfileEdit", "✗ Re-authentication failed", e)
                        Toast.makeText(ctx, "Re-authentication failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        loading = false
                    }
                )
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),

        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Save")
            }
        }
    }
}
