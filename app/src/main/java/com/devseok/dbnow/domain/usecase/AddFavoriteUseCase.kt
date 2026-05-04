package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.model.FavoriteBus
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.model.toEntity
import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(searchItem: SearchResultItem): Result<Unit> = runCatching {
        // 1. 사용자 ID 확인 (없으면 그 자리에서 즉시 익명 로그인 수행)
        // 이 부분이 '인증되지 않은 사용자' 에러를 방지하는 핵심 로직입니다.
        val userId = authRepository.getCurrentUserId()
            ?: authRepository.signInAnonymously().getOrThrow()

        // 2. 이미 등록된 항목인지 중복 체크
        val currentFavorites = favoriteRepository.getFavoriteList().getOrThrow()
        val isDuplicate = currentFavorites.any { favorite ->
            if (searchItem.isBus) {
                favorite.busId == searchItem.id
            } else {
                favorite.stationId == searchItem.id
            }
        }

        if (isDuplicate) {
            throw Exception("이미 즐겨찾기에 등록된 항목입니다.")
        }

        // 3. 도메인 모델 생성
        val favoriteBus = FavoriteBus(
            busId = if (searchItem.isBus) searchItem.id else "",
            busNumber = if (searchItem.isBus) searchItem.title else "",
            busType = if (searchItem.isBus) searchItem.subtitle else "",
            stationId = if (!searchItem.isBus) searchItem.id else "",
            stationName = if (!searchItem.isBus) searchItem.title else "",
            addedAt = System.currentTimeMillis()
        )

        // 4. 저장 요청 (인증된 userId는 Repository 구현체 내부에서 다시 사용됨)
        favoriteRepository.addFavorite(favoriteBus).getOrThrow()
    }
}