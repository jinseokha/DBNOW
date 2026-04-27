package com.devseok.dbnow.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devseok.dbnow.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainDashboardState())
    val uiState: StateFlow<MainDashboardState> = _uiState.asStateFlow()

    init {
        // 앱 시작 시 자동 로그인 확인 및 데이터 로드
        checkAuthAndLoadData()
    }

    fun onEvent(event: MainDashboardEvent) {
        when (event) {
            is MainDashboardEvent.Refresh -> {
                loadBusArrivals(isRefresh = true)
            }
            is MainDashboardEvent.DeleteFavorite -> {
                deleteFavoriteItem(event.stationId, event.busId)
            }
            is MainDashboardEvent.ToggleAlarm -> {
                // 알림 설정 로직 호출
            }
            is MainDashboardEvent.Retry -> {
                loadBusArrivals(isRefresh = false)
            }
        }
    }

    private fun checkAuthAndLoadData() {
        viewModelScope.launch {
            if (authRepository.getCurrentUserId() == null) {
                authRepository.signInAnonymously()
            }
            loadBusArrivals()
        }
    }

    private fun loadBusArrivals(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            getFavoriteBusArrivalsUseCase()
                .onSuccess { busItems ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        favoriteBuses = busItems,
                        errorMessage = null
                    )}
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: "데이터를 가져오지 못했습니다."
                    )}
                }
        }
    }

    private fun deleteFavoriteItem(stationId: String, busId: String) {
        viewModelScope.launch {
            // 롤백(Rollback)을 대비해 현재 상태의 리스트를 백업해둡니다.
            val previousList = _uiState.value.favoriteBuses

            // UI State에서 즉시 해당 항목을 제거합니다 (Optimistic Update).
            // 서버 응답을 기다리지 않고 화면에서 바로 카드가 사라지게 되어 UX가 매우 부드러워집니다.
            _uiState.update { state ->
                state.copy(
                    favoriteBuses = state.favoriteBuses.filterNot {
                        it.stationId == stationId && it.busId == busId
                    }
                )
            }

            // 백그라운드에서 실제 Firebase 삭제를 요청합니다.
            deleteFavoriteUseCase(stationId, busId)
                .onSuccess {
                    // 성공 시 이미 UI에서 지웠으므로 추가 작업이 필요 없습니다.
                    // (필요하다면 "삭제되었습니다" 스낵바 이벤트를 발생시킬 수 있습니다)
                }
                .onFailure { error ->
                    // 네트워크 에러 등으로 삭제 실패 시,
                    // 백업해둔 이전 리스트로 화면을 롤백(복구)하고 에러 메시지를 띄웁니다.
                    _uiState.update { state ->
                        state.copy(
                            favoriteBuses = previousList,
                            errorMessage = "즐겨찾기 삭제에 실패했습니다: ${error.message}"
                        )
                    }
                }
        }
    }

}