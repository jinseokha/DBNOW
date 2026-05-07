package com.devseok.dbnow.ui.busroutedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.BusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusRouteDetailViewModel @Inject constructor(
    private val busRepository: BusRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routeId: String = checkNotNull(savedStateHandle["routeId"])
    val routeName: String = checkNotNull(savedStateHandle["routeName"])

    private val _routeStations = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val routeStations = _routeStations.asStateFlow()

    private val _busPositions = MutableStateFlow<List<BusPosition>>(emptyList())
    val busPositions = _busPositions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadRouteDetails()
    }

    fun loadRouteDetails() {
        viewModelScope.launch {
            _isLoading.value = true

            // 병렬 호출 (async)
            val stationsDeferred = async { busRepository.getBs(routeId) }
            val positionsDeferred = async { busRepository.getPos(routeId) }

            val stationsResult = stationsDeferred.await()
            val positionsResult = positionsDeferred.await()

            // Repository에서 이미 SearchResultItem으로 변환해서 주므로 타입이 완벽하게 일치합니다.
            stationsResult.onSuccess { _routeStations.value = it }
            positionsResult.onSuccess { _busPositions.value = it }

            _isLoading.value = false
        }
    }
}