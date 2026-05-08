package com.example.eventradar.notifications

interface NotificationManager {
    fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        timeInMillis: Long
    )
    fun cancelNotification(id: Int)
}
