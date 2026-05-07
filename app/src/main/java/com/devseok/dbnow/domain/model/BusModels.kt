package com.devseok.dbnow.domain.model

data class SearchResultItem(
    val id: String,         // routeId 또는 nodeId (★ 노선도용일 때는 "bsId_seq" 조합 사용 권장)
    val title: String,      // 버스 번호 또는 정류장 이름
    val subtitle: String,   // 버스 타입 또는 정류장 번호
    val isBus: Boolean,

    // 2. 실시간 API 호출용 식별자 필드
    val stationId: String? = null, // 정류장 고유 ID
    val routeId: String? = null,   // 버스 노선 고유 ID
    val busType: String? = null,   // 버스 타입 (간선, 지선, 급행 등)

    // ★ [신규 추가] 노선도 타임라인 정차 순번 (getBs의 seq 매핑)
    // 상행/하행에서 중복되는 정류장을 완벽하게 구분하기 위한 핵심 키
    val seq: Int? = null
)

data class BusArrivalInfo(
    val routeId: String,
    val routeNo: String,
    val stationId: String,
    val arrTime: Int,
    val arrPrevStationCnt: Int,
    val vehicleType: String,

    val currentStationName: String? = null,
    val arrivalState: String? = null
) {
    val isLowFloor: Boolean
        get() = vehicleType.contains("저상")
}

// [수정됨] 버스 실시간 위치 모델 (getPos)
data class BusPosition(
    val nodeId: String,
    val vehId: String,
    val gpsLati: Double,
    val gpsLong: Double,

    // ★ [신규 추가] 현재 버스가 전체 노선 중 "몇 번째" 정류장에 있는지 (getPos의 seq 매핑)
    // 노선도 UI에서 버스 아이콘을 정확한 위치에 찍기 위한 필수 필드
    val seq: Int? = null
)

// [신규] 노선 경로(지도 그리기용) 모델 (getLink02) - 기존 코드 유지 (완벽함)
data class RouteLink(
    val routeId: String,
    val linkId: String,
    val linkSequence: Int,
    val path: List<Coordinate>
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)