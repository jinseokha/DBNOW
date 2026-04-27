package com.devseok.dbnow.ui.main

data class MainDashboardState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userName: String = "",
    val favoriteBuses: List<FavoriteBusItem> = emptyList(),
    val errorMessage: String? = null
)

// 화면에서만 쓰이는 작은 UI 모델들
data class FavoriteBusItem(
    val busId: String,          // 노선 ID
    val busNumber: String,      // 노선 번호 (예: "706")
    val stationId: String,      // 정류장 ID
    val stationName: String,    // 정류장 이름
    val remainingTime: String,  // 도착 예정 시간 (예: "5분 30초")
    val isLowFloor: Boolean,    // 저상 버스 여부
    val busType: String,        // 급행, 간선 등
    val isAlarmOn: Boolean = false
)