package com.devseok.dbnow.ui.search

import com.devseok.dbnow.domain.model.SearchResultItem

sealed class SearchEvent {
    data class OnQueryChanged(val query: String) : SearchEvent() // 검색어 입력
    object OnSearchClick : SearchEvent()                         // 검색 버튼 클릭
    object OnClearClick : SearchEvent()                          // 검색어 지우기 (X 버튼)
}