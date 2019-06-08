package com.example.babylon.posts.detail.di

import com.example.babylon.posts.detail.PostDetailsScreenDependencies
import com.example.babylon.posts.detail.PostDetailsScreenFragment
import com.example.babylon.posts.detail.PostDetailsScreenViewModel
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [PostDetailsScreenDependencies::class])
interface PostDetailsScreenComponent {
    fun inject(screen: PostDetailsScreenFragment)

    @Component.Factory
    interface Factory {
        fun create(
            postDetailsScreenDependencies: PostDetailsScreenDependencies,
            @BindsInstance screenParams: PostDetailsScreenViewModel.ScreenParams
        ): PostDetailsScreenComponent
    }
}
