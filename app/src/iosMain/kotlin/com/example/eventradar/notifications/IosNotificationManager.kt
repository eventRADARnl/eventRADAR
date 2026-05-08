package com.example.eventradar.notifications

import platform.UserNotifications.*
import platform.Foundation.*

class IosNotificationManager : NotificationManager {
    
    init {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (error != null) {
                // Handle error
            }
        }
    }

    override fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        timeInMillis: Long
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound())
        }

        val now = NSDate().timeIntervalSince1970 * 1000
        val delay = (timeInMillis - now.toLong()) / 1000.0
        
        if (delay > 0) {
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delay, false)
            val request = UNNotificationRequest.requestWithIdentifier(id.toString(), content, trigger)
            
            UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
                if (error != null) {
                    // Handle error
                }
            }
        }
    }

    override fun cancelNotification(id: Int) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(id.toString()))
    }
}
