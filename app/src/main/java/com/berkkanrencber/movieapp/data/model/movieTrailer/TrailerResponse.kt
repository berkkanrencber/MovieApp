package com.berkkanrencber.movieapp.data.model.movieTrailer

import com.google.gson.annotations.SerializedName

data class TrailerResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("results") val results: List<Trailer>?
)

