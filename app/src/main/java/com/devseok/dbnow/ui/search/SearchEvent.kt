package com.devseok.dbnow.ui.search

sealed class SearchEvent {
    data class OnQueryChange(val query: String) : SearchEvent()
    data class AddFavorite(val item: SearchResultItem) : SearchEvent()
}