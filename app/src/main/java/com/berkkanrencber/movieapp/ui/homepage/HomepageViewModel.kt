package com.berkkanrencber.movieapp.ui.homepage

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
class HomepageViewModel
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) : ViewModel() {
        private val _topRatedMovieList = MutableStateFlow<List<Movie>>(emptyList())
        val topRatedMovieList: StateFlow<List<Movie>> get() = _topRatedMovieList

        private val _popularMovieList = MutableStateFlow<List<Movie>>(emptyList())
        val popularMovieList: StateFlow<List<Movie>> get() = _popularMovieList

        private val _upcomingMovieList = MutableStateFlow<List<Movie>>(emptyList())
        val upcomingMovieList: StateFlow<List<Movie>> get() = _upcomingMovieList

        private val _nowPlayingMovieList = MutableStateFlow<List<Movie>>(emptyList())
        val nowPlayingMovieList: StateFlow<List<Movie>> get() = _nowPlayingMovieList

        private val _searchMovieList = MutableStateFlow<List<Movie>>(emptyList())
        val searchMovieList: StateFlow<List<Movie>> get() = _searchMovieList

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> get() = _isLoading

        private val _showNoInternetDialog = MutableStateFlow(false)
        val showNoInternetDialog: StateFlow<Boolean> get() = _showNoInternetDialog

        private var currentPage = 1
        private var totalPages = 1

        fun getTopRatedMovieList(page: Int) {
            viewModelScope.launch {
                val response = repository.getTopRatedMovieList(page)
                response?.let {
                    val updatedList =
                        _topRatedMovieList.value.toMutableList().apply {
                            addAll(response)
                        }
                    if (_topRatedMovieList.value != updatedList) {
                        _topRatedMovieList.value = updatedList
                    }
                } ?: run {
                    if (page <= 500) {
                        _showNoInternetDialog.value = true
                    }
                }
            }
        }

        fun getPopularMovieList(page: Int) {
            viewModelScope.launch {
                val response = repository.getPopularMovieList(page)
                response?.let {
                    val updatedList =
                        _popularMovieList.value.toMutableList().apply {
                            addAll(response)
                        }
                    if (_popularMovieList.value != updatedList) {
                        _popularMovieList.value = updatedList
                    }
                } ?: run {
                    if (page <= 500) {
                        _showNoInternetDialog.value = true
                    }
                }
            }
        }

        fun getUpcomingMovieList(page: Int) {
            viewModelScope.launch {
                val response = repository.getUpcomingMovieList(page)
                response?.let {
                    val updatedList =
                        _upcomingMovieList.value.toMutableList().apply {
                            addAll(response)
                        }
                    if (_upcomingMovieList.value != updatedList) {
                        _upcomingMovieList.value = updatedList
                    }
                } ?: run {
                    if (page < 500) {
                        _showNoInternetDialog.value = true
                    }
                }
            }
        }

        fun getNowPlayingMovieList(page: Int) {
            viewModelScope.launch {
                val response = repository.getNowPlayingMovieList(page)
                response?.let {
                    val updatedList =
                        _nowPlayingMovieList.value.toMutableList().apply {
                            addAll(response)
                        }
                    if (_nowPlayingMovieList.value != updatedList) {
                        _nowPlayingMovieList.value = updatedList
                    }
                } ?: run {
                    if (page < 500) {
                        _showNoInternetDialog.value = true
                    }
                }
            }
        }

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
                    _searchMovieList.value = _searchMovieList.value + response.results
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
                _searchMovieList.value = emptyList()
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

        fun retryTopRatedFetchingData(page: Int) {
            _showNoInternetDialog.value = false
            getTopRatedMovieList(page)
        }

        fun retryPopularFetchingData(page: Int) {
            _showNoInternetDialog.value = false
            getPopularMovieList(page)
        }

        fun retryUpcomingFetchingData(page: Int) {
            _showNoInternetDialog.value = false
            getUpcomingMovieList(page)
        }

        fun retryNowPlayingFetchingData(page: Int) {
            _showNoInternetDialog.value = false
            getNowPlayingMovieList(page)
        }

        fun retryFetchingData() {
            _showNoInternetDialog.value = false
        }

        fun canLoadMore(): Boolean = currentPage < totalPages

        fun resetMovieList() {
            _searchMovieList.value = emptyList()
        }
    }
