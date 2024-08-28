package com.berkkanrencber.movieapp.data.model.actorMovie

import com.google.gson.annotations.SerializedName

data class ActorMovie(
    @SerializedName("id") val id: Int?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("character") val character: String?,
)
