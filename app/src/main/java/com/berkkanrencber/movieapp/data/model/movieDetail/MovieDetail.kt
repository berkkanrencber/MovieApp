package com.berkkanrencber.movieapp.data.model.movieDetail

import com.google.gson.annotations.SerializedName

data class MovieDetail(
    @SerializedName("adult") val adult: Boolean?,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName("id") val id: Int?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("runtime") val runtime: Double,
    @SerializedName("poster_path") val posterPath: String,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("video") val video: Boolean,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int,
)
