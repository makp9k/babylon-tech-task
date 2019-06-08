package com.example.babylon.posts.service

import com.example.babylon.posts.service.models.CommentModel
import com.example.babylon.posts.service.models.PostModel
import com.example.babylon.posts.service.models.UserModel
import io.reactivex.Single
import retrofit2.http.GET

interface PostsRemoteApi {

    @GET("/posts")
    fun loadPosts(): Single<List<PostModel>>

    @GET("/users")
    fun loadUsers(): Single<List<UserModel>>

    @GET("/comments")
    fun loadComments(): Single<List<CommentModel>>

}
