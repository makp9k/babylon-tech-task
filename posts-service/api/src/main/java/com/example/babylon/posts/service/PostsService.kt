package com.example.babylon.posts.service

import com.example.babylon.common.Resource
import com.example.babylon.posts.service.entities.PostEntity
import com.gojuno.koptional.Optional
import io.reactivex.Observable

interface PostsService {

    interface Error {
        data class HttpError(val httpCode: Int, val error: Throwable) : Error
        data class ConnectionError(val error: Throwable) : Error
        data class UnknownError(val error: Throwable) : Error
        object PostNotFoundError : Error
    }

    fun getPosts(): Observable<Resource<List<PostEntity>, Error>>

    fun getPostById(id: Int): Observable<Resource<Optional<PostEntity>, Error>>

}
