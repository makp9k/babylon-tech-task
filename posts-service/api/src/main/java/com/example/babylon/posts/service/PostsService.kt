package com.example.babylon.posts.service

import com.example.babylon.posts.service.entities.PostEntity
import io.reactivex.Observable

interface PostsService {

    fun getPosts(): Observable<List<PostEntity>>

}
