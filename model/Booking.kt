package com.example.helpern2.model

import com.google.firebase.Timestamp

data class Booking(
    val id: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val category: String = "",
    val date: String = "",
    val time: String = "",
    val userId: String = "",
    val status: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val price: Double = 0.0,
    val rating: Double? = null
)
