package com.example.babylon.posts.detail

import com.example.babylon.common.android.ApplicationSchedulers
import com.example.babylon.common.android.ComponentDependencies
import com.example.babylon.posts.service.PostsService

interface PostDetailsScreenDependencies : ComponentDependencies {

    fun getPostsService(): PostsService

    fun appSchedulers(): ApplicationSchedulers

}
