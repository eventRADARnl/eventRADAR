package com.example.eventradar.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SelectedSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSelectedSet(set: SelectedSetEntity)

    @Delete
    suspend fun removeSelectedSet(set: SelectedSetEntity)

    @Query("SELECT * FROM selected_sets WHERE userId = :userId AND festivalId = :festivalId")
    fun getSelectedSets(userId: String, festivalId: String): Flow<List<SelectedSetEntity>>

    @Query("SELECT * FROM selected_sets WHERE userId = :userId")
    fun getAllSelectedSets(userId: String): Flow<List<SelectedSetEntity>>
}
