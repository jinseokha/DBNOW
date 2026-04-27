package com.devseok.dbnow.ui.main

sealed class MainDashboardEvent {
    // 1. 사용자가 화면을 당겨서 새로고침할 때
    object Refresh : MainDashboardEvent()

    // 2. 특정 즐겨찾기 항목을 삭제할 때 (정류장 ID와 노선 ID가 필요함)
    data class DeleteFavorite(
        val stationId: String,
        val busId: String
    ) : MainDashboardEvent()

    // 3. 특정 버스의 도착 알림(FCM)을 켜거나 끌 때
    data class ToggleAlarm(
        val busId: String,
        val isEnabled: Boolean
    ) : MainDashboardEvent()

    // 4. 에러 발생 후 '다시 시도' 버튼을 눌렀을 때
    object Retry : MainDashboardEvent()
}