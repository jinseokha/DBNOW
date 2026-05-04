package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.repository.BusRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import com.devseok.dbnow.ui.main.FavoriteBusItem
import javax.inject.Inject

class GetFavoriteBusArrivalsUseCase @Inject constructor (
    private val busRepository: BusRepository,
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(): Result<List<FavoriteBusItem>> = runCatching {
        // 1. Result에서 실제 리스트를 꺼냅니다.
        val favorites = favoriteRepository.getFavoriteList().getOrThrow()

        // 2. 각 항목별로 실시간 정보를 가져와 결합합니다.
        favorites.map { favorite ->
            // 실시간 도착 정보 호출 (Result 반환 가정)
            val arrivalResult = busRepository.getBusArrival(favorite.stationId, favorite.busId)

            // 실시간 정보가 실패하더라도 리스트 전체가 죽지 않게 getOrNull 등으로 방어 처리
            val arrival = arrivalResult.getOrNull()

            FavoriteBusItem(
                busId = favorite.busId,
                busNumber = favorite.busNumber,
                stationId = favorite.stationId,
                stationName = favorite.stationName,
                // 실시간 정보가 없으면 "정보 없음" 등으로 표시
                remainingTime = arrival?.let { formatTime(it.remainingTime) } ?: "정보 없음",
                isLowFloor = arrival?.isLowFloor ?: false,
                busType = favorite.busType
            )
        }
    }

    private fun formatTime(seconds: Int): String {
        return when {
            seconds == 0 -> "정보 없음"
            seconds < 60 -> "잠시 후 도착"
            else -> "${seconds / 60}분 ${seconds % 60}초"
        }
    }
}