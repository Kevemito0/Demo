package com.example.capstone

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.capstone.ui.theme.CapstoneTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.UUID

data class FamilyMember(
    val id: String,
    val name: String,
    val email: String,
    val joinDate: String,
    val role: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gaser(paddingValues: PaddingValues, navController: NavHostController) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Invitation Code", "Join to Family", "Family")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ev Güvenliği – Aile Yönetimi") },

                )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Key
                                    1 -> Icons.Default.Person
                                    else -> Icons.Default.Home
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> InviteGenerationScreen()
                1 -> JoinWithInviteCodeScreen(navController)
                2 -> FamilyMemberListScreen(navController)
            }
        }
    }
}

@Composable
fun InviteGenerationScreen() {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser

    var inviteCode by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                if (currentUser == null) {
                    Toast.makeText(context, "Giriş yapılmamış!", Toast.LENGTH_SHORT).show()
                } else {
                    isGenerating = true
                    val userId = currentUser.uid
                    firestore.collection("UsersTest").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val familyId = userDoc.getString("familyId")
                                ?: run {
                                    Toast.makeText(context, "Aile ID alınamadı", Toast.LENGTH_SHORT)
                                        .show()
                                    isGenerating = false
                                    return@addOnSuccessListener
                                }
                            val familyRef = firestore.collection("Families").document(familyId)
                            familyRef.get().addOnSuccessListener { famDoc ->

                                val code = (100000..999999).random().toString()
                                inviteCode = code
                                firestore.collection("invites")
                                    .add(
                                        mapOf(
                                            "code" to code,
                                            "createdAt" to FieldValue.serverTimestamp(),
                                            "isUsed" to false,
                                            "familyId" to familyId,
                                            "inviterId" to userId
                                        )
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Code generated: $code",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isGenerating = false
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Could not generate code",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isGenerating = false
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Kullanıcı verisi alınamadı",
                                Toast.LENGTH_SHORT
                            ).show()
                            isGenerating = false
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            if (isGenerating) CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text("Generate Invitation Code")
        }
        HardwareCommandButtons()

        Spacer(modifier = Modifier.height(16.dp))

        inviteCode?.let { code ->
            val clipboard = LocalClipboardManager.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kod: $code", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(code))
                    Toast.makeText(context, "Code coppied", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }
        }
    }
}

@Composable
fun HardwareCommandButtons() {
    // Firebase RealtimeDB’de "komut" yoluna referans
    val komutRef = FirebaseDatabase.getInstance().getReference("komut")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { komutRef.setValue("kapi_ac") }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Kapı Aç")
        }
        Button(
            onClick = { komutRef.setValue("kamera_foto") }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Fotoğraf Çek")
        }
        Button(
            onClick = { komutRef.setValue("vana_ac") }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Gaz Vanasını Aç")
        }
        Button(
            onClick = { komutRef.setValue("vana_kapat") }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Gaz Vanasını Kapat")
        }
        Button(
            onClick = { komutRef.setValue("buzzer_kapat") },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Buzzer'ı Kapat")
        }
    }
}

@Composable
fun JoinWithInviteCodeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var inputCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingFamilyId by remember { mutableStateOf<String?>(null) }
    var oldFamilyId by remember { mutableStateOf<String?>(null) }
    var oldFamilyName by remember { mutableStateOf<String?>(null) }

    // Mevcut aile ID'sini al
    LaunchedEffect(userId) {
        firestore.collection("UsersTest").document(userId)
            .get()
            .addOnSuccessListener { doc -> oldFamilyId = doc.getString("familyId") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it },
            label = { Text("Invitation Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                firestore.collection("invites")
                    .whereEqualTo("code", inputCode)
                    .whereEqualTo("isUsed", false)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Toast.makeText(
                                context,
                                "Code is invalid or already used",
                                Toast.LENGTH_SHORT
                            ).show()
                            isLoading = false
                            return@addOnSuccessListener
                        }

                        val inviteDoc = result.documents[0]
                        val newFamilyId =
                            inviteDoc.getString("familyId") ?: return@addOnSuccessListener
                        val inviteId = inviteDoc.id

                        // Kullanıcıyı UsersTest'ten çek
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                            ?: return@addOnSuccessListener
                        firestore.collection("UsersTest")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val userEmail = userDoc.getString("E-Mail") ?: ""
                                val userName = userDoc.getString("User Name") ?: ""

                                // 1. Kullanıcının familyId'sini güncelle
                                firestore.collection("UsersTest").document(userId)
                                    .update("familyId", newFamilyId)

                                // 2. Families → {familyId} → members → {userId}
                                val currentTime = Date()
                                val memberInfo = mapOf(
                                    "email" to userEmail,
                                    "name" to userName,
                                    "joinedAt" to currentTime,
                                    "role" to "Member",
                                    "userId" to userId
                                )
                                firestore.collection("Families")
                                    .document(newFamilyId)
                                    .collection("members")
                                    .document(userId)
                                    .set(memberInfo)

                                // 3. invite isUsed = true
                                firestore.collection("invites").document(inviteId)
                                    .update("isUsed", true)

                                firestore.collection("UsersTest")
                                    .document(userId)
                                    .update("inFamily", true)

                                Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT)
                                    .show()
                                isLoading = false
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
            }
            Text("Join")
        }

    }

    if (showConfirmDialog && pendingFamilyId != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Family Change") },
            text = { Text("You are already in **${oldFamilyName}** . Are you sure you want to leave this family and join the new one?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    // Eski aileden sil ve yeni aileye katıl
                    firestore.collection("Families").document(oldFamilyId!!)
                        .collection("members").document(userId).delete()
                        .addOnSuccessListener {
                            joinFamily(userId, pendingFamilyId!!, inputCode, context) {
                                isLoading = false
                            }
                        }
                }) { Text("Yes,continue") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun joinFamily(
    userId: String,
    familyId: String,
    inviteDocId: String,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    val firestore = Firebase.firestore

    // UsersTest'ten name ve email al
    firestore.collection("UsersTest").document(userId)
        .get()
        .addOnSuccessListener { userDoc ->
            val name = userDoc.getString("User Name").orEmpty()
            val email = userDoc.getString("E-Mail").orEmpty()

            // UsersTest'teki familyId güncelle
            firestore.collection("UsersTest").document(userId)
                .update("familyId", familyId)
                .addOnSuccessListener {
                    // Yeni üye verilerini hazırla
                    val memberData = hashMapOf(
                        "userId" to userId,
                        "name" to name,
                        "email" to email,
                        "joinedAt" to FieldValue.serverTimestamp(),
                        "role" to "member"
                    )
                    // Yeni aileye ekle
                    firestore.collection("Families").document(familyId)
                        .collection("members").document(userId)
                        .set(memberData)

                    // Davet kodunu işaretle
                    firestore.collection("invites").document(inviteDocId)
                        .update("isUsed", true)

                    Toast.makeText(
                        context,
                        "You have successfully joined the family!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error while joining: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnCompleteListener { onComplete() }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Kullanıcı verisi okunamadı: ${e.message}", Toast.LENGTH_SHORT)
                .show()
            onComplete()
        }
}

@Composable
fun FamilyMemberListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var randomFamilyId = firestore.collection("Families").document().id

    var familyId by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var isOwner by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val memberList = remember { mutableStateListOf<FamilyMember>() }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            firestore.collection("UsersTest").document(userId)
                .addSnapshotListener { userDoc, _ ->
                    userDoc?.getString("familyId")?.let { fid ->
                        familyId = fid
                        // listen family doc
                        firestore.collection("Families").document(fid)
                            .addSnapshotListener { famDoc, _ ->
                                famDoc?.let { doc ->
                                    familyName = doc.getString("familyName").orEmpty()
                                    isOwner = doc.getString("ownerId") == userId
                                }
                            }
                        // listen members
                        firestore.collection("Families").document(fid)
                            .collection("members")
                            .addSnapshotListener { snap, _ ->
                                memberList.clear()
                                snap?.documents?.forEach { d ->
                                    memberList.add(
                                        FamilyMember(
                                            id = d.getString("userId").orEmpty(),
                                            name = d.getString("name").orEmpty(),
                                            email = d.getString("email").orEmpty(),
                                            joinDate = d.getTimestamp("joinedAt")?.toDate()
                                                .toString(),
                                            role = d.getString("role").orEmpty()
                                        )
                                    )
                                }
                            }
                    }
                }
        }
    }

    Spacer(Modifier.height(16.dp))

    FamilyNameCard(
        familyName = familyName,
        isOwner = isOwner,
        onEditClick = { showEditDialog = true }
    )

    if (showEditDialog && isOwner) {
        EditFamilyNameDialog(
            currentName = familyName,
            onConfirm = { updated ->
                firestore.collection("Families").document(familyId)
                    .update("familyName", updated)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Family name updated!", Toast.LENGTH_SHORT).show()
                    }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    Spacer(Modifier.height(24.dp))
    Text("Family Members (${memberList.size})", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))

    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(memberList, key = { it.id }) { member ->
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("  ${member.name} (${member.role})", fontWeight = FontWeight.Bold)
                        if (isOwner && member.role != "admin") {
                            IconButton(onClick = {
                                firestore.collection("Families")
                                    .document(familyId)
                                    .collection("members")
                                    .document(member.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        memberList.remove(member)
                                        firestore.collection("UsersTest")
                                            .document(member.id)
                                            .update("inFamily", false)
                                        Toast.makeText(
                                            context,
                                            "${member.name} silindi",

                                            Toast.LENGTH_SHORT,

                                            ).show()


                                        member.id.let { userId ->
                                            firestore.collection("UsersTest").document(userId)
                                                .update("familyId", randomFamilyId)
                                                .addOnSuccessListener {
                                                    Log.d(
                                                        "UpdateFamilyId",
                                                        "familyId başarıyla güncellendi"
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "UpdateFamilyId",
                                                        "familyId güncellenirken hata oluştu",
                                                        e
                                                    )
                                                }
                                        }


                                    }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }


                    Text(
                        "  Join Time: ${member.joinDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun FamilyNameCard(
    familyName: String,
    isOwner: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(" $familyName", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            if (isOwner) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Ev")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFamilyNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempName by rememberSaveable { mutableStateOf(currentName) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aile İsmini Düzenle") },
        text = {
            Column {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = {
                        tempName = it
                        errorMsg = if (it.isBlank()) "İsim boş olamaz" else null
                    },
                    label = { Text("Yeni Aile İsmi") },
                    singleLine = true,
                    isError = errorMsg != null,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMsg?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (tempName.isNotBlank()) onConfirm(tempName) }) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GaserPreview() {
    CapstoneTheme {
        Gaser(paddingValues = PaddingValues(), navController = rememberNavController())
    }
}

