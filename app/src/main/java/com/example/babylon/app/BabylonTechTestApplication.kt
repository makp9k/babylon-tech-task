package com.example.babylon.app

import android.app.Application
import com.example.babylon.app.di.DaggerApplicationComponent
import com.example.babylon.common.android.ComponentDependenciesProvider
import com.example.babylon.common.android.HasComponentDependencies
import com.example.babylon.posts.service.PostsService
import javax.inject.Inject

class BabylonTechTestApplication : Application() {//, HasComponentDependencies {

//    @Inject
//    override lateinit var dependencies: ComponentDependenciesProvider
//        protected set

    @Inject
    protected lateinit var postsServices: PostsService

    override fun onCreate() {
        super.onCreate()

        DaggerApplicationComponent.create().inject(this)

        println(postsServices)
    }
}
