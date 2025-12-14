package com.app.punchinapplication.di

import android.content.Context
import androidx.room.Room
import com.app.punchinapplication.data.local.dao.PunchInDao
import com.app.punchinapplication.data.local.dao.UserDao
import com.app.punchinapplication.data.local.database.AppDatabase
import com.app.punchinapplication.data.repository.PunchInRepository
import com.app.punchinapplication.data.repository.UserRepository
import com.app.punchinapplication.util.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin Dependency Injection Module
 * Provides all dependencies needed throughout the app
 */
val appModule = module {
    
    // Database
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "punch_in_database"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()
    }
    
    // DAOs
    single<PunchInDao> { get<AppDatabase>().punchInDao() }
    single<UserDao> { get<AppDatabase>().userDao() }
    
    // Repositories
    single { PunchInRepository(get()) }
    single { UserRepository(get()) }
    
    // Session Manager
    single { SessionManager(androidContext()) }
    
    // ViewModels
    viewModel { com.app.punchinapplication.presentation.viewmodel.LoginViewModel(get(), get()) }
    viewModel { com.app.punchinapplication.presentation.viewmodel.HomeViewModel(get(), get()) }
    viewModel { com.app.punchinapplication.presentation.viewmodel.PunchInViewModel(get(), androidContext(), get()) }
    viewModel { com.app.punchinapplication.presentation.viewmodel.RouteViewModel(get(), get()) }
}

