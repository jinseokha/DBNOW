package com.devseok.dbnow.data.model

data class FavoriteEntity(
    val busId: String = "",         // 노선 ID
    val busNumber: String = "",     // 노선 번호 (예: "706")
    val busType: String = "",       // 버스 타입 (예: "간선")
    val stationId: String = "",     // 정류장 ID
    val stationName: String = "",   // 정류장 명칭
    val userId: String = "",        // Firebase User UID (사용자별 식별)
    val createdAt: Long = 0L        // 등록 시간 (정렬용 타임스탬프)
)