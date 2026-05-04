package com.devseok.dbnow.domain.model

import com.devseok.dbnow.data.model.BusItem

data class BusCacheData(
    val items: List<BusItem> = emptyList(),
    val savedAt: Long = 0L
)