package com.example.babylon.common

sealed class Resource<out Result, out Error> {

    object Progress : Resource<Nothing, Nothing>()

    data class Result<Result>(val data: Result) : Resource<Result, Nothing>()

    data class Failure<Error>(val error: Error) : Resource<Nothing, Error>()

}
