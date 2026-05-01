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
        // 1. Firebase에서 즐겨찾기 목록 가져오기
        val favorites = favoriteRepository.getFavoriteList()

        // 2. 각 즐겨찾기 항목에 대해 실시간 API 호출 및 결합
        favorites.map { favorite ->
            val arrival = busRepository.getArrivalInfo(favorite.stationId, favorite.busId)

            FavoriteBusItem(
                busId = favorite.busId,
                busNumber = favorite.busNumber,
                stationId = favorite.stationId,
                stationName = favorite.stationName,
                remainingTime = formatTime(arrival.remainingTime),
                isLowFloor = arrival.isLowFloor,
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