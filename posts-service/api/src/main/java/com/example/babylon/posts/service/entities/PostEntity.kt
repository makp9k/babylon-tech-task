package com.example.babylon.posts.service.entities

data class PostEntity(
    val id: Int,
    val title: String,
    val body: String,
    val author: UserEntity,
    val comments: List<CommentEntity>
)
