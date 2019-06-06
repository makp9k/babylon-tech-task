package com.example.babylon.app.di

import com.example.babylon.posts.service.HttpPostsService
import com.example.babylon.posts.service.PostsService
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class PostsServiceModule {

    @Binds
    @Singleton
    abstract fun providePostsService(httpPostsService: HttpPostsService): PostsService

}
