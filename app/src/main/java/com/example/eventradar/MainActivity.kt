package com.example.eventradar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.eventradar.ui.MapScreen
import com.example.eventradar.ui.components.AdBanner
import com.example.eventradar.ui.FestivalViewModel
import com.example.eventradar.model.LatLng
import com.example.eventradar.ads.AdManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

import android.os.Build
import com.example.eventradar.notifications.AndroidNotificationManager
import androidx.lifecycle.ViewModelProvider

import com.example.eventradar.data.database.getDatabaseBuilder
import com.example.eventradar.data.database.getRoomDatabase

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adManager: AdManager
    private val viewModel: FestivalViewModel by lazy {
        val notificationManager = AndroidNotificationManager(this)
        val databaseBuilder = getDatabaseBuilder(this)
        val database = getRoomDatabase(databaseBuilder)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FestivalViewModel(database, notificationManager) as T
            }
        }
        ViewModelProvider(this, factory).get(FestivalViewModel::class.java)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getCurrentLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize AdMob
        adManager = AdManager(this)
        adManager.initialize()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        checkLocationPermission()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationRequestSignal.collect {
                    checkLocationPermission()
                }
            }
        }

        setContent {
            App(
                viewModel = viewModel, 
                mapScreen = { vm, onClick -> MapScreen(vm, onClick) },
                adBanner = { AdBanner() }
            )
        }
        
        // Zeige die Werbung nur einmal beim Start der App
        adManager.showAppOpenAd(this)
    }

    private fun checkLocationPermission() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        viewModel.onUserLocationUpdated(LatLng(it.latitude, it.longitude))
                    }
                }
        } catch (e: SecurityException) {
            // Permission lost?
        }
    }
}
