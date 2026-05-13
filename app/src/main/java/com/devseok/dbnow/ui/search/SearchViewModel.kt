package com.devseok.dbnow.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.BusRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import com.devseok.dbnow.domain.repository.RecentSearchRepository
import com.devseok.dbnow.domain.usecase.AddFavoriteUseCase
import com.devseok.dbnow.domain.usecase.DeleteFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val busRepository: BusRepository,
    private val favoriteRepository: FavoriteRepository,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    // 검색어 타이핑 시 API 과호출을 막기 위한 디바운스용 Flow
    private val queryFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val searchQueryFlow = MutableStateFlow("")

    init {
        // 1. Room DB의 즐겨찾기 목록을 실시간으로 관찰하여 하트(별) 아이콘 상태를 동기화합니다.
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect { favorites ->
                val favoriteIdSet = favorites.map { it.id }.toSet()
                _state.update { it.copy(favoriteIds = favoriteIdSet) }
            }
        }

        // 2. 검색어 입력 디바운스 처리 (0.3초 동안 추가 입력이 없으면 검색 실행)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500L) // ⏳ 사용자가 타자를 멈추고 0.5초(500ms) 대기
                .distinctUntilChanged() // 🔄 방금 전 검색어와 똑같으면 무시
                .collect { query ->
                    // 0.5초 동안 추가 입력이 없으면 드디어 검색 실행!
                    if (query.isNotBlank()) {
                        executeSearch(query)
                    } else {
                        // 검색어가 다 지워졌을 때는 검색 결과를 초기화
                        _state.update { it.copy(results = emptyList(), hasSearched = false) }
                    }
                }
        }

        // 최근 검색어 실시간 관찰
        viewModelScope.launch {
            recentSearchRepository.getRecentSearches().collect { searches ->
                _state.update { it.copy(recentSearches = searches) }
            }
        }
    }

    /**
     * UI에서 발생하는 모든 이벤트를 수신하는 단일 통로 (MVI 패턴)
     */
    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChanged -> {
                // UI(텍스트 필드)의 글자는 타자를 치는 즉시 바뀌어야 하므로 바로 업데이트!
                _state.update { it.copy(query = event.query) }

                // 검색 실행은 searchQueryFlow에 넘겨서 디바운스(0.5초 대기)를 타게 만듭니다.
                searchQueryFlow.value = event.query
            }
            SearchEvent.OnSearchClick -> search()
            SearchEvent.OnClearClick -> resetState()
            is SearchEvent.OnFavoriteClick -> toggleFavorite(event.item)
            SearchEvent.OnToastShown -> _state.update { it.copy(toastMessage = null) }
            is SearchEvent.OnRowClick -> fetchDetails(event.item)
            SearchEvent.OnBottomSheetDismiss -> clearSelection()

            // 최근 검색어 클릭 시 텍스트 필드 업데이트 및 검색 실행
            is SearchEvent.OnRecentSearchClick -> {
                _state.update { it.copy(query = event.query) }
                search()
            }
            is SearchEvent.OnDeleteRecentSearch -> {
                viewModelScope.launch { recentSearchRepository.deleteSearch(event.query) }
            }
            SearchEvent.OnClearRecentSearches -> {
                viewModelScope.launch { recentSearchRepository.clearAll() }
            }
        }
    }

    private fun search() {
        val currentQuery = _state.value.query
        if (currentQuery.isNotBlank()) {
            viewModelScope.launch { executeSearch(currentQuery) }
        }
    }

    private suspend fun executeSearch(query: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null, hasSearched = true) }

        recentSearchRepository.addSearch(query)

        busRepository.searchBusAndStations(query)
            .onSuccess { searchResults ->
                _state.update { it.copy(isLoading = false, results = searchResults) }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "검색 중 오류가 발생했습니다.")
                }
            }
    }

    /**
     * 바텀 시트에 띄울 상세 데이터를 가져옵니다. (정류소 vs 버스 분기 처리 및 병렬 통신 적용)
     */
    private fun fetchDetails(item: SearchResultItem) {
        // 기존 데이터 초기화 및 로딩 시작
        _state.update {
            it.copy(
                selectedItem = item,
                isDetailLoading = true,
                stationArrivals = emptyList(),
                routeStations = emptyList(),
                busPositions = emptyList()
            )
        }

        viewModelScope.launch {
            if (item.isBus) {
                // [버스 클릭] getBs(노선도)와 getPos(실시간 위치)를 병렬(async)로 호출
                val routeId = item.routeId ?: item.id
                val stationsDeferred = async { busRepository.getBs(routeId) }
                val positionsDeferred = async { busRepository.getPos(routeId) }

                val stationsResult = stationsDeferred.await()
                val positionsResult = positionsDeferred.await() // 실패해도 위치만 안 보이면 되므로 치명적이지 않음

                stationsResult.onSuccess { stations ->
                    _state.update {
                        it.copy(
                            isDetailLoading = false,
                            routeStations = stations,
                            busPositions = positionsResult.getOrNull() ?: emptyList()
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(isDetailLoading = false, toastMessage = "노선 정보를 불러오지 못했습니다.")
                    }
                }
            } else {
                // [정류소 클릭] getRealtime02(전광판) 호출
                val stationId = item.stationId ?: item.id
                busRepository.getRealtime02(stationId)
                    .onSuccess { arrivals ->
                        _state.update {
                            it.copy(isDetailLoading = false, stationArrivals = arrivals)
                        }
                    }
                    .onFailure { e ->

                        Log.d("test", "" + e.message)
                        _state.update {
                            it.copy(isDetailLoading = false, toastMessage = "도착 정보를 불러오지 못했습니다.")
                        }
                    }
            }
        }
    }

    /**
     * 즐겨찾기 추가/삭제 토글
     */
    private fun toggleFavorite(item: SearchResultItem) {
        viewModelScope.launch {
            val isFavorite = _state.value.favoriteIds.contains(item.id)

            runCatching {
                if (isFavorite) {
                    deleteFavoriteUseCase(item)
                } else {
                    addFavoriteUseCase(item)
                }
            }.onFailure {
                _state.update { it.copy(toastMessage = "즐겨찾기 처리에 실패했습니다.") }
            }
        }
    }

    private fun resetState() {
        _state.update {
            it.copy(query = "", results = emptyList(), hasSearched = false, errorMessage = null)
        }

        searchQueryFlow.value = ""
    }

    private fun clearSelection() {
        _state.update {
            it.copy(
                selectedItem = null,
                stationArrivals = emptyList(),
                routeStations = emptyList(),
                busPositions = emptyList()
            )
        }
    }
}