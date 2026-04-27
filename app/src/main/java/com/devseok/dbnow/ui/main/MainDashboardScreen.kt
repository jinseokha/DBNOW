package com.devseok.dbnow.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: MainDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("대구 버스 나우", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        // uiState.isRefreshing 상태에 따라 인디케이터가 자동으로 나타나고 사라짐
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(MainDashboardEvent.Refresh) }
        ) {
            when {
                // 최초 로딩 상태 (새로고침이 아닌 초기 진입 시)
                uiState.isLoading && !uiState.isRefreshing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 에러 상태
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 즐겨찾기 목록이 비어있을 때
                uiState.favoriteBuses.isEmpty() -> {
                    Text(
                        text = "즐겨찾기한 버스가 없습니다.\n노선을 검색하여 추가해 보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 정상적인 리스트 렌더링
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.favoriteBuses,
                            key = { "${it.busId}_${it.stationId}" } // 재구성 최적화를 위한 고유 키
                        ) { busItem ->
                            BusArrivalCard(
                                busItem = busItem,
                                onDeleteClick = {
                                    viewModel.onEvent(
                                        MainDashboardEvent.DeleteFavorite(
                                            stationId = busItem.stationId,
                                            busId = busItem.busId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusArrivalCard(
    busItem: FavoriteBusItem,
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 버스 번호 배지 (노선 타입에 따른 색상 변경)
            BusNumberBadge(
                number = busItem.busNumber,
                busType = busItem.busType
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. 정류장 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = busItem.stationName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (busItem.isLowFloor) {
                    Text(
                        text = "♿ 저상 버스",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50) // 초록색 강조
                    )
                }
            }

            // 3. 도착 시간 정보 (곧 도착일 경우 빨간색 강조 및 애니메이션 효과 가능)
            ArrivalStatusView(remainingTime = busItem.remainingTime)

            // 4. 추가 기능 버튼 (삭제 등)
            /*IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.outline
                )
            }*/
        }
    }
}

@Composable
fun BusNumberBadge(number: String, busType: String) {
    // 대구 버스 표준 색상 매핑
    val badgeColor = when {
        busType.contains("급행") -> Color(0xFFE53935) // 빨강
        busType.contains("간선") -> Color(0xFF1E88E5) // 파랑
        busType.contains("지선") -> Color(0xFFFDD835) // 노랑
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = badgeColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = number,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (busType.contains("지선")) Color.Black else Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ArrivalStatusView(remainingTime: String) {
    val isApproaching = remainingTime.contains("곧") || remainingTime.contains("잠시")

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = remainingTime,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = if (isApproaching) Color.Red else MaterialTheme.colorScheme.onSurface
        )
        if (isApproaching) {
            Text(
                text = "준비하세요!",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red
            )
        }
    }
}