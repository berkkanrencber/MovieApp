package com.berkkanrencber.movieapp.ui.detailpage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkkanrencber.movieapp.data.model.actor.Actor
import com.berkkanrencber.movieapp.data.model.actorMovie.ActorMovie
import com.berkkanrencber.movieapp.data.model.credit.Cast
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.data.model.movieDetail.MovieDetail
import com.berkkanrencber.movieapp.data.model.movieImage.Backdrops
import com.berkkanrencber.movieapp.data.model.movieTrailer.Trailer
import com.berkkanrencber.movieapp.data.model.review.Review
import com.berkkanrencber.movieapp.data.repository.MovieRepository
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) : ViewModel() {
        private val _movieDetails = MutableLiveData<MovieDetail?>()
        val movieDetails: MutableLiveData<MovieDetail?> get() = _movieDetails

        private val _movieImageList = MutableStateFlow<List<Backdrops>?>(emptyList())
        val movieImageList: StateFlow<List<Backdrops>?> get() = _movieImageList

        private val _movieCredits = MutableStateFlow<List<Cast>>(emptyList())
        val movieCredits: StateFlow<List<Cast>> get() = _movieCredits

        private val _actorDetails = MutableLiveData<Actor?>()
        val actorDetails: MutableLiveData<Actor?> get() = _actorDetails

        private val _actorMovies = MutableStateFlow<List<ActorMovie>?>(emptyList())
        val actorMovies: StateFlow<List<ActorMovie>?> get() = _actorMovies

        private val _similarMovies = MutableStateFlow<List<Movie>?>(emptyList())
        val similarMovies: StateFlow<List<Movie>?> get() = _similarMovies

        private val _movieReviews = MutableStateFlow<List<Review>?>(emptyList())
        val movieReviews: StateFlow<List<Review>?> get() = _movieReviews

        private val _movieTrailers = MutableStateFlow<List<Trailer>?>(emptyList())
        val movieTrailers: StateFlow<List<Trailer>?> get() = _movieTrailers

        private val _showNoInternetDialog = MutableStateFlow(false)
        val showNoInternetDialog: StateFlow<Boolean> get() = _showNoInternetDialog

        fun getMovieDetail(movieId: Int) {
            viewModelScope.launch {
                val response = repository.getMovieDetail(movieId)
                response?.let {
                    if (response != null) {
                        _movieDetails.value = response
                    }
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

        fun getMovieImages(movieId: Int) {
            viewModelScope.launch {
                val response = repository.getMovieImages(movieId)
                response?.let {
                    _movieImageList.value = response.backdrops
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

        fun getMovieCast(movieId: Int) {
            viewModelScope.launch {
                val response = repository.getMovieCredits(movieId)
                response?.let {
                    if (response != null) {
                        _movieCredits.value = response.cast
                    }
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

        fun getActorDetail(personId: Int) {
            viewModelScope.launch {
                val response = repository.getActorDetail(personId)
                response?.let {
                    if (response != null) {
                        _actorDetails.value = response
                    }
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

        fun getActorMovies(personId: Int) {
            viewModelScope.launch {
                val response = repository.getActorMovies(personId)
                response?.let {
                    if (response != null) {
                        _actorMovies.value = response.cast
                    }
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

        fun getSimilarMovies(movieId: Int) {
            viewModelScope.launch {
                val response = repository.getSimilarMovies(movieId)
                response?.let {
                    _similarMovies.value = response.results
                } ?: run {
                    _showNoInternetDialog.value = true
                }
            }
        }

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

        fun getMovieTrailer(movieId: Int) {
            viewModelScope.launch {
                val response = repository.getMovieTrailer(movieId)
                response?.let {
                    _movieTrailers.value = response.results
                } ?: run {
                    _showNoInternetDialog.value = true
                }
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

        suspend fun isFavorite(movieId: Int): Boolean = repository.isFavorite(movieId) != null

        private fun changeDialogValue() {
            _showNoInternetDialog.value = false
        }

        fun retryDetailPageData(movieId: Int) {
            changeDialogValue()
            getMovieDetail(movieId)
            getMovieImages(movieId)
            getMovieCast(movieId)
            getSimilarMovies(movieId)
            getMovieReviews(movieId)
            getMovieTrailer(movieId)
        }

        fun retryActorDetailPageData(personId: Int) {
            changeDialogValue()
            getActorDetail(personId)
            getActorMovies(personId)
        }
    }
