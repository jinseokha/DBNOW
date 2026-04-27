package com.devseok.dbnow.data.api


import com.devseok.dbnow.data.model.BusArrivalResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    @GET("getRealTime02")
    suspend fun getArrivalInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("stationId") stationId: String,
        @Query("type") type: String = "json" // JSON 타입 명시
    ): BusArrivalResponse
}