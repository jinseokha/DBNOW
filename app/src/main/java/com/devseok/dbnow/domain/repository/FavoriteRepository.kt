package com.devseok.dbnow.domain.repository

import com.devseok.dbnow.domain.model.SearchResultItem
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    // 1. 즐겨찾기 목록 불러오기 (Flow를 쓰면 DB 업데이트 시 화면이 자동 갱신됩니다)
    fun getFavorites(): Flow<List<SearchResultItem>>

    // 2. 즐겨찾기 추가
    suspend fun addFavorite(item: SearchResultItem)

    // 3. 즐겨찾기 삭제 (UseCase에서 호출하는 함수)
    suspend fun deleteFavorite(id: String)

}