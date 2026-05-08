package com.example.eventradar.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<EventRadarDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("event_radar_database.db")
    return Room.databaseBuilder<EventRadarDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
