package com.example.babylon.posts.service

import com.example.babylon.common.Resource
import com.example.babylon.posts.service.entities.PostEntity
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Observable

class PostsServiceStub(
    private val posts: List<PostEntity> = emptyList(),
    private val error: PostsService.Error? = null
) : PostsService {

    override fun getPosts(): Observable<Resource<List<PostEntity>, PostsService.Error>> {
        return Observable.just(
            Resource.Progress,
            if (error != null)
                Resource.Failure(error) else
                Resource.Result(posts)
        )
    }

    override fun getPostById(id: Int): Observable<Resource<Optional<PostEntity>, PostsService.Error>> {
        return Observable.just(
            Resource.Progress,
            if (error != null)
                Resource.Failure(error) else
                Resource.Result(posts.firstOrNull { it.id == id }.toOptional())
        )
    }
}
