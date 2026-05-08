package com.example.eventradar.data.database

import androidx.room.Entity

@Entity(
    tableName = "followed_artists",
    primaryKeys = ["userId", "artistName"]
)
data class FollowedArtistEntity(
    val userId: String,
    val artistName: String
)
