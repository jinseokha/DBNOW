package com.devseok.dbnow.data.repository

import android.util.Log
import com.devseok.dbnow.data.api.BusApiService
import com.devseok.dbnow.domain.model.ArrivalInfo
import com.devseok.dbnow.domain.repository.BusRepository
import javax.inject.Inject
import javax.inject.Named

class BusRepositoryImpl @Inject constructor(
    private val apiService: BusApiService,
    @Named("serviceKey") private val apiKey: String // Hilt로 주입받는 API 키
) : BusRepository {
    override suspend fun getArrivalInfo(stationId: String, busId: String): ArrivalInfo {
        return try {
            // 1. API 호출 (타임아웃이나 네트워크 에러 대비)
            val response = apiService.getArrivalInfo(apiKey, stationId)

            // 2. 리스트가 비어있거나 body가 null인 경우 처리
            val ticketList = response.body?.list
                ?: throw Exception("해당 정류장에 운행 중인 버스 정보가 없습니다.")

            // 3. 내가 찾는 busId(노선 ID)가 있는지 확인
            val ticket = ticketList.find { it.routeId == busId }
                ?: throw Exception("현재 이 버스의 실시간 정보를 확인할 수 없습니다.")

            // 4. 안전하게 도메인 모델로 변환 (기존에 작성한 toDomain 확장함수 활용 권장)
            ArrivalInfo(
                remainingTime = ticket.arrTime ?: 0,
                remainingStations = ticket.arrOrder ?: 0, // 0 대신 실제 필드 매핑
                isLowFloor = ticket.busType == "1",
                isLastBus = ticket.lastBus == "Y",
                currentStationName = ticket.currentStationName ?: "정보 없음"
            )
        } catch (e: Exception) {
            // 5. 에러 발생 시 로그를 남기거나 기본 빈 정보를 반환하여 앱 크래시 방지
            Log.e("BusRepository", "Error fetching arrival info: ${e.message}")
            // 에러를 상위(ViewModel)로 던지거나, 실패 상태의 객체를 반환
            throw e
        }
    }
}