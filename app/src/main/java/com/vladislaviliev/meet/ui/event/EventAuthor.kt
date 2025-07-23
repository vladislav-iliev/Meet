package com.vladislaviliev.meet.ui.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme

@Composable
internal fun EventAuthor(imgUri: String, name: String, occupation: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = imgUri,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(MaterialTheme.shapes.medium),
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_background),
            contentScale = ContentScale.FillWidth,
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(occupation, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MeetTheme {
        EventAuthor("", "Vlad", "Student")
    }
}