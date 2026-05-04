package com.devseok.dbnow.data.repository

import com.devseok.dbnow.data.mapper.toDomain
import com.devseok.dbnow.data.mapper.toEntity
import com.devseok.dbnow.data.model.FavoriteEntity
import com.devseok.dbnow.domain.model.FavoriteBus
import com.devseok.dbnow.domain.model.toEntity
import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : FavoriteRepository {

    private val favoriteCollection = firestore.collection("favorites")

    /**
     * 사용자의 모든 즐겨찾기 목록을 가져와 도메인 모델 리스트로 변환합니다.
     */
    override suspend fun getFavoriteList(): Result<List<FavoriteBus>> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: authRepository.signInAnonymously().getOrThrow()

        // 1. Firestore에서 해당 유저의 즐겨찾기 쿼리
        val snapshot = favoriteCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt") // 추가된 순서대로 정렬
            .get()
            .await()

        // 2. Entity 리스트를 Domain 모델 리스트로 변환
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(FavoriteEntity::class.java)?.toDomain()
        }
    }

    /**
     * 즐겨찾기 항목을 추가합니다. (Domain 모델을 받아 Entity로 변환 후 저장)
     */
    override suspend fun addFavorite(favoriteBus: FavoriteBus): Result<Unit> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: authRepository.signInAnonymously().getOrThrow()

        // 1. 도메인 모델에 사용자 ID를 입혀 Entity로 변환
        val entity = favoriteBus.toEntity(userId)

        // 2. Firestore 저장 (문서 ID를 정류장_버스 조합으로 만들어 중복 방지)
        val documentId = "${entity.stationId}_${entity.busId}"
        favoriteCollection.document(documentId)
            .set(entity)
            .await()
    }

    /**
     * 즐겨찾기 항목을 삭제합니다.
     */
    override suspend fun removeFavorite(stationId: String, busId: String): Result<Unit> = runCatching {
        val documentId = "${stationId}_${busId}"
        favoriteCollection.document(documentId)
            .delete()
            .await()
    }

    override suspend fun isFavorite(stationId: String, busId: String): Result<Boolean> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw Exception("인증되지 않은 사용자입니다.")

        // 1. 사용자 ID, 정류장 ID, 버스 ID가 모두 일치하는 문서를 쿼리
        val snapshot = favoriteCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("stationId", stationId)
            .whereEqualTo("busId", busId)
            .limit(1) // 존재 여부만 알면 되므로 1개로 제한하여 성능 최적화
            .get()
            .await()

        // 2. 결과가 비어있지 않으면 이미 즐겨찾기에 등록된 상태
        !snapshot.isEmpty
    }
}