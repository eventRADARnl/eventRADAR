package com.example.eventradar.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [UserEntity::class, FavoriteEntity::class, AttendanceEntity::class, FollowedArtistEntity::class, SelectedSetEntity::class],
    version = 6,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class EventRadarDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun followedArtistDao(): FollowedArtistDao
    abstract fun selectedSetDao(): SelectedSetDao
}

// Room KMP constructor requirement
expect object AppDatabaseConstructor : RoomDatabaseConstructor<EventRadarDatabase>
