package com.devseok.dbnow.data.repository

import com.devseok.dbnow.data.local.FavoriteDao
import com.devseok.dbnow.data.local.FavoriteEntity
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<SearchResultItem>> {
        // DB 엔티티(Flow)를 도메인 모델(SearchResultItem)로 변환하여 방출
        return favoriteDao.getFavorites().map { entities ->
            entities.map { entity ->
                SearchResultItem(
                    id = entity.id,
                    title = entity.title,
                    subtitle = entity.subtitle,
                    isBus = entity.isBus,
                    stationId = entity.stationId,
                    routeId = entity.routeId,
                    busType = entity.busType,
                    seq = entity.seq
                )
            }
        }
    }

    override suspend fun addFavorite(item: SearchResultItem) {
        // 도메인 모델을 DB 엔티티로 변환하여 저장
        val entity = FavoriteEntity(
            id = item.id,
            title = item.title,
            subtitle = item.subtitle,
            isBus = item.isBus,
            stationId = item.stationId,
            routeId = item.routeId,
            busType = item.busType,
            seq = item.seq
        )
        favoriteDao.insertFavorite(entity)
    }

    override suspend fun deleteFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }
}