package com.example.helpern2.ui.screens

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.os.Build
import android.content.Context
import android.content.Intent
import com.example.helpern2.utils.BookingReminderReceiver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.helpern2.model.sampleServices
import com.example.helpern2.ui.components.CustomButton
import com.example.helpern2.ui.components.CustomRatingBar
import java.util.Locale
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(navController: NavHostController, serviceId: String) {
    val context = LocalContext.current
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    var service by remember { mutableStateOf<com.example.helpern2.model.Service?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val calendar = remember { Calendar.getInstance() }

    LaunchedEffect(serviceId) {
        // First check samples
        val sample = sampleServices.find { it.id == serviceId }
        if (sample != null) {
            service = sample
        }
        
        // Then try to fetch from Firestore (overwrites sample if exists)
        db.collection("services").document(serviceId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                service = document.toObject(com.example.helpern2.model.Service::class.java)
            }
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }

    var selectedDate by remember { mutableStateOf("Select Date") }
    var selectedTime by remember { mutableStateOf("Select Time") }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentService = service ?: run {
        Text("Service not found")
        return
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentService.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentService.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentService.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
                        placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
                    )
                } else {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = currentService.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = currentService.category, color = Color.Gray, fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomRatingBar(rating = currentService.rating)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${String.format(Locale.getDefault(), "%.1f", currentService.rating)})",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "About", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = currentService.description,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Book Appointment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedCard(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = selectedDate)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = selectedTime)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            val notificationHelper = com.example.helpern2.utils.NotificationHelper(context)
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

            CustomButton(
                text = "Confirm Booking - ₹${currentService.price}",
                onClick = {
                    if (selectedDate == "Select Date" || selectedTime == "Select Time") {
                        android.widget.Toast.makeText(context, "Please select date and time", android.widget.Toast.LENGTH_SHORT).show()
                        return@CustomButton
                    }

                    val booking = hashMapOf(
                        "serviceId" to currentService.id,
                        "serviceName" to currentService.name,
                        "category" to currentService.category,
                        "date" to selectedDate,
                        "time" to selectedTime,
                        "userId" to (auth.currentUser?.uid ?: "anonymous"),
                        "status" to "Confirmed",
                        "price" to currentService.price,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("bookings")
                        .add(booking)
                        .addOnSuccessListener {
                            notificationHelper.showBookingNotification(currentService.name)
                            scheduleAlarm(context, currentService.name, calendar)
                            android.widget.Toast.makeText(context, "Booking Successful!", android.widget.Toast.LENGTH_LONG).show()
                            navController.navigate(com.example.helpern2.navigation.Screen.Home.route) {
                                popUpTo(com.example.helpern2.navigation.Screen.Home.route) { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            android.widget.Toast.makeText(context, "Booking Failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                }
            )
        }
    }
}

private fun scheduleAlarm(context: Context, serviceName: String, calendar: Calendar) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, BookingReminderReceiver::class.java).apply {
        putExtra("SERVICE_NAME", serviceName)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Using the selected calendar time for the reminder
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    } catch (e: SecurityException) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
