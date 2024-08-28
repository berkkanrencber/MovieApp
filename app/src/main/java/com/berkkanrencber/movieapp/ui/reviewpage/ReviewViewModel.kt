package com.berkkanrencber.movieapp.ui.reviewpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkkanrencber.movieapp.data.model.review.Review
import com.berkkanrencber.movieapp.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class
ReviewViewModel@Inject
    constructor(
        private val repository: MovieRepository,
    ) : ViewModel() {
        private val _movieReviews = MutableStateFlow<List<Review>?>(emptyList())
        val movieReviews: StateFlow<List<Review>?> get() = _movieReviews

        private val _showNoInternetDialog = MutableStateFlow(false)
        val showNoInternetDialog: StateFlow<Boolean> get() = _showNoInternetDialog

        fun getMovieReviews(movieId: Int) {
            viewModelScope.launch {
                val response = repository.getMovieReviews(movieId)
                response?.let {
                    if (response != null) {
                        _movieReviews.value = response.results
                    }
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

        fun retryFetchingData(movieId: Int) {
            _showNoInternetDialog.value = false
            getMovieReviews(movieId)
        }
    }
