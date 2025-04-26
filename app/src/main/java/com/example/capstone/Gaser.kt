import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import java.util.UUID


data class FamilyMember(
    val id: String,
    val name: String,
    val email: String,
    val joinDate: String,
    val role: String
)


fun logoutUser(onLoggedOut: () -> Unit) {
    FirebaseAuth.getInstance().signOut()
    onLoggedOut()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gaser(paddingValues: PaddingValues, navController: NavHostController) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Davet Oluştur", "Koda Katıl", "Aile Üyeleri")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ev Güvenliği – Aile Yönetimi") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış Yap")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Çıkış Yap") },
                text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
                confirmButton = {
                    Button(onClick = {
                        showLogoutDialog = false
                        logoutUser {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true } // ❗️"login" değil → dıştaki root hedef olmalı
                            }
                        }

                    }) {
                        Text("Evet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }

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
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> InviteGenerationScreen()
                1 -> JoinWithInviteCodeScreen()
                2 -> FamilyMemberListScreen(navController)
            }
        }
    }
}


@Composable
fun InviteGenerationScreen() {
    var inviteCode by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onClick@{
                if (currentUser == null) {
                    Toast.makeText(context, "Giriş yapılmamış!", Toast.LENGTH_SHORT).show()
                    return@onClick
                }

                isGenerating = true
                val userId = currentUser.uid

                firestore.collection("UsersTest").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val familyId = userDoc.getString("familyId") ?: run {
                            Toast.makeText(context, "Aile ID alınamadı", Toast.LENGTH_SHORT).show()
                            isGenerating = false
                            return@addOnSuccessListener
                        }

                        // Aile dokümanı yoksa oluştur
                        firestore.collection("Families").document(familyId).get()
                            .addOnSuccessListener { familyDoc ->
                                if (!familyDoc.exists()) {
                                    val familyData = hashMapOf(
                                        "ownerId" to userId,
                                        "createdAt" to FieldValue.serverTimestamp()
                                    )

                                    firestore.collection("Families").document(familyId).set(familyData)
                                }
                            }

                        val code = (100000..999999).random().toString()
                        inviteCode = code

                        val inviteData = hashMapOf(
                            "code" to code,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "isUsed" to false,
                            "familyId" to familyId,
                            "inviterId" to userId
                        )

                        firestore.collection("invites")
                            .add(inviteData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Kod oluşturuldu: $code", Toast.LENGTH_SHORT).show()
                                isGenerating = false
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Kod oluşturulamadı", Toast.LENGTH_SHORT).show()
                                isGenerating = false
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Kullanıcı verisi alınamadı", Toast.LENGTH_SHORT).show()
                        isGenerating = false
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGenerating
        ) {
            if (isGenerating) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            Text("Davet Kodu Oluştur")
        }

        Spacer(modifier = Modifier.height(16.dp))
        inviteCode?.let {
            val clipboardManager = LocalClipboardManager.current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Kod: $it", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(it))
                    Toast.makeText(context, "Kod kopyalandı", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala")
                }
            }
        }
    }
}

@Composable
fun JoinWithInviteCodeScreen() {
    var inputCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it },
            label = { Text("Davet Kodu") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val userId = currentUser?.uid ?: return@Button

                firestore.collection("invites")
                    .whereEqualTo("code", inputCode)
                    .whereEqualTo("isUsed", false)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Toast.makeText(context, "Kod geçersiz veya kullanıldı", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val doc = result.documents[0]
                        val familyId = doc.getString("familyId") ?: return@addOnSuccessListener

                        firestore.collection("UsersTest")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val name = userDoc.getString("User Name") ?: "Bilinmeyen"
                                val email = userDoc.getString("E-Mail") ?: "bilgi@belirsiz.com"

                                firestore.collection("UsersTest").document(userId)
                                    .update("familyId", familyId)
                                    .addOnSuccessListener {
                                        val memberData = hashMapOf(
                                            "userId" to userId,
                                            "name" to name,
                                            "email" to email,
                                            "joinedAt" to FieldValue.serverTimestamp(),
                                            "role" to "member"
                                        )

                                        firestore.collection("Families")
                                            .document(familyId)
                                            .collection("members")
                                            .document(userId)
                                            .set(memberData)
                                            .addOnSuccessListener {
                                                doc.reference.update("isUsed", true)
                                                Toast.makeText(context, "Aileye başarıyla katıldınız!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Aileye eklenemedi", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "FamilyID güncellenemedi", Toast.LENGTH_SHORT).show()
                                    }
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Kod kontrol hatası", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Katıl")
        }
    }
}

@Composable
fun FamilyMemberListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    val memberList = remember { mutableStateListOf<FamilyMember>() }
    val isOwner = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = currentUser?.uid ?: return@LaunchedEffect

        firestore.collection("UsersTest").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val familyId = userDoc.getString("familyId") ?: return@addOnSuccessListener

                firestore.collection("Families").document(familyId).get()
                    .addOnSuccessListener { famDoc ->
                        val ownerId = famDoc.getString("ownerId")
                        isOwner.value = ownerId == userId
                    }

                firestore.collection("Families")
                    .document(familyId)
                    .collection("members")
                    .get()
                    .addOnSuccessListener { result ->
                        memberList.clear()
                        for (doc in result.documents) {
                            val id = doc.getString("userId") ?: continue
                            val name = doc.getString("name") ?: "Bilinmeyen"
                            val email = doc.getString("email") ?: "-"
                            val joined = doc.getTimestamp("joinedAt")?.toDate()?.toString() ?: "-"
                            val role = doc.getString("role") ?: "member"
                            memberList.add(FamilyMember(id, name, email, joined, role))
                        }
                    }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 🔥 Çıkış Yap Butonu En Üste
        Button(
            onClick = {
                logoutUser {
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Çıkış Yap", color = MaterialTheme.colorScheme.onError)
        }

        Text("Aile Üyeleri (${memberList.size})", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(memberList, key = { it.id }) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${member.name} (${member.role})", fontWeight = FontWeight.Bold)
                            if (isOwner.value && member.role != "admin") {
                                IconButton(onClick = {
                                    // Silme işlemi burada
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil")
                                }
                            }
                        }
                        Text(member.email)
                        Text("Katılım: ${member.joinDate}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}



fun items(count: SnapshotStateList<FamilyMember>, key: (index: Int) -> Unit, itemContent: @Composable LazyItemScope.(index: Int) -> Unit) {}

