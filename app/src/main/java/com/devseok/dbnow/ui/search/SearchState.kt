package com.devseok.dbnow.ui.search

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<SearchResultItem> = emptyList(),
    val errorMessage: String? = null
)

// UI에 표시할 검색 결과 데이터 모델
data class SearchResultItem(
    val id: String,           // 버스 ID 또는 정류장 ID
    val title: String,        // 버스 번호 또는 정류장 이름
    val subtitle: String,     // 버스 종류 또는 정류장 번호 등
    val isBus: Boolean        // true: 버스 노선, false: 정류장
)