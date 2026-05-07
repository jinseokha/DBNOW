package com.devseok.dbnow.data.model

/**
 * 공공데이터포털의 고질적인 JSON 파싱 문제를 해결하는 커스텀 파서
 */
/*
class BusItemsDeserializer : JsonDeserializer<BusItems> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BusItems {
        if (json.isJsonPrimitive) return BusItems(emptyList())

        val jsonObject = json.asJsonObject
        val combinedList = mutableListOf<BusItem>()

        // 대구 API가 사용할 가능성이 있는 모든 배열 키값 목록
        val possibleKeys = listOf("item", "bs", "ro", "route", "rt")

        // 각각의 키값이 존재하는지 확인하고, 있으면 전부 combinedList에 합칩니다.
        possibleKeys.forEach { key ->
            val element = jsonObject.get(key)
            if (element != null) {
                if (element.isJsonArray) {
                    element.asJsonArray.forEach {
                        combinedList.add(context.deserialize(it, BusItem::class.java))
                    }
                } else if (element.isJsonObject) {
                    // 결과가 딱 1개일 때의 방어 로직
                    combinedList.add(context.deserialize(element, BusItem::class.java))
                }
            }
        }

        // 정류장과 버스 노선이 하나로 합쳐진 리스트를 반환!
        return BusItems(itemList = combinedList)
    }
}*/
