package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import javax.inject.Inject

class DeleteFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(stationId: String, busId: String): Result<Unit> {
        val documentId = "${stationId}_${busId}"
        return favoriteRepository.removeFavorite(documentId)
    }
}