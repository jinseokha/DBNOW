package com.devseok.dbnow.data.repository

import com.devseok.dbnow.data.local.RecentSearchDao
import com.devseok.dbnow.data.local.RecentSearchEntity
import com.devseok.dbnow.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentSearchRepositoryImpl @Inject constructor(
    private val dao: RecentSearchDao
) : RecentSearchRepository {
    override fun getRecentSearches(): Flow<List<String>> {
        return dao.getRecentSearches().map { list -> list.map { it.query } }
    }

    override suspend fun addSearch(query: String) {
        dao.insertSearch(RecentSearchEntity(query = query))
    }

    override suspend fun deleteSearch(query: String) {
        dao.deleteSearch(query)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}