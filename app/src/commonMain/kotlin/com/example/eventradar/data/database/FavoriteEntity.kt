package com.example.eventradar.data.database

import androidx.room.Entity

@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "festivalId"]
)
data class FavoriteEntity(
    val userId: String,
    val festivalId: String
)
