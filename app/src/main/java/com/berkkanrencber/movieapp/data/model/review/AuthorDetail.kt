package com.berkkanrencber.movieapp.data.model.review

import com.google.gson.annotations.SerializedName

data class AuthorDetail(
    @SerializedName("username") val username: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("avatar_path") val avatarPath: String,
)
