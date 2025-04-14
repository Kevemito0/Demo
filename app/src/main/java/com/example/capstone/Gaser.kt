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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.util.UUID


data class FamilyMember(
    val id: String,
    val name: String,
    val email: String,
    val joinDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gaser(paddingValues: PaddingValues) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Davet Oluştur", "Koda Katıl", "Aile Üyeleri")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ev Güvenliği – Aile Yönetimi") })
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
                2 -> FamilyMemberListScreen()
            }
        }
    }
}

// 🔐 Kod oluşturma ekranı
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
            onClick = {
                isGenerating = true
                val code = (100000..999999).random().toString()
                inviteCode = code

                val inviteData = hashMapOf(
                    "code" to code,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "isUsed" to false,
                    "familyId" to "home123",
                    "inviterId" to (currentUser?.uid ?: "unknown")
                )

                firestore.collection("invites")
                    .add(inviteData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Kod oluşturuldu: $code", Toast.LENGTH_SHORT).show()
                        isGenerating = false
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Oluşturulamadı", Toast.LENGTH_SHORT).show()
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

// 🔓 Kod ile katılım ekranı
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
                firestore.collection("invites")
                    .whereEqualTo("code", inputCode)
                    .whereEqualTo("isUsed", false)
                    .get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            val doc = result.documents[0]
                            val familyId = doc.getString("familyId") ?: ""
                            val userId = currentUser?.uid ?: ""
                            val uuid = UUID.randomUUID().toString()
                            val memberData = hashMapOf(
                                "userId" to userId,
                                "joinedAt" to FieldValue.serverTimestamp(),
                                "role" to "member"
                            )
//
//                            firestore.collection("families")
//                                .document(familyId)
//                                .collection("members")
//                                .document(userId) // ✅ BU KISIM doğru
//                                .set(memberData)
                            val memberRef = firestore
                                .collection("families")
                                .document(familyId)
                                .collection("members")
                                .document(uuid)

                            memberRef.set(memberData)
                                .addOnSuccessListener {
                                    doc.reference.update("isUsed", true)
                                    Toast.makeText(context, "Aileye katıldınız!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Hata oluştu", Toast.LENGTH_SHORT).show()
                                }


                        } else {
                            Toast.makeText(context, "Kod geçersiz veya kullanıldı", Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Katıl")
        }
    }
}

// 📋 Aile üyelerini gösteren ekran (fake verilerle)
@Composable
fun FamilyMemberListScreen() {
    val context = LocalContext.current // ✅ Hemen fonksiyonun başında çağrıldı
    val memberList = remember {
        mutableStateListOf(
            FamilyMember("1", "Ahmet Yılmaz", "ahmet@ornek.com", "01.03.2023"),
            FamilyMember("2", "Ayşe Demir", "ayse@ornek.com", "28.02.2023"),
            FamilyMember("3", "Mehmet Kaya", "mehmet@ornek.com", "15.02.2023")
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                            Text(member.name, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                memberList.remove(member)
                                Toast.makeText(context, "${member.name} silindi", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil")
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

fun items(count: SnapshotStateList<FamilyMember>, key: (index: Int) -> Unit, itemContent: @Composable LazyItemScope.(index: Int) -> Unit) {

}
