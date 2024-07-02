package com.ecoheat.di

import com.ecoheat.ui.screen.MainActivity
import com.ecoheat.ui.screen.RegisterActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: RegisterActivity)
}