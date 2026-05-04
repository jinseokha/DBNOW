package com.devseok.dbnow.domain.repository

import com.devseok.dbnow.domain.model.ArrivalInfo
import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.RouteLink
import com.devseok.dbnow.domain.model.SearchResultItem

interface BusRepository {
    /** 1 & 2. 버스 번호 및 정류장 통합 검색 (getBasic02 + getBs02) */
    suspend fun searchBusAndStations(query: String): Result<List<SearchResultItem>>

    /** 3-1. 특정 정류장의 전체 버스 도착 정보 (getRealtime02) */
    suspend fun getStationArrivals(stationId: String): Result<List<BusArrivalInfo>>

    /** 3-2. 특정 정류장의 "특정 버스" 도착 정보 (getRealtime02 기반 필터링) */
    suspend fun getBusArrival(stationId: String, routeId: String): Result<BusArrivalInfo>

    /** 4. 특정 노선의 실시간 버스 위치 목록 (getPos02) */
    suspend fun getBusPositions(routeId: String): Result<List<BusPosition>>

    /** 5. 특정 노선의 지도 그리기용 경로 좌표 목록 (getLink02) */
    suspend fun getRouteLinks(routeId: String): Result<List<RouteLink>>
}