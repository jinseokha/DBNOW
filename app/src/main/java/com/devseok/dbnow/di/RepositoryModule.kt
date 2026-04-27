package com.devseok.dbnow.di

import com.devseok.dbnow.data.repository.AuthRepositoryImpl
import com.devseok.dbnow.data.repository.BusRepositoryImpl
import com.devseok.dbnow.data.repository.FavoriteRepositoryImpl
import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.BusRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBusRepository(
        busRepositoryImpl: BusRepositoryImpl
    ): BusRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository

}