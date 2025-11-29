package com.example.myapplication.di

import androidx.room.Room
import com.example.myapplication.data.local.TodoDatabase
import com.example.myapplication.data.repository.TodoRepositoryImpl
import com.example.myapplication.domain.repository.TodoRepository
import com.example.myapplication.domain.usecase.TodoUseCases
import com.example.myapplication.presentation.TodoViewModel
import com.example.myapplication.util.SoundManager
import com.example.myapplication.util.TodoNotificationManager
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SoundManager(androidApplication()) }

    single {
        Room.databaseBuilder(
            androidApplication(),
            TodoDatabase::class.java,
            "todo_database"
        ).build()
    }

    single { get<TodoDatabase>().todoDao }

    single<TodoRepository> { TodoRepositoryImpl(get()) }

    single { TodoUseCases(get()) }

    single { TodoNotificationManager(get()) }

    viewModel {
        TodoViewModel(
            todoUseCases = get(),
            soundManager = get(),
            notificationManager = get(),
            application = androidApplication()
        )
    }
} 