package com.berkkanrencber.movieapp.ui.favoritepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkkanrencber.movieapp.data.repository.MovieRepository
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) : ViewModel() {
        private val _favoriteMovieList = MutableStateFlow<List<FavoriteMovie>>(emptyList())
        val favoriteMovieList: StateFlow<List<FavoriteMovie>> get() = _favoriteMovieList

        init {
            getAllFavoriteMovieList()
        }

        fun getAllFavoriteMovieList() {
            viewModelScope.launch {
                repository.getAllFavorites().collect { favorites ->
                    _favoriteMovieList.value = favorites
                }
            }
        }

        fun removeFavoriteMovie(movieId: Int) {
            viewModelScope.launch {
                repository.removeFavorite(movieId)
            }
        }
    }
