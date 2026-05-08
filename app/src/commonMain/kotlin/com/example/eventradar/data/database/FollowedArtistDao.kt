package com.example.eventradar.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowedArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followArtist(artist: FollowedArtistEntity)

    @Delete
    suspend fun unfollowArtist(artist: FollowedArtistEntity)

    @Query("SELECT artistName FROM followed_artists WHERE userId = :userId")
    fun getFollowedArtists(userId: String): Flow<List<String>>
}
