package com.ben.inly

import android.app.Application
import com.ben.inly.di.androidModule
import com.ben.inly.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InlyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@InlyApplication)
            modules(sharedModule, androidModule)
        }
    }
}