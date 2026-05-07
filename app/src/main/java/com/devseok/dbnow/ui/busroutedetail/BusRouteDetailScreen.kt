package com.devseok.dbnow.ui.busroutedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devseok.dbnow.ui.search.BusRouteTimelineView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteDetailScreen(
    viewModel: BusRouteDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val routeStations by viewModel.routeStations.collectAsState()
    val busPositions by viewModel.busPositions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("[${viewModel.routeName}] 실시간 노선도", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadRouteDetails() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // 기존에 작성해두신 노선도 타임라인 UI 재활용
                // (즐겨찾기 관련 파라미터는 필요에 따라 ViewModel에 추가 구현하시면 됩니다)
                BusRouteTimelineView(
                    routeStations = routeStations,
                    busPositions = busPositions,
                    favoriteIds = emptySet(), // 임시
                    onFavoriteClick = {}      // 임시
                )
            }
        }
    }
}