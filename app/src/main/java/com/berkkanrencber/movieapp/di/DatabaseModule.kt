package com.berkkanrencber.movieapp.di

import android.content.Context
import androidx.room.Room
import com.berkkanrencber.movieapp.data.repository.MovieRepository
import com.berkkanrencber.movieapp.data.room.MovieDao
import com.berkkanrencber.movieapp.data.room.MovieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MovieDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MovieDatabase::class.java,
            "movie_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(database: MovieDatabase): MovieDao {
        return database.movieDao()
    }
}