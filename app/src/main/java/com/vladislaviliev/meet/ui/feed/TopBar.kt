package com.vladislaviliev.meet.ui.feed

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme

@Composable
internal fun FeedTopBar(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth()) {

        val horizontalPadding =
            dimensionResource(R.dimen.feed_list_padding) + dimensionResource(R.dimen.feed_card_padding)
        val verticalPadding = dimensionResource(R.dimen.feed_list_padding)

        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontalPadding, verticalPadding)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                IconButton({}) { Icon(Icons.Filled.Tune, stringResource(R.string.filter)) }
                IconButton({}) { Icon(Icons.Filled.Search, stringResource(R.string.search)) }
                IconButton({}) { Icon(Icons.Filled.Notifications, stringResource(R.string.notifications)) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(true, {}, { Text(stringResource(R.string.events)) })
                FilterChip(false, {}, { Text(stringResource(R.string.clubs)) })
                FilterChip(false, {}, { Text(stringResource(R.string.friends)) })
            }
        }
    }
}

@Preview(
    showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun PreviewCustomTopAppBar() {
    MeetTheme {
        FeedTopBar()
    }
}
