package com.vladislaviliev.meet.ui.chips.big

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vladislaviliev.meet.ui.event.EventProfilePics

@Composable
private fun BigChip(
    data: BigChipData,
    description: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false
) {
    val contents: @Composable () -> Unit = {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            data.icon()
            Spacer(Modifier.width(12.dp))
            Column {
                Text(data.text, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                description()
            }
        }
    }

    if (outlined) {
        Card(modifier.fillMaxWidth()) { contents() }
        return
    }
    OutlinedCard({}, modifier.fillMaxWidth()) { contents() }
}

@Composable
internal fun BigChip(data: BigChipData, modifier: Modifier = Modifier, outlined: Boolean = false) {
    BigChip(data, { Text(data.description, style = MaterialTheme.typography.bodyMedium) }, modifier, outlined)
}

@Composable
internal fun BigChipParticipants(data: BigChipData, uris: Iterable<String>, modifier: Modifier = Modifier) {
    BigChip(data, { EventProfilePics(uris) }, modifier)
}