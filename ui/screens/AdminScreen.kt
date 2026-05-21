package com.example.helpern2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import coil.request.ImageRequest
import com.example.helpern2.model.Service
import com.example.helpern2.model.sampleServices
import com.example.helpern2.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var isSeeding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                userRole = document.getString("role")
                if (userRole != "admin") {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Admin.route) {
                            inclusive = true
                        }
                    }
                }
            }
        } else {
            navController.navigate(Screen.Login.route)
        }

        db.collection("services")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    services = snapshot.toObjects(Service::class.java)
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSeeding = true
                        scope.launch {
                            val batch = db.batch()
                            sampleServices.forEach { service ->
                                val docRef = db.collection("services").document(service.id)
                                batch.set(docRef, service)
                            }
                            batch.commit().addOnCompleteListener { isSeeding = false }
                        }
                    }) {
                        if (isSeeding) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Seed/Reset Data")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AdminAddEdit.createRoute("new")) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Merge Firestore services with samples, prioritizing Firestore versions
            val displayServices = (services + sampleServices).distinctBy { it.id }
            
            Column(modifier = Modifier.padding(padding)) {
                if (services.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Showing offline samples. Use the sync icon in the top bar to add all samples to Firestore, or add them individually below.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayServices) { service ->
                        val isSample = services.none { it.id == service.id }
                        AdminServiceCard(
                            service = service,
                            isSample = isSample,
                            onEdit = { navController.navigate(Screen.AdminAddEdit.createRoute(service.id)) },
                            onDelete = {
                                db.collection("services").document(service.id).delete()
                            },
                            onAddToCloud = {
                                db.collection("services").document(service.id).set(service)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminServiceCard(
    service: Service, 
    isSample: Boolean, 
    onEdit: () -> Unit, 
    onDelete: () -> Unit,
    onAddToCloud: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isSample) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) 
                 else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (service.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(service.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = service.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
                        placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        service.name, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (isSample) {
                        Surface(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("OFFLINE", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
                Text(service.category, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                Text("₹${service.price}/hr", fontSize = 14.sp)
            }
            Row {
                if (isSample) {
                    IconButton(onClick = onAddToCloud) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Add to Cloud", tint = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}
