package com.devseok.dbnow.domain.model

data class ArrivalInfo(
    val remainingTime: Int,       // 도착 예정 시간 (초 단위 정수)
    val remainingStations: Int,   // 남은 정류장 수
    val isLowFloor: Boolean,      // 저상 버스 여부 (교통약자 정보)
    val isLastBus: Boolean,       // 막차 여부
    val currentStationName: String // 현재 버스의 위치 (정류장 명칭)
)