package com.example.eventradar.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun removeAttendance(attendance: AttendanceEntity)

    @Query("SELECT festivalId FROM attendance WHERE userId = :userId")
    fun getAttendanceForUser(userId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM attendance WHERE festivalId = :festivalId")
    fun getAttendanceCountForFestival(festivalId: String): Flow<Int>
}
