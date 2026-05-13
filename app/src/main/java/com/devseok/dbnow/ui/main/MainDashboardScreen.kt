package com.devseok.dbnow.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: MainDashboardViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit, // 검색 화면(SearchScreen)으로 이동하는 콜백
    onNavigateToStation: (stationId: String, name: String) -> Unit,
    onNavigateToBus: (routeId: String, name: String) -> Unit
) {
    // 뷰모델의 상태를 Compose UI가 관찰하도록 설정
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 즐겨찾기", fontWeight = FontWeight.Bold) },
                actions = {
                    // 수동 새로고침 버튼
                    IconButton(onClick = { viewModel.loadBusArrivals(isRefresh = true) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                    // 검색 화면 이동 버튼
                    IconButton(onClick = { onNavigateToSearch() }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "검색")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                // 1. 전체 화면 로딩 상태
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // 2. 즐겨찾기 데이터가 아예 없을 때 (Empty State)
                state.favoriteBuses.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "등록된 즐겨찾기가 없습니다.\n우측 상단 돋보기 아이콘을 눌러 추가해보세요!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // 3. 즐겨찾기 데이터가 있을 때 목록 렌더링
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ★ 낙관적 업데이트를 위해 item.baseInfo.id를 Key로 사용합니다.
                        items(
                            items = state.favoriteBuses,
                            key = { it.baseInfo.id }
                        ) { favoriteItem ->
                            FavoriteBusCard(
                                item = favoriteItem,
                                onDeleteClick = {
                                    // 사용자가 삭제 버튼을 누르면 뷰모델의 삭제 함수(낙관적 업데이트) 호출
                                    viewModel.deleteFavoriteItem(favoriteItem)
                                },
                                onCardClick = {
                                    val baseInfo = favoriteItem.baseInfo
                                    if (baseInfo.isBus) {
                                        onNavigateToBus(baseInfo.routeId ?: baseInfo.id, baseInfo.title)
                                    } else {
                                        onNavigateToStation(baseInfo.stationId ?: baseInfo.id, baseInfo.title)
                                    }
                                },
                                // ★ 2. 내부 도착 버스 리스트 클릭 시 (해당 버스 노선도로 이동)
                                onRouteClick = { routeId, routeName ->
                                    onNavigateToBus(routeId, routeName)
                                }
                            )
                        }
                    }
                }
            }

            // 에러 메시지가 있으면 스낵바나 토스트를 띄워줍니다 (간단히 Text로 오버레이 처리 가능)
            if (state.errorMessage != null) {
                // SnackbarHost 등을 활용해 처리하는 것을 권장합니다.
            }
        }
    }
}

/**
 * 개별 즐겨찾기 항목을 그려주는 카드 UI 컴포저블
 */
@Composable
fun FavoriteBusCard(
    item: FavoriteBusItem,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit,
    onRouteClick: (routeId: String, routeName: String) -> Unit
) {
    val baseInfo = item.baseInfo
    val arrivals = item.arrivals

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // [상단] 타이틀 및 삭제 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 버스인지 정류장인지에 따라 아이콘 분기
                    Icon(
                        imageVector = if (baseInfo.isBus) Icons.Default.DirectionsBus else Icons.Default.Place,
                        contentDescription = "타입 아이콘",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = baseInfo.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = baseInfo.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 삭제(X) 버튼
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // [하단] 실시간 도착 정보 영역
            if (arrivals.isEmpty()) {
                Text(
                    text = "도착 예정 정보가 없습니다 (운행 종료 또는 대기 중)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // 상위 2대까지만 보여주도록 제한 (전광판 UI)
                arrivals.take(2).forEach { arrival ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)) // 터치 효과(Ripple) 모양 다듬기
                            .clickable { onRouteClick(arrival.routeId, arrival.routeNo) } // ★ 도착 버스 개별 클릭 영역
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 버스 번호 (정류장을 즐겨찾기 했을 경우 여러 버스가 올 수 있으므로 명시)
                        Text(
                            text = arrival.routeNo,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 도착 시간 (초 단위 -> 분 단위 변환)
                        val timeText = when {
                            arrival.arrTime < 0 -> arrival.arrivalState ?: "운행 대기"
                            arrival.arrTime <= 60 -> "곧 도착"
                            else -> "${arrival.arrTime / 60}분 후"
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${arrival.arrPrevStationCnt}정거장 전",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error // 눈에 띄는 빨간색
                            )
                        }
                    }
                }
            }
        }
    }
}