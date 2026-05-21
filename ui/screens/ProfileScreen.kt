package com.example.helpern2.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.helpern2.ui.components.CustomTextField
import com.example.helpern2.utils.ImageUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("user") }
    var userPhone by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var joinedDate by remember { mutableStateOf<Timestamp?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            userEmail = currentUser.email ?: ""
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    userName = document.getString("name") ?: ""
                    userRole = document.getString("role") ?: "user"
                    userPhone = document.getString("phone") ?: ""
                    userAddress = document.getString("address") ?: ""
                    profileImageUrl = document.getString("profileImageUrl")
                    val timestamp = document.getTimestamp("joinedDate")
                    if (timestamp == null) {
                        val now = Timestamp.now()
                        joinedDate = now
                        db.collection("users").document(currentUser.uid).update("joinedDate", now)
                    } else {
                        joinedDate = timestamp
                    }
                } else {
                    userName = currentUser.displayName ?: "User"
                    val now = Timestamp.now()
                    joinedDate = now
                    val newUser = hashMapOf(
                        "name" to userName,
                        "email" to userEmail,
                        "role" to "user",
                        "joinedDate" to now
                    )
                    db.collection("users").document(currentUser.uid).set(newUser)
                }
                isLoading = false
            }.addOnFailureListener {
                isLoading = false
            }
        }
    }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    fun saveProfile() {
        if (currentUser == null) return
        isLoading = true

        val onComplete = { imageUrl: String? ->
            val updates = mutableMapOf<String, Any>(
                "name" to userName,
                "phone" to userPhone,
                "address" to userAddress
            )
            imageUrl?.let { updates["profileImageUrl"] = it }

            db.collection("users").document(currentUser.uid)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    isEditing = false
                    isLoading = false
                    if (imageUrl != null) profileImageUrl = imageUrl
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        if (selectedImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("profile_images/${currentUser.uid}.jpg")
            
            val compressedImage = ImageUtils.compressImage(context, selectedImageUri!!)
            
            val uploadTask = if (compressedImage != null) {
                storageRef.putBytes(compressedImage)
            } else {
                storageRef.putFile(selectedImageUri!!)
            }

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(task.result.toString())
                } else {
                    isLoading = false
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            onComplete(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { saveProfile() }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(enabled = isEditing) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    val imageSource = selectedImageUri ?: profileImageUrl
                    if (imageSource != null) {
                        AsyncImage(
                            model = imageSource,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Select Profile Picture",
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (isEditing) {
                    Text(
                        text = "Tap to change photo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isEditing) {
                    CustomTextField(value = userName, onValueChange = { userName = it }, label = "Full Name")
                } else {
                    Text(text = userName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = userEmail, fontSize = 16.sp, color = Color.Gray)
                }
                
                if (userRole == "admin") {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "ADMIN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                if (isEditing) {
                    CustomTextField(value = userPhone, onValueChange = { userPhone = it }, label = "Phone")
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(value = userAddress, onValueChange = { userAddress = it }, label = "Address")
                } else {
                    ProfileInfoItem(label = "Phone", value = userPhone.ifEmpty { "Not set" })
                    ProfileInfoItem(label = "Address", value = userAddress.ifEmpty { "Not set" })
                    
                    val dateStr = joinedDate?.let {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        sdf.format(it.toDate())
                    } ?: "N/A"
                    ProfileInfoItem(label = "Joined", value = dateStr)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
