package com.devseok.dbnow.domain.model

import com.devseok.dbnow.data.model.FavoriteEntity

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

fun FavoriteBus.toEntity(): FavoriteEntity {
    return FavoriteEntity(
        busId = this.busId,
        busNumber = this.busNumber,
        busType = this.busType,
        stationId = this.stationId,
        stationName = this.stationName,
        createdAt = this.addedAt
    )
}