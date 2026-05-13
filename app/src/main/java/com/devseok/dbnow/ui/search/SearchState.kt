package com.devseok.dbnow.ui.search

import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.SearchResultItem

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResultItem> = emptyList(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false, // 검색 버튼을 누른 적이 있는지 여부 (초기 화면 vs 결과 없음 구분)
    val toastMessage: String? = null,

    // 현재 즐겨찾기된 아이템들의 ID 집합 (빠른 검색을 위해 Set 사용)
    val favoriteIds: Set<String> = emptySet(),

    // ==========================================
    // ★ 바텀 시트 공통 상태
    // ==========================================
    val selectedItem: SearchResultItem? = null,        // 클릭한 항목 (이 값이 null이 아니면 바텀 시트가 열림)
    val isDetailLoading: Boolean = false,               // 상세 정보 로딩 상태

    val recentSearches: List<String> = emptyList(),

// ==========================================
    // 1. 정류소 클릭 시 상태 (전광판)
    // ==========================================
    val stationArrivals: List<BusArrivalInfo> = emptyList(),

    // ==========================================
    // 2. 버스 클릭 시 상태 (노선도 + 실시간 위치)
    // ==========================================
    val routeStations: List<SearchResultItem> = emptyList(), // 전체 노선(정류소 목록)
    val busPositions: List<BusPosition> = emptyList()        // 현재 달리고 있는 버스들의 위치
)