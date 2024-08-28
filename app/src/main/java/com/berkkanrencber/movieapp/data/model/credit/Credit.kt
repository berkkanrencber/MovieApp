package com.berkkanrencber.movieapp.data.model.credit

import com.google.gson.annotations.SerializedName

data class Credit(
    @SerializedName("cast") val cast: List<Cast>,
    @SerializedName("id") val id: Int
)
