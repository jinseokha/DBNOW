package com.devseok.dbnow.domain.repository

import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.RouteLink
import com.devseok.dbnow.domain.model.SearchResultItem

interface BusRepository {
    // 1. 마스터 데이터 기반 통합 검색
    suspend fun searchBusAndStations(query: String): Result<List<SearchResultItem>>

    // 2. 정류소 클릭 시: 전광판용 실시간 도착 예정 정보 (getRealtime02 API 사용)
    suspend fun getRealtime02(stationId: String): Result<List<BusArrivalInfo>>

    // 3. (선택) 개별 즐겨찾기 카드 갱신용
    suspend fun getBusArrival(stationId: String, routeId: String): Result<BusArrivalInfo?>

    // 4. 버스 노선 클릭 시 [1]: 해당 노선의 전체 경유 정류소 목록 (getBs API 사용)
    suspend fun getBs(routeId: String): Result<List<SearchResultItem>>

    // 5. 버스 노선 클릭 시 [2]: 해당 노선을 달리고 있는 버스들의 현재 위치 (getPos API 사용)
    suspend fun getPos(routeId: String): Result<List<BusPosition>>

    // 6. (선택) 지도 화면 드로잉용
    suspend fun getRouteLinks(routeId: String): Result<List<RouteLink>>
}