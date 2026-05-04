package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.model.SearchResultItem
import com.devseok.dbnow.domain.repository.BusRepository
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val busRepository: BusRepository
) {
    suspend operator fun invoke(query: String): Result<List<SearchResultItem>> {
        // 1. 비즈니스 검증: 검색어 공백 제거
        val trimmedQuery = query.trim()

        // 2. 빈 문자열인 경우 API를 낭비하지 않고 즉시 성공(빈 리스트) 처리
        if (trimmedQuery.isEmpty()) {
            return Result.success(emptyList())
        }

        // 3. Repository 호출
        // Data 레이어(Impl)에서 이미 Result 래핑과 데이터 가공을 완벽히 끝냈으므로,
        // UseCase는 그 결과를 안전하게 받아 UI(ViewModel)로 전달하기만 합니다.
        return busRepository.searchBusAndStations(trimmedQuery)
    }
}