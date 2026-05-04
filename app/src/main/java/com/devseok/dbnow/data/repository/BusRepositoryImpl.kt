package com.devseok.dbnow.data.repository

import android.util.Log
import com.devseok.dbnow.data.api.BusApiService
import com.devseok.dbnow.data.model.BusItem
import com.devseok.dbnow.data.model.BusResponse
import com.devseok.dbnow.domain.model.ArrivalInfo
import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.BusCacheData
import com.devseok.dbnow.domain.model.BusPosition
import com.devseok.dbnow.domain.model.RouteLink
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.BusRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
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

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun extractItems(response: Response<BusResponse>): List<BusItem> {
        if (!response.isSuccessful) throw Exception("API 통신 에러: ${response.code()}")

        val responseBody = response.body() ?: return emptyList()

        // 껍데기가 있는 표준 규격이든, 껍데기가 없는 대구 규격이든 itemList만 꺼내면 끝!
        return responseBody.responseWrapper?.body?.items?.itemList
            ?: responseBody.body?.items?.itemList
            ?: emptyList()
    }

    /**
     * 1 & 2. 통합 검색: 코루틴을 이용해 버스와 정류장 검색을 병렬(Parallel)로 수행하여 속도를 2배 높입니다.
     */
    override suspend fun searchBusAndStations(query: String): Result<List<SearchResultItem>> = runCatching {
        // 전체 마스터 데이터 불러오기 (캐시 또는 API)
        val masterData = getCachedOrFetchMasterData()

        // 검색어 기반 로컬 필터링 (버스 번호 또는 정류장 이름)
        val filteredData = masterData.filter { item ->
            val matchRoute = item.routeNo?.contains(query) == true
            val matchStation = item.nodeNm?.contains(query) == true
            matchRoute || matchStation
        }

        // 3. API 특성에 맞춰 노선(Bus)과 정류장(Station)을 분리하여 매핑
        val busResults = filteredData
            .filter { it.routeNo?.contains(query) == true }
            .distinctBy { it.routeId } // 중복 제거
            .map {
                SearchResultItem(
                    id = it.routeId ?: "", // 이 ID를 나중에 getRealtime02에 사용!
                    title = it.routeNo ?: "",
                    subtitle = "버스 노선",
                    isBus = true
                )
            }

        val stationResults = filteredData
            .filter { it.nodeNm?.contains(query) == true }
            .distinctBy { it.nodeId ?: it.bsId } // API에 따라 nodeId나 bsId 사용
            .map {
                SearchResultItem(
                    id = it.nodeId ?: it.bsId ?: "", // 이 ID를 나중에 getRealtime02에 사용!
                    title = it.nodeNm ?: "",
                    subtitle = "정류장",
                    isBus = false
                )
            }

        // 노선 결과가 먼저, 정류장 결과가 뒤에 나오도록 합쳐서 반환
        busResults + stationResults
    }

    /**
     * 3-1. 특정 정류장의 전체 도착 정보
     */
    override suspend fun getStationArrivals(stationId: String): Result<List<BusArrivalInfo>> = runCatching {
        val response = busApiService.getStationArrivalList(serviceKey, stationId)

        extractItems(response).map {
            BusArrivalInfo(
                remainingTime = it.arrTime,
                remainingStations = it.arrPrevStationCnt,
                isLowFloor = it.busType == "1", // 1: 저상, 2: 일반 등 API 규격에 맞춤
                busNumber = it.routeNo ?: ""
            )
        }
    }

    /**
     * 3-2. 특정 정류장의 특정 버스 도착 정보 (전체 목록을 가져온 후 앱에서 필터링)
     */
    override suspend fun getBusArrival(stationId: String, routeId: String): Result<BusArrivalInfo> = runCatching {
        val response = busApiService.getStationArrivalList(serviceKey, stationId)

        val targetBus = extractItems(response).firstOrNull { it.routeId == routeId }
            ?: throw Exception("도착 예정 정보가 없습니다.")

        BusArrivalInfo(
            remainingTime = targetBus.arrTime,
            remainingStations = targetBus.arrPrevStationCnt,
            isLowFloor = targetBus.busType == "1",
            busNumber = targetBus.routeNo ?: ""
        )
    }

    /**
     * 4. 노선별 차량 실시간 위치
     */
    override suspend fun getBusPositions(routeId: String): Result<List<BusPosition>> = runCatching {
        val response = busApiService.getBusPositions(serviceKey, routeId)

        extractItems(response).map {
            BusPosition(
                busNo = it.busNo ?: "번호 미상",
                latitude = it.gpsLati,
                longitude = it.gpsLong,
                nodeNm = it.nodeNm ?: "위치 불명"
            )
        }
    }

    /**
     * 5. 노선 경로 (지도 폴리라인 그리기용)
     */
    override suspend fun getRouteLinks(routeId: String): Result<List<RouteLink>> = runCatching {
        val response = busApiService.getRouteLinks(serviceKey, routeId)

        extractItems(response).map {
            RouteLink(
                latitude = it.gpsLati,
                longitude = it.gpsLong
            )
        }
    }

    private suspend fun getCachedOrFetchMasterData(): List<BusItem> {
        val documentId = "${getTodayDateString()}_master_data"
        // 단일 문서가 아닌 'chunks'라는 서브 컬렉션을 사용합니다.
        val cacheCollectionRef = firestore.collection("bus_master_cache")
            .document(documentId)
            .collection("chunks")

        // 1. 파이어베이스 캐시 조회 (분할된 문서들을 한 번에 긁어옴)
        val snapshot = try {
            cacheCollectionRef.get().await()
        } catch (e: Exception) {
            println("로그: 오프라인이거나 캐시 읽기 실패 -> ${e.message}")
            null
        }

        if (snapshot != null && !snapshot.isEmpty) {
            // 여러 문서에 쪼개져 있는 items 리스트를 하나로 합침(flatMap)
            val cachedData = snapshot.documents.flatMap { doc ->
                doc.toObject(BusCacheData::class.java)?.items ?: emptyList()
            }
            if (cachedData.isNotEmpty()) {
                println("로그: Firestore에서 마스터 캐시를 완벽히 불러왔습니다. (총 ${cachedData.size}개)")
                return cachedData
            }
        }

        // 2. 캐시가 없으면 getBasic02 API 호출 (전체 리스트 가져오기)
        println("로그: 마스터 캐시가 없어 API를 호출합니다.")
        val response = busApiService.searchBusRoute(serviceKey)
        val apiItems = extractItems(response)

        // 3. 백그라운드에서 Firestore에 '분할' 저장 (1MB 용량 초과 방어)
        if (apiItems.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    // 코틀린의 chunked 함수로 리스트를 800개씩 쪼갬 (약 0.5MB씩 안전하게 분할)
                    val chunkedList = apiItems.chunked(800)

                    chunkedList.forEachIndexed { index, chunk ->
                        cacheCollectionRef.document("part_$index").set(
                            BusCacheData(items = chunk, savedAt = System.currentTimeMillis())
                        ).await()
                    }
                }.onSuccess {
                    println("로그: 마스터 캐시를 Firestore에 성공적으로 분할 저장했습니다. (총 ${apiItems.size}개)")
                }.onFailure { e ->
                    println("로그: 🚨 파이어베이스 저장 실패 -> ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        return apiItems
    }

}