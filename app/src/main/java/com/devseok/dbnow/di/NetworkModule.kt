package com.devseok.dbnow.di

import com.devseok.dbnow.BuildConfig
import com.devseok.dbnow.data.api.BusApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("serviceKey")
    fun provideServiceKey(): String {
        return BuildConfig.DAEGU_BUS_API_KEY
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/6270000/dbmsapi02/")
            // Json 설정 코드 전체를 이 한 줄로 우회합니다.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBusApiService(retrofit: Retrofit): BusApiService {
        return retrofit.create(BusApiService::class.java)
    }
}