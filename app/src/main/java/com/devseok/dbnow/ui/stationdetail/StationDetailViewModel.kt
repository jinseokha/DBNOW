package com.devseok.dbnow.ui.stationdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.repository.BusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationDetailViewModel @Inject constructor(
    private val busRepository: BusRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 파라미터 수신
    private val stationId: String = checkNotNull(savedStateHandle["stationId"])
    val stationName: String = checkNotNull(savedStateHandle["stationName"])

    private val _arrivals = MutableStateFlow<List<BusArrivalInfo>>(emptyList())
    val arrivals = _arrivals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadArrivals()
    }

    fun loadArrivals() {
        viewModelScope.launch {
            _isLoading.value = true
            busRepository.getRealtime02(stationId)
                .onSuccess { _arrivals.value = it }
            _isLoading.value = false
        }
    }
}