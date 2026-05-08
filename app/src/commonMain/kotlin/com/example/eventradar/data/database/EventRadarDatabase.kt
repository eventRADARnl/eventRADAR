package com.example.eventradar.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, FavoriteEntity::class, AttendanceEntity::class, FollowedArtistEntity::class, SelectedSetEntity::class],
    version = 6,
    exportSchema = false
)
abstract class EventRadarDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun followedArtistDao(): FollowedArtistDao
    abstract fun selectedSetDao(): SelectedSetDao
}
