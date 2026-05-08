package com.example.eventradar.model

data class TimetableEntry(
    val startTime: String,
    val endTime: String,
    val artist: String,
    val stage: String
)

data class Festival(
    val id: String,
    val name: String,
    val date: String,
    val duration: String,
    val time: String = "",
    val position: LatLng,
    val locationName: String,
    val ticketUrl: String,
    val lineUpUrl: String,
    val timetableUrl: String = "",
    val floorplanUrl: String = "",
    val imageUrl: String,
    val listImageUrl: String,
    val isWeekend: Boolean,
    val minAge: Int,
    val genre: String,
    val isOutdoor: Boolean,
    val organizer: String = "Unbekannt",
    val description: String = "",
    val lineUp: List<String> = emptyList(),
    val lineUpByDay: Map<String, Map<String, List<String>>> = emptyMap(),
    val timetable: Map<String, List<TimetableEntry>> = emptyMap()
)
