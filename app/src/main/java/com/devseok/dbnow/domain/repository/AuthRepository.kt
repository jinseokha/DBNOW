package com.devseok.dbnow.domain.repository

interface AuthRepository {
    // 현재 로그인된 사용자의 UID를 반환 (비로그인 시 null)
    fun getCurrentUserId(): String?

    // 익명 로그인을 수행 (즐겨찾기 기능을 즉시 사용하게 하기 위함)
    suspend fun signInAnonymously(): Result<String>

    // 로그아웃
    suspend fun signOut(): Result<Unit>
}