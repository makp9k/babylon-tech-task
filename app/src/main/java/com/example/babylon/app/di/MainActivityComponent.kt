package com.example.babylon.app.di

import com.example.babylon.app.MainActivity
import com.example.babylon.common.android.ComponentDependencies
import com.example.babylon.common.android.ComponentDependenciesKey
import com.example.babylon.common.android.ComponentScope
import com.example.babylon.posts.collection.PostsCollectionScreenDependencies
import com.example.babylon.posts.collection.PostsCollectionScreenFragment
import com.example.babylon.posts.detail.PostDetailsScreenDependencies
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap

@Component(dependencies = [ApplicationComponent::class], modules = [MainActivityComponent.ComponentDependenciesModule::class])
@ComponentScope
interface MainActivityComponent : PostsCollectionScreenDependencies, PostDetailsScreenDependencies {
    fun inject(mainActivity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance screenCallbacks: PostsCollectionScreenFragment.ScreenCallbacks
        ): MainActivityComponent
    }

    @Module
    abstract class ComponentDependenciesModule private constructor() {
        @Binds
        @IntoMap
        @ComponentDependenciesKey(PostsCollectionScreenDependencies::class)
        abstract fun providePostsCollectionScreenDependencies(component: MainActivityComponent): ComponentDependencies

        @Binds
        @IntoMap
        @ComponentDependenciesKey(PostDetailsScreenDependencies::class)
        abstract fun providePostDetailsScreenDependencies(component: MainActivityComponent): ComponentDependencies
    }
}
