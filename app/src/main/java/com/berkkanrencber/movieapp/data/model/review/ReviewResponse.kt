package com.berkkanrencber.movieapp.data.model.review

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("results") val results: List<Review>,
)
