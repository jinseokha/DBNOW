package com.devseok.dbnow.data.mapper

import com.devseok.dbnow.data.model.BusArrivalResponse
import com.devseok.dbnow.data.model.BusArrivalTicket
import com.devseok.dbnow.domain.model.ArrivalInfo

fun BusArrivalTicket.toDomain(): ArrivalInfo {
    return ArrivalInfo(
        remainingTime = this.arrTime ?: 0,
        remainingStations = this.arrOrder ?: 0,
        isLowFloor = this.busType == "1", // 대구 API 기준 '1'이 저상버스
        isLastBus = this.lastBus == "Y",
        currentStationName = this.currentStationName ?: "정보 없음"
    )
}