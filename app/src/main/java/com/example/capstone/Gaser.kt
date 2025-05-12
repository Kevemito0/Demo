package com.example.capstone

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

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
    val tabs = listOf("Family", "Join to Family", "Invitation Code")





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
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Person
                                    else -> Icons.Default.Key
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> FamilyMemberListScreen(navController)
                1 -> JoinWithInviteCodeScreen(navController)
                2 -> InviteGenerationScreen()
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
//        HardwareCommandButtons()

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

//@Composable
//fun HardwareCommandButtons() {
//    // Firebase RealtimeDB’de "komut" yoluna referans
//    val komutRef = FirebaseDatabase.getInstance().getReference("komut")
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        Button(
//            onClick = { komutRef.setValue("kapi_ac") }, colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text("Kapı Aç")
//        }
//        Button(
//            onClick = { komutRef.setValue("kamera_foto") }, colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text("Fotoğraf Çek")
//        }
//        Button(
//            onClick = { komutRef.setValue("vana_ac") }, colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text("Gaz Vanasını Aç")
//        }
//        Button(
//            onClick = { komutRef.setValue("vana_kapat") }, colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text("Gaz Vanasını Kapat")
//        }
//        Button(
//            onClick = { komutRef.setValue("buzzer_kapat") },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary
//            )
//        ) {
//            Text("Buzzer'ı Kapat")
//        }
//    }
//}

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
                            Toast.makeText(context, "Code is invalid or already used", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@addOnSuccessListener
                        }

                        val inviteDoc = result.documents[0]
                        val newFamilyId = inviteDoc.getString("familyId") ?: return@addOnSuccessListener
                        val inviteId = inviteDoc.id

                        firestore.collection("UsersTest").document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val userEmail = userDoc.getString("E-Mail") ?: ""
                                val userName = userDoc.getString("User Name") ?: ""
                                val currentFamily = userDoc.getString("familyId")

                                if (currentFamily != null && currentFamily != newFamilyId) {
                                    // Kullanıcı zaten başka bir ailede, onay al
                                    oldFamilyId = currentFamily
                                    pendingFamilyId = newFamilyId
                                    oldFamilyName = currentFamily // (Opsiyonel: familyName almak istersen çekebilirsin)
                                    showConfirmDialog = true
                                    isLoading = false
                                } else {
                                    // İlk kez katılıyor ya da aynı aileye
                                    joinFamily(userId, newFamilyId, inviteId, userName, userEmail, context) {
                                        isLoading = false
                                    }
                                }
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
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp).padding(end = 8.dp))
            Text("Join")
        }

// 🔽 AlertDialog mantıklı hale getirildi:
        if (showConfirmDialog && pendingFamilyId != null && oldFamilyId != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Family Change") },
                text = { Text("You are already in a family. Do you want to leave and join the new family?") },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        val memberRef = firestore.collection("Families")
                            .document(oldFamilyId!!)
                            .collection("members")
                            .document(userId)

                        memberRef.delete().addOnSuccessListener {
                            // Eski ailede kimse kalmadıysa aileyi tamamen sil (opsiyonel)
                            firestore.collection("Families").document(oldFamilyId!!)
                                .collection("members")
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.isEmpty) {
                                        firestore.collection("Families").document(oldFamilyId!!).delete()
                                    }
                                }

                            firestore.collection("UsersTest").document(userId)
                                .get()
                                .addOnSuccessListener { doc ->
                                    val name = doc.getString("User Name").orEmpty()
                                    val email = doc.getString("E-Mail").orEmpty()
                                    joinFamily(userId, pendingFamilyId!!, inputCode, name, email, context) {
                                        isLoading = false
                                    }
                                }
                        }
                    }) { Text("Yes, continue") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

private fun joinFamily(
    userId: String,
    familyId: String,
    inviteDocId: String,
    name: String,
    email: String,
    context: Context,
    onComplete: () -> Unit
) {
    val firestore = Firebase.firestore
    val currentTime = Date()

    val memberInfo = mapOf(
        "userId" to userId,
        "name" to name,
        "email" to email,
        "joinedAt" to currentTime,
        "role" to "Member"
    )

    firestore.collection("UsersTest").document(userId)
        .update("familyId", familyId, "inFamily", true)
        .addOnSuccessListener {
            firestore.collection("Families").document(familyId)
                .collection("members").document(userId)
                .set(memberInfo)

            firestore.collection("invites").document(inviteDocId)
                .update("isUsed", true)

            Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Join failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
        .addOnCompleteListener { onComplete() }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(memberList, key = { it.id }) { member ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("  ${member.name} (${member.role})", fontWeight = FontWeight.Bold)
                        if (isOwner && member.role != "admin" && member.id != currentUser?.uid) {
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
//                                if (member.id != currentUser?.uid) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
//                                }
//                                else{
//                                    Icon(Icons.Default.Done, contentDescription = "Done")
//                                }

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

