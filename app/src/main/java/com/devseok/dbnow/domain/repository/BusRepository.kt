package com.devseok.dbnow.domain.repository

import com.devseok.dbnow.domain.model.ArrivalInfo

interface BusRepository {
    suspend fun getArrivalInfo(stationId: String, busId: String): ArrivalInfo
}