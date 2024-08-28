package com.berkkanrencber.movieapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(movie: FavoriteMovie)

    @Query("DELETE FROM favorite_movie WHERE movie_id = :movieId")
    suspend fun removeFavorite(movieId: Int)

    @Query("SELECT * FROM favorite_movie")
    fun getAllFavorites(): Flow<List<FavoriteMovie>>

    @Query("SELECT * FROM favorite_movie WHERE movie_id = :movieId LIMIT 1")
    suspend fun isFavorite(movieId: Int): FavoriteMovie?
}
