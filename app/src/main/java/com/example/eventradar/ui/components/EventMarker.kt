package com.example.eventradar.ui.components

import androidx.compose.runtime.Composable
import com.example.eventradar.model.Festival
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun EventMarker(
    festival: Festival,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState(
        position = GoogleLatLng(
            festival.position.latitude,
            festival.position.longitude
        )
    )
    Marker(
        state = markerState,
        title = festival.name,
        onClick = {
            false // Returning false allows the default behavior: show the info window
        },
        onInfoWindowClick = {
            onClick()
        }
    )
}
