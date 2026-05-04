package com.devseok.dbnow.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase
) : ViewModel(){
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChanged -> {
                _state.update { it.copy(query = event.query) }
                // 검색어를 다 지우면 초기 상태로 되돌림
                if (event.query.isBlank()) {
                    resetState()
                }
            }
            SearchEvent.OnSearchClick -> search()
            SearchEvent.OnClearClick -> resetState()
        }
    }

    private fun search() {
        val currentQuery = _state.value.query.trim()
        if (currentQuery.isEmpty()) return

        viewModelScope.launch {
            // 로딩 시작, 에러 초기화, 검색 여부 true
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    hasSearched = true
                )
            }

            searchUseCase(currentQuery)
                .onSuccess { results ->
                    _state.update {
                        it.copy(isLoading = false, results = results)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "검색 중 오류가 발생했습니다.",
                            results = emptyList()
                        )
                    }
                }
        }
    }

    private fun resetState() {
        _state.update {
            it.copy(
                query = "",
                isLoading = false,
                results = emptyList(),
                errorMessage = null,
                hasSearched = false
            )
        }
    }
}