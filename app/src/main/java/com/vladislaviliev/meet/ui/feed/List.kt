package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.vladislaviliev.meet.R
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun ColumnScope.FeedList(
    onEventClick: (String) -> Unit, items: LazyPagingItems<PostResponseDto>, modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier
            .weight(1f)
            .fillMaxWidth(),
        contentPadding = PaddingValues(dimensionResource(R.dimen.feed_list_padding)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.itemCount, items.itemKey { it.id }) { CardItem(onEventClick, items[it]!!) }
    }
}