package com.example.babylon.posts.service

import okhttp3.OkHttpClient

interface HttpPostsServiceDependencies {

    fun okHttpClient(): OkHttpClient

}
