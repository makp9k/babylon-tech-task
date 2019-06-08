package com.example.babylon.posts.collection.di

import com.example.babylon.posts.collection.PostsCollectionScreenDependencies
import com.example.babylon.posts.collection.PostsCollectionScreenFragment
import dagger.Component

@Component(dependencies = [PostsCollectionScreenDependencies::class])
interface PostsCollectionScreenComponent {
    fun inject(screen: PostsCollectionScreenFragment)
}
