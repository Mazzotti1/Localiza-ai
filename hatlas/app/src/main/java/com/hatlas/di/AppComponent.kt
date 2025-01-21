package com.hatlas.di

import com.hatlas.ui.screen.RegisterActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: RegisterActivity)
}