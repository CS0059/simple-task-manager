package com.example.myapplication

import android.app.Application
import android.content.Context
import com.example.myapplication.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.Locale

class TodoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", "tr")
        updateLocale(savedLanguage ?: "tr")
        
        startKoin {
            androidLogger()
            androidContext(this@TodoApp)
            modules(appModule)
        }
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
    }
} 