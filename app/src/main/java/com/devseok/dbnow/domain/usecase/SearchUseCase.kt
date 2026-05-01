package com.devseok.dbnow.domain.usecase

import com.devseok.dbnow.domain.repository.BusRepository
import com.devseok.dbnow.ui.search.SearchResultItem
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val busRepository: BusRepository
) {
    suspend operator fun invoke(query: String): Result<List<SearchResultItem>> = runCatching {
        if (query.isBlank()) return@runCatching emptyList()

        // 실무 팁: 숫자 위주면 노선 검색, 한글 위주면 정류장 검색으로 나누어 호출 가능
        // 여기서는 통합 검색으로 가정합니다.
        val response = busRepository.searchBusAndStations(query)

        // API DTO를 UI에서 사용하기 좋은 SearchResultItem으로 매핑
        response.map { item ->
            SearchResultItem(
                id = item.id,
                title = item.name,
                subtitle = if (item.isBus) item.routeType else "정류장 번호: ${item.stationNumber}",
                isBus = item.isBus
            )
        }
    }
}