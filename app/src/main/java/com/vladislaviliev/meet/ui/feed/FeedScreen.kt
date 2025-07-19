package com.vladislaviliev.meet.ui.feed

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.vladislaviliev.meet.ui.theme.MeetTheme
import kotlinx.coroutines.flow.asFlow
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun FeedScreen(events: LazyPagingItems<PostResponseDto>, modifier: Modifier = Modifier) {
    Surface {
        Column(modifier.fillMaxSize()) {
            FeedTopBar(Modifier.fillMaxWidth())
            FeedList(
                events, Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            FeedBottomBar(Modifier.fillMaxWidth())
        }
    }
}

@Preview(showSystemUi = true, showBackground = false,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun Preview() {
    MeetTheme {
        val data = PagingData.from(emptyList<PostResponseDto>())
        FeedScreen(listOf(data).asFlow().collectAsLazyPagingItems())
    }
}