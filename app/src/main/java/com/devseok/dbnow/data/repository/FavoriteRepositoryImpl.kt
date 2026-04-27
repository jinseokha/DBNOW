package com.devseok.dbnow.data.repository

import com.devseok.dbnow.data.model.FavoriteEntity
import com.devseok.dbnow.domain.repository.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FavoriteRepository {


    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("로그인이 필요합니다.")

    private val userFavoritesCollection
        get() = firestore.collection("users").document(userId).collection("favorites")

    override suspend fun getFavoriteList(): List<FavoriteEntity> {
        return try {
            val snapshot = userFavoritesCollection.get().await() // kotlinx-coroutines-play-services 필요
            snapshot.toObjects(FavoriteEntity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addFavorite(favorite: FavoriteEntity): Result<Unit> = runCatching {
        // busId와 stationId를 조합해 고유 문서 ID 생성 (중복 방지)
        val docId = "${favorite.busId}_${favorite.stationId}"
        userFavoritesCollection.document(docId).set(favorite).await()
    }

    override suspend fun removeFavorite(favoriteId: String): Result<Unit> = runCatching {
        userFavoritesCollection.document(favoriteId).delete().await()
    }
}