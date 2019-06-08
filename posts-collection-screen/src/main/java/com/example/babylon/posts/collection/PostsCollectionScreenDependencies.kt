package com.example.babylon.posts.collection

import com.example.babylon.common.android.ApplicationSchedulers
import com.example.babylon.common.android.ComponentDependencies
import com.example.babylon.posts.service.PostsService

interface PostsCollectionScreenDependencies : ComponentDependencies {

    fun postsService(): PostsService

    fun appSchedulers(): ApplicationSchedulers

    fun screenCallbacks(): PostsCollectionScreenFragment.ScreenCallbacks

}
