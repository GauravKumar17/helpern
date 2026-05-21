package com.example.helpern2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.helpern2.model.Service
import com.example.helpern2.model.sampleServices
import com.example.helpern2.navigation.Screen
import com.example.helpern2.ui.components.CustomRatingBar
import androidx.compose.ui.tooling.preview.Preview
import com.example.helpern2.ui.theme.Helpern2Theme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.*

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Helpern2Theme {
        HomeScreen(navController = rememberNavController())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var userRole by remember { mutableStateOf("user") }
    var userName by remember { mutableStateOf("User") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    userLocation = location
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                userRole = document.getString("role") ?: "user"
                userName = document.getString("name") ?: "User"
                profileImageUrl = document.getString("profileImageUrl")
            }
        }

        db.collection("services").addSnapshotListener { snapshot, _ ->
            isLoading = false
            services = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Service::class.java)?.copy(id = doc.id)
            } ?: emptyList()
        }
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                userLocation = location
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController, userRole, userName, profileImageUrl) {
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isSearchActive) {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search services...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search")
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("Helpern", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CategoryList(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
                
                // Merge Firestore data with sample data to prevent "hollow" cards
                val displayServices = sampleServices.map { sample ->
                    services.find { it.id == sample.id }?.let { firestore ->
                        // If firestore document is mostly empty (e.g. only rating updated), merge it with sample
                        if (firestore.name.isEmpty()) {
                            sample.copy(rating = firestore.rating)
                        } else {
                            firestore
                        }
                    } ?: sample
                } + services.filter { firestore -> 
                    // Only add NEW services from firestore if they have a name and aren't in the sample list
                    firestore.name.isNotEmpty() && sampleServices.none { it.id == firestore.id } 
                }
                
                val filteredServices = displayServices.filter { service ->
                    val categoryMatch = when (selectedCategory) {
                        "All" -> true
                        "Near You" -> {
                            if (userLocation != null) {
                                val distance = calculateDistance(
                                    userLocation!!.latitude, userLocation!!.longitude,
                                    service.latitude, service.longitude
                                )
                                distance < 10.0
                            } else false
                        }
                        else -> service.category == selectedCategory
                    }

                    val searchMatch = searchQuery.isEmpty() || 
                                     service.name.contains(searchQuery, ignoreCase = true) || 
                                     service.category.contains(searchQuery, ignoreCase = true)
                    
                    categoryMatch && searchMatch
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredServices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No services found", color = Color.Gray)
                    }
                } else {
                    ServiceGrid(
                        services = filteredServices,
                        modifier = Modifier.weight(1f),
                        onServiceClick = { serviceId ->
                            navController.navigate(Screen.ServiceDetail.createRoute(serviceId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavHostController, userRole: String, userName: String, profileImageUrl: String?, onMenuItemClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(64.dp).clip(androidx.compose.foundation.shape.CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
                Text("Welcome, $userName", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        DrawerItem(Icons.Default.Home, "Home") { 
            onMenuItemClick()
            navController.navigate(Screen.Home.route) 
        }
        DrawerItem(Icons.Default.History, "My Bookings") { 
            onMenuItemClick()
            navController.navigate(Screen.Bookings.route) 
        }
        
        if (userRole == "admin") {
            DrawerItem(Icons.Default.AdminPanelSettings, "Admin Panel") { 
                onMenuItemClick()
                navController.navigate(Screen.Admin.route) 
            }
        }

        DrawerItem(Icons.Default.Settings, "Settings") { 
            onMenuItemClick()
            navController.navigate(Screen.Settings.route) 
        }
        DrawerItem(Icons.Default.Info, "About Us") { 
            onMenuItemClick()
            navController.navigate(Screen.About.route) 
        }
        Spacer(modifier = Modifier.weight(1f))
        DrawerItem(Icons.AutoMirrored.Filled.Logout, "Logout") {
            onMenuItemClick()
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp)
    }
}

@Composable
fun CategoryList(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Near You", "Electrician", "Plumber", "Barber", "Cleaner", "Painter")
    LazyRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@Composable
fun ServiceGrid(
    services: List<Service>, 
    modifier: Modifier = Modifier,
    onServiceClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(services) { service ->
            ServiceCard(service, onServiceClick)
        }
    }
}

@Composable
fun ServiceCard(service: Service, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(service.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
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
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
                        placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = service.name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = service.category, 
                    color = Color.Gray, 
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomRatingBar(rating = service.rating) 
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = String.format(Locale.getDefault(), "%.1f", service.rating), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${service.price}/hr",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
