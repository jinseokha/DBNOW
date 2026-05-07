package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(item: SearchResultItem) {
        favoriteRepository.addFavorite(item)
    }
}