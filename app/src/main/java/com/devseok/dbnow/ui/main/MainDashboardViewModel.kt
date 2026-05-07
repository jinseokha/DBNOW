package com.devseok.dbnow.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devseok.dbnow.domain.repository.AuthRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import com.devseok.dbnow.domain.usecase.DeleteFavoriteUseCase
import com.devseok.dbnow.domain.usecase.GetFavoriteBusArrivalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainDashboardViewModel @Inject constructor(
    private val getFavoriteBusArrivalsUseCase: GetFavoriteBusArrivalsUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainDashboardState())
    val uiState: StateFlow<MainDashboardState> = _uiState.asStateFlow()

    init {
        // 앱이 시작되면 로컬 DB(Room)에서 즐겨찾기 목록을 관찰하기 시작합니다.
        observeFavorites()
    }

    /**
     * 1. 로컬 DB(Room) 즐겨찾기 목록 관찰 (Flow)
     * DB에 변경(추가/삭제)이 발생할 때마다 자동으로 호출되어 목록을 최신화합니다.
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect { favorites ->
                _uiState.update { it.copy(favoriteList = favorites) }
                // 즐겨찾기 원본이 갱신되었으므로 실시간 도착 정보를 요청합니다.
                loadBusArrivals(isRefresh = false)
            }
        }
    }

    /**
     * 2. 실시간 도착 정보 조회 (당겨서 새로고침 또는 DB 갱신 시 호출)
     */
    fun loadBusArrivals(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favoriteList

            // 즐겨찾기가 없으면 로딩 없이 빈 화면 표시
            if (currentFavorites.isEmpty()) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, favoriteBuses = emptyList())
                }
                return@launch
            }

            // 로딩 UI 상태 ON
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true) else it.copy(isLoading = true)
            }

            // UseCase를 통해 병렬(async/awaitAll)로 API 통신
            runCatching {
                getFavoriteBusArrivalsUseCase(currentFavorites)
            }.onSuccess { busItemsMap ->
                // Map -> UI 전용 모델(FavoriteBusItem) 리스트로 변환
                val uiFavoriteItems = busItemsMap.map { (baseItem, arrivalList) ->
                    FavoriteBusItem(baseInfo = baseItem, arrivals = arrivalList)
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    favoriteBuses = uiFavoriteItems,
                    errorMessage = null
                )}
            }.onFailure { error ->
                _uiState.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = error.message ?: "실시간 정보를 불러오지 못했습니다."
                )}
            }
        }
    }

    /**
     * 3. 즐겨찾기 삭제 (낙관적 업데이트 적용)
     * 체감 속도 향상을 위해 UI에서 먼저 지운 뒤, 백그라운드에서 DB를 삭제합니다.
     */
    fun deleteFavoriteItem(targetItem: FavoriteBusItem) {
        viewModelScope.launch {
            // [UI 선 반영] 기존 리스트에서 선택된 항목을 제외한 새 리스트로 덮어쓰기
            val previousList = _uiState.value.favoriteBuses
            val updatedList = previousList.filter { it.baseInfo.id != targetItem.baseInfo.id }
            _uiState.update { it.copy(favoriteBuses = updatedList) }

            // [DB 삭제 로직 수행]
            runCatching {
                deleteFavoriteUseCase(targetItem.baseInfo)
            }.onFailure {
                // 실패 시 원래 UI 리스트로 복구하고 에러 메시지 띄움
                _uiState.update {
                    it.copy(
                        favoriteBuses = previousList,
                        errorMessage = "즐겨찾기 삭제에 실패했습니다."
                    )
                }
            }
        }
    }

}