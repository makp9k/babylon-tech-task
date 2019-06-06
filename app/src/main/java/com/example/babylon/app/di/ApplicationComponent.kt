package com.example.babylon.app.di

import com.example.babylon.app.BabylonTechTestApplication
import com.example.babylon.common.android.ComponentDependencies
import com.example.babylon.common.android.ComponentDependenciesKey
import com.example.babylon.posts.service.PostsService
import dagger.Binds
import dagger.Component
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Singleton
@Component(modules = [HttpModule::class, PostsServiceModule::class, ComponentDependenciesModule::class])
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        fun create(): ApplicationComponent
    }

    fun inject(app: BabylonTechTestApplication)
}

@dagger.Module
private abstract class ComponentDependenciesModule private constructor() {
//    @Binds
//    @IntoMap
//    @ComponentDependenciesKey(PostsService::class)
//    abstract fun provideStartScreenDependencies(component: ApplicationComponent): ComponentDependencies
}
