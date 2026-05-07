package com.devseok.dbnow.ui.main

import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.SearchResultItem

data class MainDashboardState(
    val isLoading: Boolean = false,                 // 초기 로딩 상태
    val isRefreshing: Boolean = false,              // 당겨서 새로고침(SwipeRefresh) 상태

    val favoriteList: List<SearchResultItem> = emptyList(), // 1. 로컬 DB에서 불러온 내 즐겨찾기 목록 원본
    val favoriteBuses: List<FavoriteBusItem> = emptyList(), // 2. UI에 실제로 그려질 실시간 매핑 데이터

    val errorMessage: String? = null                // 에러 발생 시 토스트/스낵바 텍스트
)

// 화면에서만 쓰이는 작은 UI 모델들
data class FavoriteBusItem(
    val baseInfo: SearchResultItem,          // 즐겨찾기 한 원본 항목 (버스 노선 또는 정류소)
    val arrivals: List<BusArrivalInfo>       // 해당 항목의 실시간 도착 예정 정보 리스트
)