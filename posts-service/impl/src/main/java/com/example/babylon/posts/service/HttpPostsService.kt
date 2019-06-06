package com.example.babylon.posts.service

import com.example.babylon.posts.service.entities.PostEntity
import io.reactivex.Observable
import okhttp3.OkHttpClient
import javax.inject.Inject

class HttpPostsService @Inject constructor() : PostsService {
    override fun getPosts(): Observable<List<PostEntity>> {

        return Observable.just(
            IntRange(0, 10).map {
                PostEntity(
                    "id$it",
                    "Title $it"
                )
            }
        )
    }
}
