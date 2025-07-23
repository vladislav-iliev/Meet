package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.vladislaviliev.meet.R
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun ColumnScope.FeedList(
    onEventClick: (String) -> Unit, items: LazyPagingItems<PostResponseDto>, modifier: Modifier = Modifier
) {
    val onRetryClick = { items.retry() }

    LazyColumn(
        modifier
            .weight(1f)
            .fillMaxWidth(),
        contentPadding = PaddingValues(dimensionResource(R.dimen.feed_list_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.feed_list_padding)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        observeState(items.loadState.prepend, onRetryClick)

        items(items.itemCount, items.itemKey { it.id }) { CardItem(onEventClick, items[it]!!) }

        observeState(items.loadState.append, onRetryClick)

        observeState(items.loadState.refresh, onRetryClick, extraCheck = { items.itemCount == 0 })
    }
}

private fun LazyListScope.loadingIndicator(modifier: Modifier = Modifier) {
    item { CircularProgressIndicator(modifier) }
}

private fun LazyListScope.errorIndicator(onRetryClick: () -> Unit, message: String, modifier: Modifier = Modifier) {
    item {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            Button(onRetryClick) { Text(stringResource(R.string.retry)) }
        }
    }
}

private fun LazyListScope.observeState(
    state: LoadState, onRetryClick: () -> Unit, modifier: Modifier = Modifier, extraCheck: () -> Boolean = { true }
) {
    when (state) {
        is LoadState.Loading -> if (extraCheck()) loadingIndicator(modifier)
        is LoadState.Error -> if (extraCheck()) errorIndicator(onRetryClick, state.error.toString(), modifier)
        else -> {}
    }
}