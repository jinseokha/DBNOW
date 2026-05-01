package com.devseok.dbnow.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(

) : ViewModel(){
    private val _uiState = MutableStateFlow(SearchState())
    val uiState = _uiState.asStateFlow()

    // 검색어를 관찰하여 debounce 처리하기 위한 Flow
    private val searchQueryFlow = MutableStateFlow("")

    init {
        observeSearchQuery()
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> {
                _uiState.update { it.copy(query = event.query) }
                searchQueryFlow.value = event.query
            }
            is SearchEvent.AddFavorite -> {
                // 즐겨찾기 추가 로직 호출
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500L) // 0.5초 대기
                .distinctUntilChanged() // 이전 검색어와 동일하면 무시
                .filter { it.length >= 2 } // 최소 2글자 이상일 때만 검색
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // TODO: 실제 API 호출 (searchUseCase(query))
        // 임시 더미 데이터로 동작 확인
        delay(1000)
        val mockResults = listOf(
            SearchResultItem("3000706000", "706", "간선 버스", true),
            SearchResultItem("7011010100", "경북대학교북문앞", "정류장 번호: 02010", false)
        ).filter { it.title.contains(query) }

        _uiState.update {
            it.copy(
                isLoading = false,
                searchResults = mockResults,
                errorMessage = if (mockResults.isEmpty()) "검색 결과가 없습니다." else null
            )
        }
    }
}