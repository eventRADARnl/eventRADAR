package com.example.eventradar.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun getRoomDatabase(
    builder: RoomDatabase.Builder<EventRadarDatabase>
): EventRadarDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .build()
}
