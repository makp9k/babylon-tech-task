package com.example.babylon.posts.service.models

import com.google.gson.annotations.SerializedName

data class CommentModel(
    @SerializedName("postId") val postId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("body") val body: String
)
