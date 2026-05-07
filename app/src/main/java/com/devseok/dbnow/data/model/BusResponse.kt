package com.devseok.dbnow.data.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BusResponse(
    @SerializedName("header") val header: BusHeader?,
    @SerializedName("body") val body: BusBody?
)

data class BusHeader(
    @SerializedName("resultCode") val resultCode: String?,
    @SerializedName("resultMsg") val resultMsg: String?,
    @SerializedName("success") val success: String?
)

data class BusBody(
    @SerializedName("totalCount") val totalCount: Int?,

    // 🔥 에러 해결의 핵심: items를 곧바로 객체로 받지 않고, 형태를 알 수 없는 JsonElement로 먼저 받습니다.
    @SerializedName("items") private val _itemsRaw: JsonElement? = null
) {
    // UI나 Repository에서 body.items를 호출할 때 안전하게 가공해서 내보냅니다.
    val items: BusItems
        get() {
            if (_itemsRaw == null || _itemsRaw.isJsonNull) return BusItems()

            // 2. 정상적으로 객체로 온 경우
            if (_itemsRaw.isJsonObject) {
                return Gson().fromJson(_itemsRaw, BusItems::class.java)
            }

            // 3. 🚨 문제의 원인: 서버가 다이렉트로 배열을 던진 경우
            if (_itemsRaw.isJsonArray) {
                // 배열 자체를 모든 하위 변수들에게 쥐어주고, 내부에서 스스로 껍데기를 까도록 위임합니다.
                return BusItems(
                    _node = _itemsRaw,
                    _bs = _itemsRaw,
                    _route = _itemsRaw,
                    _link = _itemsRaw,
                    _arrList = _itemsRaw,
                    _item = _itemsRaw,
                    _pos = _itemsRaw
                )
            }
            return BusItems()
        }
}

data class BusItems(
    @SerializedName("node") private val _node: JsonElement? = null,
    @SerializedName("bs") private val _bs: JsonElement? = null,
    @SerializedName("route") private val _route: JsonElement? = null,
    @SerializedName("link") private val _link: JsonElement? = null,
    @SerializedName("arrList") private val _arrList: JsonElement? = null,
    @SerializedName("item") private val _item: JsonElement? = null,
    @SerializedName("pos") private val _pos: JsonElement? = null,
    @SerializedName("routeNo") val routeNo: String? = null
) {
    // 각각 자신이 찾아야 할 껍데기 이름("arrList", "item" 등)을 알려줍니다.
    val node: List<BusNode> get() = parseSafeList(_node, BusNode::class.java, "node")
    val bs: List<BusStation> get() = parseSafeList(_bs, BusStation::class.java, "bs")
    val route: List<BusRoute> get() = parseSafeList(_route, BusRoute::class.java, "route")
    val link: List<BusLink> get() = parseSafeList(_link, BusLink::class.java, "link")
    val arrList: List<BusArrivalItem> get() = parseSafeList(_arrList, BusArrivalItem::class.java, "arrList")
    val routeStations: List<BusRouteStation> get() = parseSafeList(_item, BusRouteStation::class.java, "item")
    val posList: List<BusPosItem> get() = parseSafeList(_pos, BusPosItem::class.java, "pos")

    /**
     * 🔥 배열 내부에 한 겹 더 씌워진 껍데기를 자동으로 까서 알맹이만 List로 만들어주는 궁극의 헬퍼
     */
    private fun <T> parseSafeList(element: JsonElement?, clazz: Class<T>, unwrapKey: String): List<T> {
        if (element == null || element.isJsonNull) return emptyList()
        val gson = Gson()
        val result = mutableListOf<T>()

        try {
            if (element.isJsonArray) {
                element.asJsonArray.forEach { item ->
                    if (item.isJsonObject) {
                        val obj = item.asJsonObject
                        // 배열 안의 객체에 "arrList" 같은 껍데기가 씌워져 있다면?
                        if (obj.has(unwrapKey)) {
                            val inner = obj.get(unwrapKey) // 껍데기 분리!
                            if (inner.isJsonArray) {
                                inner.asJsonArray.forEach { result.add(gson.fromJson(it, clazz)) }
                            } else if (inner.isJsonObject) {
                                result.add(gson.fromJson(inner, clazz))
                            }
                        } else {
                            // 껍데기가 없으면 바로 변환
                            result.add(gson.fromJson(item, clazz))
                        }
                    }
                }
            } else if (element.isJsonObject) {
                result.add(gson.fromJson(element, clazz))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}

// ==========================================
// 7. 실시간 버스 위치 (getPos API) 정보 모델
// ==========================================
data class BusPosItem(
    @SerializedName("bsId") val bsId: String?,         // 현재 버스가 위치한 정류소 ID
    @SerializedName("vhcNo2") val vhcNo2: String?,     // 차량 번호 (예: 대구70자1234)
    @SerializedName("xPos") val xPos: Double?,         // 경도 (Longitude)
    @SerializedName("yPos") val yPos: Double?,         // 위도 (Latitude)
    @SerializedName("moveDir") val moveDir: String?,   // 진행 방향
    @SerializedName("seq") val seq: Int?,              // 현재 정차 순번
    @SerializedName("busTCd2") val busTCd2: String?,   // 버스 타입 코드
    @SerializedName("arTime") val arTime: String?      // 정보 갱신 시간
)

// ==========================================
// 6. 노선별 경유 정류소 (getBs API) 정보
// ==========================================
data class BusRouteStation(
    @SerializedName("bsId") val bsId: String?,
    @SerializedName("bsNm") val bsNm: String?,
    @SerializedName("seq") val seq: String?,           // 정차 순번 (예: "1", "2")
    @SerializedName("moveDir") val moveDir: String?,   // 진행 방향 (예: "북구청앞", "대곡지구")
    @SerializedName("xPos") val xPos: Double?,
    @SerializedName("yPos") val yPos: Double?
)

// ==========================================
// 1. 정류소 (Node) 정보
// ==========================================
data class BusNode(
    @SerializedName("nodeId") val nodeId: String?,
    @SerializedName("nodeNm") val nodeNm: String?,
    @SerializedName("xPos") val xPos: Double?,
    @SerializedName("yPos") val yPos: Double?,
    @SerializedName("bsYn") val bsYn: String?
)

// ==========================================
// 2. 버스 정류장 (Bus Station) 정보
// ==========================================
data class BusStation(
    @SerializedName("wincId") val wincId: String?,
    @SerializedName("bsId") val bsId: String?,
    @SerializedName("bsNm") val bsNm: String?,
    @SerializedName("xPos") val xPos: Double?,
    @SerializedName("yPos") val yPos: Double?
)

// ==========================================
// 3. 버스 노선 (Route) 정보
// ==========================================
data class BusRoute(
    @SerializedName("dataconnareacd") val dataConnAreaCd: String?,
    @SerializedName("routeId") val routeId: String?,
    @SerializedName("routeNo") val routeNo: String?,
    @SerializedName("stBsId") val stBsId: String?,
    @SerializedName("edBsId") val edBsId: String?,
    @SerializedName("stNm") val stNm: String?,       // 기점 이름
    @SerializedName("edNm") val edNm: String?,       // 종점 이름
    @SerializedName("routeNote") val routeNote: String?,
    @SerializedName("dirRouteNote") val dirRouteNote: String?,
    @SerializedName("ndirRouteNote") val ndirRouteNote: String?,
    @SerializedName("routeTCd") val routeTCd: String?
)

// ==========================================
// 4. 지도 경로 좌표 (Link) 정보
// ==========================================
data class BusLink(
    @SerializedName("linkId") val linkId: String?,
    @SerializedName("linkNm") val linkNm: String?,
    @SerializedName("stNode") val stNode: String?,
    @SerializedName("edNode") val edNode: String?,
    @SerializedName("gisDist") val gisDist: Double?
)

// 실시간 도착 정보 (Arrival) 정보
// ==========================================
data class BusArrivalItem(
    @SerializedName("moveDir") val moveDir: String?,
    @SerializedName("arrState") val arrState: String?,     // 예: "운행중", "회차지대기"
    @SerializedName("prevBsGap") val prevBsGap: Int?,
    @SerializedName("routeId") val routeId: String?,
    @SerializedName("routeNo") val routeNo: String?,
    @SerializedName("routeNoteOrigin") val routeNoteOrigin: String?,
    @SerializedName("bsGap") val bsGap: Int?,              // 남은 정류장 수
    @SerializedName("bsNm") val bsNm: String?,             // 현재 버스가 위치한 정류장 이름
    @SerializedName("vhcNo2") val vhcNo2: String?,         // 차량 번호
    @SerializedName("busTCd2") val busTCd2: String?,       // 버스 타입 코드
    @SerializedName("busTCd3") val busTCd3: String?,
    @SerializedName("busAreaCd") val busAreaCd: String?,
    @SerializedName("arrTime") val arrTime: Int?           // 도착 예정 시간 (초)
)