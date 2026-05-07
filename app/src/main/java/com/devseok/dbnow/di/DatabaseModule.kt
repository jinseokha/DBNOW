package com.devseok.dbnow.di

import android.content.Context
import androidx.room.Room
import com.devseok.dbnow.data.local.AppDatabase
import com.devseok.dbnow.data.local.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. Room 데이터베이스 인스턴스 제공
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dbnow_database" // 원하는 로컬 DB 이름
        ).build()
    }

    // 2. 에러의 원인이었던 FavoriteDao 제공
    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}