package com.devseok.dbnow.data.repository

import com.devseok.dbnow.data.api.BusApiService
import com.devseok.dbnow.data.model.BusNode
import com.devseok.dbnow.data.model.BusRoute
import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.RouteLink
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.BusRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class BusRepositoryImpl @Inject constructor(
    private val busApiService: BusApiService,
    private val firestore: FirebaseFirestore,
    @Named("serviceKey") private val serviceKey: String // Hilt로 주입받는 API 키
) : BusRepository {

    // ★ 최적화: 버스 노선(Route)과 정류소(Node)를 각각 독립적으로 캐싱합니다.
    private var inMemoryRoutes: List<BusRoute>? = null
    private var inMemoryNodes: List<BusNode>? = null

    private val gson = com.google.gson.Gson()

    /**
     * 1. 버스 번호 및 정류장 통합 검색
     */
    override suspend fun searchBusAndStations(query: String): Result<List<SearchResultItem>> = runCatching {
        withContext(Dispatchers.Default) {
            ensureMasterDataLoaded()

            val matchedRoutes = inMemoryRoutes?.filter { it.routeNo?.contains(query) == true }?.map { route ->
                SearchResultItem(
                    id = route.routeId ?: "",
                    title = route.routeNo ?: "이름 없음",
                    subtitle = formatRouteSubtitle(route.stNm, route.edNm),
                    isBus = true, routeId = route.routeId, stationId = null
                )
            } ?: emptyList()

            val matchedNodes = inMemoryNodes?.filter { it.nodeNm?.contains(query) == true }?.map { node ->
                SearchResultItem(
                    id = node.nodeId ?: "",
                    title = node.nodeNm ?: "이름 없음",
                    subtitle = "정류소",
                    isBus = false, routeId = null, stationId = node.nodeId
                )
            } ?: emptyList()

            (matchedRoutes + matchedNodes).distinctBy { it.id }
        }
    }

    /**
     * 2. 정류소 클릭 시 -> 전체 도착 정보 조회 (getRealtime02)
     */
    override suspend fun getRealtime02(stationId: String): Result<List<BusArrivalInfo>> = runCatching {
        withContext(Dispatchers.IO) {
            val response = busApiService.getRealtime(serviceKey, stationId)
            if (!response.isSuccessful) throw Exception("도착 정보 API 통신 에러: ${response.code()}")

            val arrivalList = response.body()?.body?.items?.arrList ?: emptyList()

            arrivalList.map { item ->
                BusArrivalInfo(
                    routeId = item.routeId ?: "",
                    routeNo = item.routeNo ?: "",
                    stationId = stationId,
                    arrTime = item.arrTime ?: -1,
                    arrPrevStationCnt = item.bsGap ?: 0,
                    vehicleType = item.busTCd2 ?: "일반",
                    currentStationName = item.bsNm,
                    arrivalState = item.arrState
                )
            }
                .filter {
                    // ★ 막차 종료 처리: 아예 "운행종료" 상태인 버스는 전광판 리스트에서 빼버립니다.
                    it.arrivalState != "운행종료"
                }
                .sortedBy {
                    // ★ 정렬 처리: 도착 시간이 있는 버스(정상 운행)를 최우선으로, -1(대기 중)은 맨 뒤로 보냅니다.
                    if (it.arrTime < 0) Int.MAX_VALUE else it.arrTime
                }
        }
    }

    override suspend fun getBusArrival(stationId: String, routeId: String): Result<BusArrivalInfo?> = runCatching {
        val arrivals = getRealtime02(stationId).getOrThrow()
        arrivals.find { it.routeId == routeId }
    }

    /**
     * 3. 버스 클릭 시 [1] -> 해당 노선의 경유 정류소 목록 (getBs)
     */
    override suspend fun getBs(routeId: String): Result<List<SearchResultItem>> = runCatching {
        withContext(Dispatchers.IO) {
            val response = busApiService.getBs(serviceKey, routeId)
            if (!response.isSuccessful) throw Exception("노선별 경유 정류소(getBs) 조회 실패: ${response.code()}")

            val routeStationList = response.body()?.body?.items?.routeStations ?: emptyList()

            routeStationList.map { station ->
                SearchResultItem(
                    id = "${station.bsId}_${station.seq}", // ★ 중복 삭제 방지
                    title = station.bsNm ?: "알 수 없음",
                    // 진행 방향(moveDir)과 정차 순번(seq)을 활용하여 디테일한 서브타이틀 구성
                    subtitle = "${station.seq ?: "0"}번 정차 • ${station.moveDir ?: "진행"} 방면",
                    isBus = false,
                    routeId = routeId,
                    stationId = station.bsId,
                    busType = null,
                    seq = station.seq?.toIntOrNull() // ★ 추가된 필드 매핑
                )
            }.distinctBy { it.id } // 중복 제거
        }
    }

    /**
     * 4. 버스 클릭 시 [2] -> 현재 달리고 있는 버스 위치 목록 (getPos)
     */
    override suspend fun getPos(routeId: String): Result<List<BusPosition>> = runCatching {
        withContext(Dispatchers.IO) {
            val response = busApiService.getPos(serviceKey, routeId)

            if (!response.isSuccessful) {
                throw Exception("실시간 버스 위치(getPos) 조회 실패: ${response.code()}")
            }

            // DTO에서 posList를 안전하게 빼옵니다.
            val posList = response.body()?.body?.items?.posList ?: emptyList()

            // DTO -> 도메인 모델로 변환
            posList.map { pos ->
                BusPosition(
                    // 현재 버스가 있는 정류소 ID (노선도 타임라인에서 버스 아이콘을 띄울 위치 판별에 사용됨)
                    nodeId = pos.bsId ?: "",

                    vehId = pos.vhcNo2 ?: "", // 차량 번호

                    // 지도에 마커를 찍을 때 사용 (yPos가 위도, xPos가 경도입니다)
                    gpsLati = pos.yPos ?: 0.0,
                    gpsLong = pos.xPos ?: 0.0
                )
            }
        }
    }

    override suspend fun getRouteLinks(routeId: String): Result<List<RouteLink>> = runCatching {
        emptyList()
    }

    // =========================================================================
    // Private 헬퍼 함수
    // =========================================================================

    private suspend fun ensureMasterDataLoaded() {
        // 1. 메모리에 데이터가 이미 로드되어 있다면 바로 리턴 (가장 빠른 응답)
        if (!inMemoryRoutes.isNullOrEmpty() && !inMemoryNodes.isNullOrEmpty()) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // 2. Firestore 캐시 확인
                // 대구 버스 데이터는 1MB 제한을 피하기 위해 노선(routes)과 정류소(nodes) 문서를 분리하는 것이 좋습니다.
                val masterDataRef = firestore.collection("bus_master_data")
                val routeDoc = masterDataRef.document("routes").get().await()
                val nodeDoc = masterDataRef.document("nodes").get().await()

                // Firestore에 오늘 자(또는 최신) 캐시가 존재하는 경우
                if (routeDoc.exists() && nodeDoc.exists()) {
                    val routesJson = routeDoc.getString("data")
                    val nodesJson = nodeDoc.getString("data")

                    if (!routesJson.isNullOrBlank() && !nodesJson.isNullOrBlank()) {
                        // JSON String -> List<DTO> 로 변환하여 메모리에 적재
                        val routeType = object : com.google.gson.reflect.TypeToken<List<BusRoute>>() {}.type
                        val nodeType = object : com.google.gson.reflect.TypeToken<List<BusNode>>() {}.type

                        inMemoryRoutes = gson.fromJson(routesJson, routeType)
                        inMemoryNodes = gson.fromJson(nodesJson, nodeType)
                        return@withContext // 성공적으로 로드했으므로 종료
                    }
                }

                // 3. Firestore에 캐시가 없거나 비어있는 경우 -> 공공데이터 API 직접 호출
                // 주의: 아래 API 함수명(getAllRoutes, getAllNodes)은 작성하신 ApiService 명세에 맞게 수정해주세요.
                val routeResponse = busApiService.getBasic(serviceKey)
                val nodeResponse = busApiService.getBasic(serviceKey)

                val fetchedRoutes = routeResponse.body()?.body?.items?.route ?: emptyList()
                val fetchedNodes = nodeResponse.body()?.body?.items?.node ?: emptyList()

                // 메모리에 적재
                inMemoryRoutes = fetchedRoutes
                inMemoryNodes = fetchedNodes

                // 4. 다음번 실행과 다른 유저들을 위해 Firestore에 데이터 캐싱 (비동기 Fire-and-Forget)
                if (fetchedRoutes.isNotEmpty() && fetchedNodes.isNotEmpty()) {
                    val today = getTodayDateString()

                    // Firestore 문서 1MB 용량 초과와 복잡한 인덱싱 방지를 위해 전체 리스트를 JSON String으로 통째로 저장합니다.
                    val routeData = mapOf(
                        "updatedAt" to today,
                        "data" to gson.toJson(fetchedRoutes)
                    )
                    val nodeData = mapOf(
                        "updatedAt" to today,
                        "data" to gson.toJson(fetchedNodes)
                    )

                    // await()를 걸지 않아 사용자 검색 속도에 영향을 주지 않고 백그라운드에서 조용히 업로드됩니다.
                    masterDataRef.document("routes").set(routeData)
                    masterDataRef.document("nodes").set(nodeData)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 오류나 파싱 에러 발생 시, 앱이 죽는 것을 방지하고 빈 검색 결과를 내보내도록 처리
                if (inMemoryRoutes == null) inMemoryRoutes = emptyList()
                if (inMemoryNodes == null) inMemoryNodes = emptyList()
            }
        }
    }

    private fun formatRouteSubtitle(stNm: String?, edNm: String?): String {
        return if (!stNm.isNullOrBlank() && !edNm.isNullOrBlank()) "$stNm ↔ $edNm" else "대구 일반 버스"
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }
}