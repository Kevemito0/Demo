package com.example.capstone


import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class User(
    val id: String,
    val name: String,
    val email: String,
    val joinDate: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gaser(paddingValues: PaddingValues) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Davet Linkleri", "Kullanıcılar")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kullanıcı Yönetimi") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                    0 -> Icons.Default.PersonAdd
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> InvitationLinkScreen()
                1 -> UserListScreen()
            }
        }
    }
}

@Composable
fun InvitationLinkScreen() {
    var userLink by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isGeneratingLink by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Toast mesajını göstermek için
    if (showToast) {
        LaunchedEffect(showToast) {
            delay(200) // Kısa bir gecikme
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Generate Link Button
        Button(
            onClick = {
                if (!isGeneratingLink) {
                    isGeneratingLink = true
                    userLink = generateUserLink()
                    toastMessage = "Link oluşturuldu!"
                    showToast = true
                    isGeneratingLink = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGeneratingLink
        ) {
            Text(text = "Davet Linki Oluştur")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Generated Link
        userLink?.let { link ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = link,
                        modifier = Modifier.weight(1f)
                    )

                    // Copy Button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(link))
                            toastMessage = "Panoya kopyalandı!"
                            showToast = true
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala")
                    }
                }
            }
        }
    }
}

@Composable
fun UserListScreen() {
    // Örnek kullanıcı listesi
    val userList = remember {
        mutableStateListOf(
            User("1", "Ahmet Yılmaz", "ahmet@ornek.com", "01.03.2025"),
            User("2", "Ayşe Demir", "ayse@ornek.com", "28.02.2025"),
            User("3", "Mehmet Kaya", "mehmet@ornek.com", "15.02.2025"),
            User("4", "Zeynep Şahin", "zeynep@ornek.com", "10.02.2025"),
            User("5", "Can Öztürk", "can@ornek.com", "05.02.2025")
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Toast mesajını göstermek için
    if (showToast) {
        LaunchedEffect(showToast) {
            delay(200) // Kısa bir gecikme
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Arama alanı
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kullanıcı Ara") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kullanıcı sayısı
        val filteredUsers = userList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }

        Text(
            text = "Toplam ${filteredUsers.size} kullanıcı",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Kullanıcı listesi
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredUsers,
                key = { user -> user.id } // Performans için key kullanımı
            ) { user ->
                UserListItem(
                    user = user,
                    onDelete = {
                        userList.remove(user)
                        toastMessage = "${user.name} silindi"
                        showToast = true
                    }
                )
            }
        }
    }
}

@Composable
fun UserListItem(user: User, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Kullanıcıyı Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Katılım: ${user.joinDate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Function to Generate a Unique Link
fun generateUserLink(): String {
    val uniqueId = Random.nextInt(100000, 999999) // Generate a random 6-digit ID
    return "https://yourapp.com/invite/$uniqueId"
}