package com.example.eventradar.data.database

actual object AppDatabaseConstructor : androidx.room.RoomDatabaseConstructor<EventRadarDatabase> {
    override fun initialize(): EventRadarDatabase = throw NotImplementedError()
}
