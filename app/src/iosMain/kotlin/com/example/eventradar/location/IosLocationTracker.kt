package com.example.eventradar.location

import com.example.eventradar.model.LatLng
import platform.CoreLocation.*
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class IosLocationTracker(
    private val onLocationUpdated: (LatLng) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {
    
    private val locationManager = CLLocationManager()

    init {
        locationManager.delegate = this
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        val latLng = LatLng(
            latitude = location.coordinate.useContents { latitude },
            longitude = location.coordinate.useContents { longitude }
        )
        onLocationUpdated(latLng)
    }

    fun requestSingleLocation() {
        locationManager.requestLocation()
    }
}
