package com.devseok.dbnow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        RecentSearchEntity::class // ★ 최근 검색어 엔티티 추가
    ],
    version = 2, // ★ 테이블이 추가되었으므로 기존 1에서 2로 올려줍니다.
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    abstract fun recentSearchDao(): RecentSearchDao
}