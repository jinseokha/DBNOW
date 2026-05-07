package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import javax.inject.Inject

class DeleteFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(item: SearchResultItem) {
        // 보통 로컬 DB 삭제 시에는 고유 ID(Primary Key)만 넘겨서 삭제합니다.
        // Room DAO 설정에 따라 item 전체를 넘기도록(deleteFavorite(item)) 작성하셔도 무방합니다.
        favoriteRepository.deleteFavorite(item.id)
    }
}