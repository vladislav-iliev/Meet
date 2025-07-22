package com.vladislaviliev.meet.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.vladislaviliev.meet.network.repositories.feed.FeedRepository

internal class FeedViewModel(repository: FeedRepository, pagingConfig: PagingConfig) : ViewModel() {
    val feed = Pager(pagingConfig, pagingSourceFactory = repository::pagingSource).flow.cachedIn(viewModelScope)
}