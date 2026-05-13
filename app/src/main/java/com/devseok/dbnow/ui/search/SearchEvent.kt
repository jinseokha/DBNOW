package com.devseok.dbnow.ui.search

import com.devseok.dbnow.domain.model.SearchResultItem

sealed class SearchEvent {
    data class OnQueryChanged(val query: String) : SearchEvent() // 검색어 입력
    object OnSearchClick : SearchEvent()                         // 검색 버튼 클릭
    object OnClearClick : SearchEvent()                          // 검색어 지우기 (X 버튼)

    data class OnFavoriteClick(val item: SearchResultItem) : SearchEvent() // 즐겨찾기 아이콘 클릭
    object OnToastShown : SearchEvent() // 토스트를 띄운 후 상태를 지우기 위한 이벤트

    data class OnRowClick(val item: SearchResultItem) : SearchEvent() // 행 전체 클릭 시
    object OnBottomSheetDismiss : SearchEvent()                     // 바텀 시트 닫을 때

    data class OnRecentSearchClick(val query: String) : SearchEvent()
    data class OnDeleteRecentSearch(val query: String) : SearchEvent()
    object OnClearRecentSearches : SearchEvent()
}