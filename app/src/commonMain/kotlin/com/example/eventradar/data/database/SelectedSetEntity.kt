package com.example.eventradar.data.database

import androidx.room.Entity

@Entity(
    tableName = "selected_sets",
    primaryKeys = ["userId", "festivalId", "artist", "startTime", "stage"]
)
data class SelectedSetEntity(
    val userId: String,
    val festivalId: String,
    val artist: String,
    val startTime: String,
    val stage: String
)
