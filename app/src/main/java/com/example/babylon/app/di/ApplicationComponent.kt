package com.example.babylon.app.di

import com.example.babylon.app.BabylonTechTestApplication
import com.example.babylon.common.android.ApplicationSchedulers
import com.example.babylon.posts.service.HttpPostsService
import com.example.babylon.posts.service.PostsService
import com.google.gson.Gson
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, SchedulersModule::class, PostsServiceModule::class])
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        fun create(): ApplicationComponent
    }

    fun inject(app: BabylonTechTestApplication)

    fun postsService(): PostsService
    fun appSchedulers(): ApplicationSchedulers
}

@Module
object NetworkModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideGson(): Gson {
        return Gson()
    }
}

@Module
object SchedulersModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideAppSchedulers() = ApplicationSchedulers(Schedulers.io(), Schedulers.computation())
}

@Module
object PostsServiceModule {
    @Provides
    @Singleton
    @JvmStatic
    fun providePostsService(okHttpClient: OkHttpClient, gson: Gson): PostsService {
        return HttpPostsService(okHttpClient, gson)
    }
}
