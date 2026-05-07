package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.model.BusArrivalInfo
import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.BusRepository
import com.devseok.dbnow.domain.repository.FavoriteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetFavoriteBusArrivalsUseCase @Inject constructor (
    private val busRepository: BusRepository,
    private val favoriteRepository: FavoriteRepository
) {
    /**
     * @param favorites 로컬 DB(Room/DataStore) 등에서 불러온 사용자의 즐겨찾기 목록
     * @return 즐겨찾기 아이템을 Key로, 해당 아이템의 도착 정보 리스트를 Value로 가지는 Map
     */
    suspend operator fun invoke(favorites: List<SearchResultItem>): Map<SearchResultItem, List<BusArrivalInfo>> {
        // coroutineScope: 내부의 비동기 작업(async)들이 모두 완료될 때까지 안전하게 대기하는 영역
        return coroutineScope {
            val deferredResults = favorites.map { favoriteItem ->
                // ★ 핵심: 각각의 즐겨찾기 항목에 대해 병렬로 API 호출을 시작합니다.
                async {
                    val arrivals = fetchArrivalFor(favoriteItem)
                    // (Key, Value) 쌍으로 반환
                    favoriteItem to arrivals
                }
            }

            // 모든 병렬 통신이 끝날 때까지 기다렸다가 하나의 Map으로 묶어 반환합니다.
            deferredResults.awaitAll().toMap()
        }
    }

    private suspend fun fetchArrivalFor(item: SearchResultItem): List<BusArrivalInfo> {
        val stationId = item.stationId

        // 1. 역(Station) 정보가 없는 경우:
        // 통합 검색에서 "706번 버스" 자체를 즐겨찾기 한 경우 등입니다.
        // 정류장이 특정되지 않았으므로 '도착 정보'를 가져올 수 없어 빈 리스트를 반환합니다.
        if (stationId.isNullOrEmpty()) {
            return emptyList()
        }

        // 2. 특정 정류장의 "특정 버스"를 즐겨찾기 한 경우:
        // 바텀 시트 안에서 특정 노선의 별을 눌렀을 때 (isBus == true && stationId 존재)
        if (item.isBus && !item.routeId.isNullOrEmpty()) {
            val result = busRepository.getBusArrival(stationId, item.routeId)
            val arrivalInfo = result.getOrNull()

            // 정보가 있으면 리스트에 담아주고, 없으면(운행 종료 등) 빈 리스트 반환
            return if (arrivalInfo != null) listOf(arrivalInfo) else emptyList()
        }

        // 3. "정류장" 전체를 즐겨찾기 한 경우:
        // 해당 정류장 전광판에 뜨는 모든 버스의 도착 정보를 가져옵니다. (isBus == false)
        val result = busRepository.getRealtime02(stationId)
        return result.getOrDefault(emptyList())
    }
}