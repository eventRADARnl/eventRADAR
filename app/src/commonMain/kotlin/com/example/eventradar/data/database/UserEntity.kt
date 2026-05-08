package com.example.eventradar.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String, // Supabase UUID
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val passwordHash: String = "" // No longer stored locally for Supabase users
)
