package com.example.babylon.app

import android.app.Application
import com.example.babylon.app.di.ApplicationComponent
import com.example.babylon.app.di.DaggerApplicationComponent
import timber.log.Timber

class BabylonTechTestApplication : Application() {
    val appComponent: ApplicationComponent = DaggerApplicationComponent.create()

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}
