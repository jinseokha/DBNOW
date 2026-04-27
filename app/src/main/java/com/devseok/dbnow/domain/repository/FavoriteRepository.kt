package com.devseok.dbnow.domain.repository

import com.devseok.dbnow.data.model.FavoriteEntity

interface FavoriteRepository {
    suspend fun getFavoriteList(): List<FavoriteEntity>
    suspend fun addFavorite(favorite: FavoriteEntity): Result<Unit>
    suspend fun removeFavorite(favoriteId: String): Result<Unit>
}