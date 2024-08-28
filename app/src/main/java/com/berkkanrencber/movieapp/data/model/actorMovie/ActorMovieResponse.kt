package com.berkkanrencber.movieapp.data.model.actorMovie

import com.google.gson.annotations.SerializedName

data class ActorMovieResponse(
    @SerializedName("cast") val cast: List<ActorMovie>?,
    @SerializedName("id") val id: Int,
)
