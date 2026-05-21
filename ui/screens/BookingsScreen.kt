package com.example.helpern2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.navigation.NavHostController
import com.example.helpern2.utils.NotificationHelper
import com.example.helpern2.model.Booking
import com.example.helpern2.ui.components.CustomRatingBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Active", "Past")
    
    val notificationHelper = remember { NotificationHelper(context) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        bookings = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Booking::class.java)?.copy(id = doc.id)
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        BookingList(
                            bookings = bookings.filter { it.status == "Confirmed" },
                            onComplete = { booking ->
                                if (booking.id.isNotEmpty()) {
                                    db.collection("bookings").document(booking.id)
                                        .update("status", "Completed")
                                        .addOnSuccessListener {
                                            notificationHelper.showBookingNotification(booking.serviceName, "Completed")
                                            Toast.makeText(context, "Marked as Completed", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )
                    } else {
                        BookingList(
                            bookings = bookings.filter { it.status == "Completed" },
                            isPast = true,
                            onRate = { booking, rating ->
                                if (booking.id.isNotEmpty()) {
                                    db.collection("bookings").document(booking.id).update("rating", rating)
                                        .addOnSuccessListener {
                                            updateServiceRating(db, booking.serviceId)
                                            Toast.makeText(context, "Rating updated", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun updateServiceRating(db: FirebaseFirestore, serviceId: String) {
    db.collection("bookings")
        .whereEqualTo("serviceId", serviceId)
        .get()
        .addOnSuccessListener { snapshot ->
            val ratings = snapshot.documents.mapNotNull { it.get("rating")?.toString()?.toDouble() }
            if (ratings.isNotEmpty()) {
                val avgRating = ratings.average()
                db.collection("services").document(serviceId).set(
                    mapOf("rating" to avgRating),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
        }
}

@Composable
fun BookingList(
    bookings: List<Booking>,
    isPast: Boolean = false,
    onRate: (Booking, Double) -> Unit = { _, _ -> },
    onComplete: (Booking) -> Unit = {}
) {
    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                if (isPast) "No past bookings" else "No active bookings",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bookings) { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(booking.category, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(
                                text = "₹${booking.price}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Date: ${booking.date}", fontSize = 14.sp)
                        Text("Time: ${booking.time}", fontSize = 14.sp)
                        
                        if (isPast) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Rate your experience:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomRatingBar(
                                rating = booking.rating ?: 0.0,
                                clickable = true,
                                onRatingChanged = { newRating ->
                                    onRate(booking, newRating)
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onComplete(booking) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mark as Completed")
                            }
                        }
                    }
                }
            }
        }
    }
}
