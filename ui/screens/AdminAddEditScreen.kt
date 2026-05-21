package com.example.helpern2.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.helpern2.model.Service
import com.example.helpern2.ui.components.CustomButton
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.helpern2.navigation.Screen
import com.example.helpern2.utils.ImageUtils
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditScreen(navController: NavHostController, serviceId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0.0) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var isCheckingRole by remember { mutableStateOf(true) }

    val isEditMode = serviceId != "new"

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role != "admin") {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.AdminAddEdit.route) {
                            inclusive = true
                        }
                    }
                }
                isCheckingRole = false
            }
        } else {
            navController.navigate(Screen.Login.route)
        }
    }

    LaunchedEffect(serviceId) {
        if (isEditMode) {
            db.collection("services").document(serviceId).get().addOnSuccessListener { document ->
                val service = document.toObject(Service::class.java)
                service?.let {
                    name = it.name
                    category = it.category
                    price = it.price.toString()
                    description = it.description
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                    imageUrl = it.imageUrl
                    rating = it.rating
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Service" else "Add Service") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isCheckingRole) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Current Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto, 
                                contentDescription = null, 
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Click to add photo")
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹/hr)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (Optional if uploading)") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.weight(1f))

                CustomButton(
                    text = if (isEditMode) "Update Service" else "Add Service",
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        val id = if (isEditMode) serviceId else UUID.randomUUID().toString()
                        
                        val onSave = { finalImageUrl: String ->
                            val service = Service(
                                id = id,
                                name = name,
                                category = category,
                                price = price.toDoubleOrNull() ?: 0.0,
                                description = description,
                                latitude = latitude.toDoubleOrNull() ?: 0.0,
                                longitude = longitude.toDoubleOrNull() ?: 0.0,
                                imageUrl = finalImageUrl,
                                rating = rating
                            )

                            db.collection("services").document(id).set(service)
                                .addOnSuccessListener {
                                    isLoading = false
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    android.widget.Toast.makeText(context, "Failed: ${it.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                        }

                        if (selectedImageUri != null) {
                            val storageRef = storage.reference.child("service_images/$id.jpg")
                            val compressedImage = ImageUtils.compressImage(context, selectedImageUri!!)
                            
                            if (compressedImage != null) {
                                storageRef.putBytes(compressedImage)
                                    .addOnSuccessListener {
                                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                                            onSave(uri.toString())
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        android.widget.Toast.makeText(context, "Upload failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Fallback to original if compression fails
                                storageRef.putFile(selectedImageUri!!)
                                    .addOnSuccessListener {
                                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                                            onSave(uri.toString())
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        android.widget.Toast.makeText(context, "Upload failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            onSave(imageUrl)
                        }
                    }
                )
            }
        }
    }
}
