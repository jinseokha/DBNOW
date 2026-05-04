package com.devseok.dbnow.domain.model

data class SearchResultItem(
    val id: String,         // routeId 또는 nodeId
    val title: String,      // 버스 번호 또는 정류장 이름
    val subtitle: String,   // 버스 타입 또는 정류장 번호
    val isBus: Boolean
)

data class BusArrivalInfo(
    val remainingTime: Int,       // 남은 시간(초)
    val remainingStations: Int,   // 남은 정류장 수
    val isLowFloor: Boolean,      // 저상버스 여부
    val busNumber: String         // 버스 번호
)

// [신규] 버스 실시간 위치 모델 (getPos02)
data class BusPosition(
    val busNo: String,      // 차량 번호판 (예: 대구70자1234)
    val latitude: Double,   // 위도
    val longitude: Double,  // 경도
    val nodeNm: String      // 현재 가장 가까운 정류장
)

// [신규] 노선 경로(지도 그리기용) 모델 (getLink02)
data class RouteLink(
    val latitude: Double,
    val longitude: Double
)