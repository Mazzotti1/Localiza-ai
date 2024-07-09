package com.localizaai.di

import com.localizaai.ui.screen.MainActivity
import com.localizaai.ui.screen.RegisterActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: RegisterActivity)
}