package com.example.uberrider.util

sealed class Result<T>(val status: Status, val data: T? = null, val errorMessage: String? = null) {
    class Success<T>(data:T, errorMessage: String? = null): Result<T>(Status.SUCCESS,data,errorMessage)
    class Error<T>(data: T? = null, errorMessage: String): Result<T>(Status.ERROR,data, errorMessage)
    class Loading<T>(data: T? = null, errorMessage: String? = null): Result<T>(Status.LOADING, data, errorMessage)
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}