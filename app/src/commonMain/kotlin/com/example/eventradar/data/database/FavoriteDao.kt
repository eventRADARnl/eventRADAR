package com.example.eventradar.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun removeFavorite(favorite: FavoriteEntity)

    @Query("SELECT festivalId FROM favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND festivalId = :festivalId)")
    fun isFavorite(userId: String, festivalId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites WHERE festivalId = :festivalId")
    fun getFavoriteCountForFestival(festivalId: String): Flow<Int>
}
