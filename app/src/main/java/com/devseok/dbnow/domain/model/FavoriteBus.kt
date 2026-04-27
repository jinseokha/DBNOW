package com.devseok.dbnow.domain.model

data class FavoriteBus(
    val busId: String,
    val busNumber: String,
    val busType: String,
    val stationId: String,
    val stationName: String,
    val addedAt: Long
) {
    // 도메인 모델 내부에 간단한 비즈니스 로직을 포함할 수 있음
    val isExpressBus: Boolean
        get() = busType.contains("급행")
}