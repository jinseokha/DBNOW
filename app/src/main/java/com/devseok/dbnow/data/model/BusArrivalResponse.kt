package com.devseok.dbnow.data.model

import com.google.gson.annotations.SerializedName

data class BusArrivalResponse(
    @SerializedName("body")
    val body: BusArrivalBody?
)

data class BusArrivalBody(
    @SerializedName("list")
    val list: List<BusArrivalTicket>?
)

data class BusArrivalTicket(
    @SerializedName("routeId")
    val routeId: String?,       // 노선 ID
    @SerializedName("arrTime")
    val arrTime: Int?,          // 도착 예정 시간 (초)
    @SerializedName("arrOrder")
    val arrOrder: Int?,         // 남은 정류장 수
    @SerializedName("busType")
    val busType: String?,       // 저상 버스 여부 (예: "1")
    @SerializedName("curStnNm")
    val currentStationName: String?,
    @SerializedName("lastBus")
    val lastBus: String?        // 막차 여부 ("Y"/"N")
)