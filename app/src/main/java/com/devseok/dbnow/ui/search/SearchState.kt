package com.devseok.dbnow.ui.search

import com.devseok.dbnow.domain.model.SearchResultItem

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResultItem> = emptyList(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false // 검색 버튼을 누른 적이 있는지 여부 (초기 화면 vs 결과 없음 구분)
)