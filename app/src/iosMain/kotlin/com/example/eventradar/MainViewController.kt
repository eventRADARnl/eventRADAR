package com.example.eventradar

import androidx.compose.ui.window.ComposeUIViewController
import com.example.eventradar.ui.FestivalViewModel

import androidx.compose.runtime.remember
import com.example.eventradar.data.database.getDatabaseBuilder
import com.example.eventradar.data.database.getRoomDatabase
import com.example.eventradar.notifications.IosNotificationManager
import com.example.eventradar.location.IosLocationTracker

fun MainViewController() = ComposeUIViewController {
    val databaseBuilder = getDatabaseBuilder()
    val database = getRoomDatabase(databaseBuilder)
    val notificationManager = IosNotificationManager()
    val viewModel = FestivalViewModel(database, notificationManager)
    
    // Auto-start location tracking on iOS
    val locationTracker = remember { 
        IosLocationTracker { location ->
            viewModel.onUserLocationUpdated(location)
        }
    }

    App(viewModel = viewModel)
}
