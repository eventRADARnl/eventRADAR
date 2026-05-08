package com.example.eventradar.data.database

import androidx.room.Entity

@Entity(
    tableName = "attendance",
    primaryKeys = ["userId", "festivalId"]
)
data class AttendanceEntity(
    val userId: String,
    val festivalId: String
)
