package com.devseok.dbnow.ui.search

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.SearchResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToStation: (stationId: String, name: String) -> Unit, // 추가
    onNavigateToBus: (routeId: String, name: String) -> Unit        // 추가
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SearchEvent.OnToastShown)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { viewModel.onEvent(SearchEvent.OnQueryChanged(it)) },
                        placeholder = { Text("버스 번호 또는 정류장 검색") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.onEvent(SearchEvent.OnClearClick)
                                    focusManager.clearFocus()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "지우기")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.onEvent(SearchEvent.OnSearchClick)
                                focusManager.clearFocus()
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // State 프로퍼티 조합에 따른 UI 렌더링 분기
            when {
                state.query.isEmpty() -> {
                    RecentSearchSection(
                        recentSearches = state.recentSearches,
                        onItemClick = { query ->
                            viewModel.onEvent(SearchEvent.OnRecentSearchClick(query))
                            focusManager.clearFocus() // 클릭 시 키보드 내림
                        },
                        onDeleteClick = { query ->
                            viewModel.onEvent(SearchEvent.OnDeleteRecentSearch(query))
                        },
                        onClearAllClick = {
                            viewModel.onEvent(SearchEvent.OnClearRecentSearches)
                        }
                    )
                }

                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.hasSearched && state.results.isEmpty() -> {
                    Text(
                        text = "검색 결과가 없습니다.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.results.isNotEmpty() -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.results) { item ->
                            val isFavorite = state.favoriteIds.contains(item.id)

                            SearchResultItemCard(
                                item = item,
                                isFavorite = isFavorite,
                                onRowClick = {
                                    if (item.isBus) {
                                        val routeId = item.routeId ?: item.id
                                        onNavigateToBus(routeId, item.title)
                                    } else {
                                        val stationId = item.stationId ?: item.id
                                        onNavigateToStation(stationId, item.title)
                                    }
                                },
                                onFavoriteClick = {
                                    // 2. 즐겨찾기 별 클릭 이벤트
                                    viewModel.onEvent(SearchEvent.OnFavoriteClick(item))
                                }
                            )
                            Divider()
                        }
                    }
                }
                else -> {
                    // 초기에 아무것도 검색하지 않았을 때 (hasSearched == false && query.isEmpty)
                    // 여기에 "최근 검색어"나 "추천 검색어" UI를 넣으면 좋습니다.
                }
            }
        }
    }
}

// =========================================================
// 최근 검색어 컴포저블 (새로 추가됨)
// =========================================================
@Composable
fun RecentSearchSection(
    recentSearches: List<String>,
    onItemClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearAllClick: () -> Unit
) {
    if (recentSearches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("최근 검색 기록이 없습니다.", color = Color.Gray)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("최근 검색어", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClearAllClick) {
                Text("전체 삭제", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(recentSearches) { query ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(query) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = query, style = MaterialTheme.typography.bodyLarge)
                    }
                    IconButton(
                        onClick = { onDeleteClick(query) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.Gray)
                    }
                }
            }
        }
    }
}


@Composable
fun SearchResultItemCard(
    item: SearchResultItem,
    isFavorite: Boolean,
    onRowClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() } // ★ 행 전체 클릭 영역
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isBus) Icons.Default.DirectionsBus else Icons.Default.Place,
            contentDescription = null,
            tint = if (item.isBus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // ★ 글자 영역에 weight(1f)를 주어 우측 별 아이콘을 끝으로 밀어냅니다.
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(text = item.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // ★ 우측 끝의 즐겨찾기 추가 버튼 (행 클릭과 겹치지 않는 독립적인 클릭 영역)
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                // 꽉 찬 별일 때는 노란색/주황색 등 강조 색상을 주면 좋습니다.
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}


// =========================================================
// 1. 버스를 눌렀을 때: 타임라인 노선도 & 실시간 버스 위치 UI
// =========================================================
@Composable
fun BusRouteTimelineView(
    routeStations: List<SearchResultItem>,
    busPositions: List<BusPosition>,
    favoriteIds: Set<String>,
    onFavoriteClick: (SearchResultItem) -> Unit
) {
    if (routeStations.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("노선 정보가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn {
        itemsIndexed(routeStations) { index, station ->
            // 현재 역(station.stationId)에 버스(busPositions.nodeId)가 있는지 체크!
            val hasBusHere = busPositions.any { it.nodeId == station.stationId }
            val isFavorite = favoriteIds.contains(station.id)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측 타임라인 그래픽 (선 + 동그라미/버스아이콘)
                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 수직 선 그리기
                    Divider(
                        modifier = Modifier.width(2.dp).height(50.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                    // 버스가 있으면 큼직한 버스 아이콘, 없으면 작은 동그라미 점
                    if (hasBusHere) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "버스 현재 위치",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp).align(Alignment.Center)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.outline
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 정류장 이름
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = station.title, fontWeight = if (hasBusHere) FontWeight.Bold else FontWeight.Normal)
                }

                // 즐겨찾기 버튼
                IconButton(onClick = { onFavoriteClick(station) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// =========================================================
// 2. 정류소를 눌렀을 때: 실시간 도착 예정 버스 리스트 (전광판)
// =========================================================
@Composable
fun StationArrivalBoardView(
    stationArrivals: List<BusArrivalInfo>
) {
    if (stationArrivals.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("도착 예정인 버스가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn {
        items(stationArrivals) { arrival ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 버스 번호 뱃지 (파란색 등 강조)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = arrival.routeNo,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 도착 정보 (예: 5분 후, 3정거장 전)
                Column(modifier = Modifier.weight(1f)) {
                    // 초(Second)를 분으로 변환. 60초 이하면 "곧 도착" 처리
                    //val timeText = if (arrival.arrTime <= 60) "곧 도착" else "${arrival.arrTime / 60}분 후"
                    val timeText = when {
                        // 1. 시간이 -1 이면 API가 보내준 상태 텍스트("회차지대기", "운행대기" 등)를 그대로 띄워줍니다.
                        arrival.arrTime < 0 -> arrival.arrivalState ?: "운행 대기"

                        // 2. 60초 이하 남았으면 "곧 도착"
                        arrival.arrTime <= 60 -> "곧 도착"

                        // 3. 정상적인 시간 계산
                        else -> "${arrival.arrTime / 60}분 후"
                    }

                    Text(text = timeText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Text(text = "${arrival.arrPrevStationCnt}정거장 전", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Divider()
        }
    }
}