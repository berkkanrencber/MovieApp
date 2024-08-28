package com.berkkanrencber.movieapp.ui.searchpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.data.repository.MovieRepository
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) : ViewModel() {
        private val _movieList = MutableStateFlow<List<Movie>>(emptyList())
        val movieList: StateFlow<List<Movie>> get() = _movieList

        private val _showNoInternetDialog = MutableStateFlow(false)
        val showNoInternetDialog: StateFlow<Boolean> get() = _showNoInternetDialog

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> get() = _isLoading

        private var currentPage = 1
        private var totalPages = 1

        fun getSearchedMovieList(
            query: String?,
            isNextPage: Boolean = false,
        ) {
            checkIsNextPage(isNextPage)
            _isLoading.value = true
            viewModelScope.launch {
                val response = repository.getSearchedMovieList(query, currentPage)
                response?.let {
                    totalPages = it.totalPages!!
                    _movieList.value = _movieList.value + response.results
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
            _isLoading.value = false
        }

        private fun checkIsNextPage(isNextPage: Boolean) {
            if (isNextPage) {
                currentPage++
            } else {
                currentPage = 1
                _movieList.value = emptyList()
            }
        }

        fun addFavoriteMovie(movie: FavoriteMovie) {
            viewModelScope.launch {
                repository.addFavorite(movie)
            }
        }

        fun removeFavoriteMovie(movieId: Int) {
            viewModelScope.launch {
                repository.removeFavorite(movieId)
            }
        }

        fun retryFetchingData() {
            _showNoInternetDialog.value = false
        }

        fun canLoadMore(): Boolean = currentPage < totalPages

        fun resetMovieList() {
            _movieList.value = emptyList()
        }
    }
