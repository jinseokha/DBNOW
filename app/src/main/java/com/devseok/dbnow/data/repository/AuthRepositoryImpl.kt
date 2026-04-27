package com.devseok.dbnow.data.repository

import com.devseok.dbnow.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // 사용자가 로그인되어 있지 않다면 익명 로그인을 수행하여 UID 확보
    override suspend fun signInAnonymously(): Result<String> = runCatching {
        val result = firebaseAuth.signInAnonymously().await()
        result.user?.uid ?: throw Exception("인증 실패")
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
    }
}