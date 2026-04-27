package com.devseok.dbnow.data.mapper

import com.devseok.dbnow.data.model.FavoriteEntity
import com.devseok.dbnow.domain.model.FavoriteBus

fun FavoriteEntity.toDomain(): FavoriteBus {
    return FavoriteBus(
        busId = this.busId,
        busNumber = this.busNumber,
        busType = this.busType,
        stationId = this.stationId,
        stationName = this.stationName,
        // Long 타입의 타임스탬프를 그대로 전달하거나,
        // 도메인 레이어에서 정렬 기준 등으로 활용합니다.
        addedAt = this.createdAt
    )
}



fun FavoriteBus.toEntity(userId: String): FavoriteEntity {
    return FavoriteEntity(
        busId = this.busId,
        busNumber = this.busNumber,
        busType = this.busType,
        stationId = this.stationId,
        stationName = this.stationName,
        userId = userId,
        createdAt = this.addedAt
    )
}