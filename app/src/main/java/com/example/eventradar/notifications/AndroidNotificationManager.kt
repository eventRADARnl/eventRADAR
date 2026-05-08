package com.example.eventradar.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNM
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.eventradar.MainActivity
import com.example.eventradar.R

class AndroidNotificationManager(private val context: Context) : NotificationManager {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleNotification(id: Int, title: String, message: String, timeInMillis: Long) {
        if (timeInMillis < System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    override fun cancelNotification(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "Event Start"
        val message = intent.getStringExtra("message") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNM
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("event_radar_alerts", "Event Alerts", AndroidNM.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "event_radar_alerts")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Falls nicht da, muss man schauen
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }
}
