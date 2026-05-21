package com.example.helpern2.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BookingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showBookingNotification(serviceName, "Reminder")
    }
}
