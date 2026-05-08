package com.example.eventradar.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<EventRadarDatabase> {
    val dbFilePath = NSHomeDirectory() + "/event_radar_database.db"
    return Room.databaseBuilder<EventRadarDatabase>(
        name = dbFilePath,
        factory =  { AppDatabaseConstructor.initialize() }
    )
}

actual object AppDatabaseConstructor : androidx.room.RoomDatabaseConstructor<EventRadarDatabase> {
    actual override fun initialize(): EventRadarDatabase = throw NotImplementedError()
}
