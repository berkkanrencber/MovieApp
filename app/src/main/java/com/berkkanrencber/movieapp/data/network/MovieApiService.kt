package com.berkkanrencber.movieapp.data.network

import com.berkkanrencber.movieapp.data.model.actor.Actor
import com.berkkanrencber.movieapp.data.model.actorMovie.ActorMovieResponse
import com.berkkanrencber.movieapp.data.model.credit.Credit
import com.berkkanrencber.movieapp.data.model.movie.MovieResponse
import com.berkkanrencber.movieapp.data.model.movieDetail.MovieDetail
import com.berkkanrencber.movieapp.data.model.movieImage.MovieImage
import com.berkkanrencber.movieapp.data.model.movieSearch.MovieSearchResponse
import com.berkkanrencber.movieapp.data.model.movieTrailer.TrailerResponse
import com.berkkanrencber.movieapp.data.model.review.ReviewResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    suspend fun getPopularMovieList(
        @Query("page") page: Int,
    ): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovieList(
        @Query("page") page: Int,
    ): MovieResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovieList(
        @Query("page") page: Int,
    ): MovieResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovieList(
        @Query("page") page: Int,
    ): MovieResponse

    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: Int,
    ): MovieImage

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
    ): MovieDetail

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String?,
        @Query("page") page: Int?,
    ): MovieSearchResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int
    ): Credit

    @GET("person/{person_id}")
    suspend fun getActorDetail(
        @Path("person_id") personId: Int,
    ): Actor

    @GET("person/{person_id}/movie_credits")
    suspend fun getActorMovies(
        @Path("person_id") personId: Int,
    ): ActorMovieResponse

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
    ): ReviewResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieTrailers(
        @Path("movie_id") movieId: Int,
    ): TrailerResponse

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
    ): MovieResponse
}
