package com.devseok.dbnow.di

import com.devseok.dbnow.BuildConfig
import com.devseok.dbnow.data.api.BusApiService
import com.devseok.dbnow.data.model.BusItems
import com.devseok.dbnow.data.model.BusItemsDeserializer
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
// 로그 찍는 인터셉터 생성 (BODY까지 전부 출력)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient에 인터셉터 장착
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val customGson = GsonBuilder()
            // BusItems 타입이 들어오면 무조건 우리가 만든 파서를 거치도록 설정
            .registerTypeAdapter(BusItems::class.java, BusItemsDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/6270000/dbmsapi02/")
            .client(client)
            // Json 설정 코드 전체를 이 한 줄로 우회합니다.
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .build()
    }

    @Provides
    @Singleton
    fun provideBusApiService(retrofit: Retrofit): BusApiService {
        return retrofit.create(BusApiService::class.java)
    }
}