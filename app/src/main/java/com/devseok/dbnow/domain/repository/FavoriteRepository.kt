package com.devseok.dbnow.domain.repository

import com.devseok.dbnow.data.model.FavoriteEntity
import com.devseok.dbnow.domain.model.FavoriteBus

interface FavoriteRepository {
    suspend fun getFavoriteList(): Result<List<FavoriteBus>>
    suspend fun addFavorite(favoriteBus: FavoriteBus): Result<Unit>
    suspend fun removeFavorite(stationId: String, busId: String): Result<Unit>

    suspend fun isFavorite(stationId: String, busId: String): Result<Boolean>
}