package com.berkkanrencber.movieapp.utils

sealed class Result<out T> {
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val errorCode: Int,
        val errorMessage: String,
    ) : Result<Nothing>()
}
