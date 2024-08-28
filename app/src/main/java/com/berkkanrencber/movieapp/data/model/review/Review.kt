package com.berkkanrencber.movieapp.data.model.review

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("author") val author: String,
    @SerializedName("content") val content: String,
    @SerializedName("id") val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("author_details") val authorDetails: AuthorDetail,
)
