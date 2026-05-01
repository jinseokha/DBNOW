package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.model.FavoriteBus
import com.devseok.dbnow.domain.model.toEntity
import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import com.devseok.dbnow.ui.search.SearchResultItem
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(searchItem: SearchResultItem): Result<Unit> = runCatching {
        // 1. 사용자 인증 확인 (Firebase UID가 있는지 확인)
        val userId = authRepository.getCurrentUserId()
            ?: throw Exception("로그인이 필요한 서비스입니다.")

        // 2. 기존 즐겨찾기 목록을 가져와 중복 여부 확인
        // Repository에서 Result<List<FavoriteBus>>를 반환한다고 가정
        val currentFavorites = favoriteRepository.getFavoriteList().getOrThrow()

        val isDuplicate = currentFavorites.any { favorite ->
            if (searchItem.isBus) {
                // 버스 노선을 추가하려는 경우: 동일한 노선 ID가 있는지 확인
                favorite.busId == searchItem.id
            } else {
                // 정류장을 추가하려는 경우: 동일한 정류장 ID가 있는지 확인
                favorite.stationId == searchItem.id
            }
        }

        if (isDuplicate) {
            throw Exception("이미 즐겨찾기에 등록된 항목입니다.")
        }

        // 3. UI 모델(SearchResultItem)을 도메인 모델(FavoriteBus)로 변환
        // 실제 대구 API 구조에 따라 busId와 stationId 할당 로직은 달라질 수 있습니다.
        val favoriteBus = FavoriteBus(
            busId = if (searchItem.isBus) searchItem.id else "",
            busNumber = if (searchItem.isBus) searchItem.title else "",
            busType = if (searchItem.isBus) searchItem.subtitle else "",
            stationId = if (!searchItem.isBus) searchItem.id else "",
            stationName = if (!searchItem.isBus) searchItem.title else "",
            addedAt = System.currentTimeMillis()
        )

        // 4. Repository를 통해 저장 실행
        // Repository 인터페이스가 FavoriteBus를 인자로 받도록 설계되어 있어야 합니다.
        favoriteRepository.addFavorite(favoriteBus).getOrThrow()
    }
}