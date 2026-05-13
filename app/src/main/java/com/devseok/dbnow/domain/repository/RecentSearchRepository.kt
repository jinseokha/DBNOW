package com.devseok.dbnow.domain.repository

import kotlinx.coroutines.flow.Flow

interface RecentSearchRepository {
    fun getRecentSearches(): Flow<List<String>>
    suspend fun addSearch(query: String)
    suspend fun deleteSearch(query: String)
    suspend fun clearAll()
}