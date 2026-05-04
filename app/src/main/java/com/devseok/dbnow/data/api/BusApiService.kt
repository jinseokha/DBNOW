package com.devseok.dbnow.data.api


import com.devseok.dbnow.data.model.BusArrivalResponse
import com.devseok.dbnow.data.model.BusResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    /**
     * 1. 노선 기본 정보 조회 (버스 번호 검색용)
     * 엔드포인트: getBasic02
     * 용도: "706" 같은 버스 번호(routeNo)로 검색하여 노선 ID(routeId) 등을 획득
     */
    @GET("getBasic02")
    suspend fun searchBusRoute(
        @Query("serviceKey", encoded = true) serviceKey: String
    ): Response<BusResponse>

    /**
     * 2. 정류소 정보 조회 (정류장 검색용)
     * 엔드포인트: getBs02
     * 용도: "경북대학교" 같은 정류장 이름(nodeNm)으로 검색하여 정류소 ID(nodeId) 획득
     */
    @GET("getBs02")
    suspend fun searchStations(
        @Query("serviceKey") serviceKey: String,
        @Query("nodeNm") nodeNm: String, // 검색할 정류장 이름
    ): Response<BusResponse>

    /**
     * 3. 실시간 버스 도착 정보 조회
     * 엔드포인트: getRealtime02
     * 용도: 특정 정류장(nodeId)에 도착 예정인 버스 목록 및 남은 시간 조회
     * (특정 노선만 필터링하려면 파라미터에 routeId를 추가하거나, 응답받은 후 앱에서 필터링)
     */
    @GET("getRealtime02")
    suspend fun getStationArrivalList(
        @Query("serviceKey") serviceKey: String,
        @Query("bsId") stationId: String, // 정류소 ID
        // @Query("routeId") routeId: String? = null, // API 지원 여부에 따라 추가
    ): Response<BusResponse>

    /**
     * 4. 노선별 버스 위치 정보 조회 (선택적 사용)
     * 엔드포인트: getPos02
     * 용도: 특정 노선(routeId)을 운행 중인 모든 버스의 현재 위치(어느 정류장 근처인지) 파악
     * (상세 화면에서 '버스들이 어디쯤 오고 있나' 보여줄 때 사용)
     */
    @GET("getPos02")
    suspend fun getBusPositions(
        @Query("serviceKey") serviceKey: String,
        @Query("routeId") routeId: String, // 노선 ID
    ): Response<BusResponse>

    /**
     * 5. 노선 경로(링크) 정보 조회 (선택적 사용)
     * 엔드포인트: getLink02
     * 용도: 지도 위에 노선이 지나가는 선(경로)을 그리기 위한 좌표 데이터 획득
     */
    @GET("getLink02")
    suspend fun getRouteLinks(
        @Query("serviceKey") serviceKey: String,
        @Query("routeId") routeId: String,
    ): Response<BusResponse>
}