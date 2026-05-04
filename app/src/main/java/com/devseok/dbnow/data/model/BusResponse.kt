package com.devseok.dbnow.data.model

import com.google.gson.annotations.SerializedName

data class BusResponse(
    @SerializedName("response") val responseWrapper: BusResponseWrapper? = null,
    @SerializedName("header") val header: BusHeader? = null,
    @SerializedName("body") val body: BusBody? = null
)

data class BusResponseWrapper(
    @SerializedName("header") val header: BusHeader? = null,
    @SerializedName("body") val body: BusBody? = null
)

data class BusHeader(
    @SerializedName("resultCode") val resultCode: String? = null,
    @SerializedName("resultMsg") val resultMsg: String? = null
)

data class BusBody(
    @SerializedName("items") val items: BusItems? = null
)

data class BusItems(
    // 파서가 모든 데이터를 여기에 몰아넣어 줍니다.
    @SerializedName("item") val itemList: List<BusItem>? = null
)

data class BusItem(
    // 버스 노선 식별자 (routeId 또는 roId)
    @SerializedName(value = "routeId", alternate = ["roId"])
    val routeId: String? = null,

    // 버스 번호 (routeNo 또는 roNo)
    @SerializedName(value = "routeNo", alternate = ["roNo"])
    val routeNo: String? = null,

    @SerializedName("nodeId") val nodeId: String? = null,
    @SerializedName("bsId") val bsId: String? = null,

    @SerializedName(value = "nodeNm", alternate = ["bsNm"])
    val nodeNm: String? = null,

    @SerializedName("nodeNo") val nodeNo: String? = null,
    @SerializedName("arrTime") val arrTime: Int = 0,
    @SerializedName("arrPrevStationCnt") val arrPrevStationCnt: Int = 0,
    @SerializedName("arrState") val arrState: String? = null,
    @SerializedName("busType") val busType: String? = null,

    @SerializedName(value = "gpsLati", alternate = ["yPos"])
    val gpsLati: Double = 0.0,

    @SerializedName(value = "gpsLong", alternate = ["xPos"])
    val gpsLong: Double = 0.0,

    @SerializedName("busNo") val busNo: String? = null
)