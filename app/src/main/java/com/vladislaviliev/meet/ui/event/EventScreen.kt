package com.vladislaviliev.meet.ui.event

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.event.EventScopeRepository
import com.vladislaviliev.meet.network.repositories.event.Event
import com.vladislaviliev.meet.ui.chips.big.BigChip
import com.vladislaviliev.meet.ui.chips.big.BigChipParticipants
import com.vladislaviliev.meet.ui.chips.big.bigChipData
import com.vladislaviliev.meet.ui.chips.overview.OverviewChipType
import com.vladislaviliev.meet.ui.chips.small.FlowRowSmallChips
import com.vladislaviliev.meet.ui.chips.small.SmallChip
import com.vladislaviliev.meet.ui.chips.small.interestChipsData
import com.vladislaviliev.meet.ui.chips.small.smallChipData
import com.vladislaviliev.meet.ui.theme.MeetTheme
import org.koin.compose.getKoin
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.Currency
import org.openapitools.client.models.Interest
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.time.LocalDate
import java.time.OffsetDateTime

@Composable
internal fun EventScreen() {
    val vm = getKoin()
        .get<EventScopeRepository>()
        .currentScope!!
        .get<EventViewModel>()
    val event by vm.repo.event.collectAsStateWithLifecycle()
    EventScreen(event as Event.Success)
}

@Composable
private fun EventScreen(event: Event.Success, modifier: Modifier = Modifier) {
    Surface(modifier) {
        Box {
            Column(Modifier.fillMaxSize()) {
                ScrollableContent(event.postResponseDto, event.participants, Modifier.weight(1f))
                EventBottomBar(Modifier.fillMaxWidth())
            }
            EventTopBar(Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun ScrollableContent(post: PostResponseDto, participantPics: Iterable<String>, modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        EventImage("", Modifier.fillMaxWidth())
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp, 16.dp, 16.dp)
        ) {
            Text(post.title, style = MaterialTheme.typography.headlineMedium)

            EventAuthor(
                post.owner.profilePhotos.first(),
                "${post.owner.firstName} ${post.owner.lastName}",
                post.owner.occupation,
                Modifier.padding(vertical = 10.dp)
            )

            val chipSpacer = Modifier.height(8.dp)

            if (post.fromDate != null)
                BigChip(post.bigChipData(OverviewChipType.Date), Modifier.padding(top = 4.dp))

            Spacer(chipSpacer)
            BigChip(post.bigChipData(OverviewChipType.Location))

            Spacer(chipSpacer)
            BigChipParticipants(post.bigChipData(OverviewChipType.Participants), participantPics)

            Spacer(chipSpacer)
            BigChip(post.bigChipData(OverviewChipType.ConfirmLocation), outlined = true)

            Spacer(chipSpacer)
            BigChip(post.bigChipData(OverviewChipType.Accessibility))

            Spacer(chipSpacer)
            BigChip(post.bigChipData(OverviewChipType.Payment))

            Description(post)
            Location(post)
            EventInterests(post)
        }
    }
}

@Composable
private fun Description(post: PostResponseDto) {
    if (post.description == null) return
    SectionHeader(R.string.description)
    Text(post.description, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun Location(post: PostResponseDto) {
    SectionHeader(R.string.location)
    SmallChip(post.smallChipData(OverviewChipType.Location).copy({ }))
    Spacer(Modifier.height(16.dp))
    Image(
        painterResource(R.drawable.ic_launcher_background),
        null,
        Modifier
            .clip(MaterialTheme.shapes.large)
            .fillMaxWidth()
            .height(300.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun EventInterests(post: PostResponseDto) {
    SectionHeader(R.string.interests)
    FlowRowSmallChips(post.interestChipsData(), Modifier.fillMaxWidth())
}

@Composable
private fun SectionHeader(@StringRes stringRes: Int) {
    Spacer(Modifier.height(dimensionResource(R.dimen.event_section_header_spacer)))
    Text(stringResource(stringRes), style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(dimensionResource(R.dimen.event_section_header_spacer)))
}

@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = false
)
@Composable
fun EventScreenFullPreview() {
    val sampleOwner = MiniUser(
        id = "user1",
        firstName = "Nikolay",
        lastName = "Atanasov",
        profilePhotos = listOf("https://example.com/nikolay.jpg"),
        occupation = "Waiter",
        location = BaseLocation(name = "Sofia", latitude = 0.0, longitude = 0.0),
        birthDate = LocalDate.now().minusYears(30),
        userRole = MiniUser.UserRole.NORMAL
    )
    val sampleInterests = setOf(
        Interest(name = "Gaming", icon = "gaming_icon", category = "Entertainment"),
        Interest(name = "Outdoor", icon = "outdoor_icon", category = "Activity"),
        Interest(name = "Card games", icon = "card_games_icon", category = "Entertainment"),
        Interest(name = "Events", icon = "events_icon", category = "Social"),
        Interest(name = "Restaurants", icon = "restaurants_icon", category = "Food"),
        Interest(name = "Party", icon = "party_icon", category = "Social"),
        Interest(name = "Networking", icon = "networking_icon", category = "Professional"),
        Interest(name = "Escape room", icon = "escape_room_icon", category = "Entertainment"),
        Interest(name = "Board games", icon = "board_games_icon", category = "Entertainment"),
        Interest(name = "Hanging in the park", icon = "hanging_in_park_icon", category = "Activity"),
        Interest(name = "Chess", icon = "chess_icon", category = "Strategy"),
        Interest(name = "Camping", icon = "camping_icon", category = "Activity"),
        Interest(name = "Picnic", icon = "picnic_icon", category = "Activity")
    )
    val samplePost = PostResponseDto(
        id = "1",
        title = "Два дни в Милано",
        images = listOf("https://images.unsplash.com/photo-1522087062904-1c393100e051?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80"),
        location = BaseLocation(
            name = "Milan Cathedral",
            city = "Milano, Lombardy",
            latitude = 45.4642,
            longitude = 9.1900,
            address = "P.za del Duomo, 20122 Milano MI, Italy"
        ),
        createdAt = OffsetDateTime.now(),
        interests = sampleInterests, // Use the correctly typed set
        owner = sampleOwner,
        payment = 0.0,
        currentUserStatus = PostResponseDto.CurrentUserStatus.PARTICIPATING,
        accessibility = PostResponseDto.Accessibility.PUBLIC,
        askToJoin = false,
        needsLocationalConfirmation = true,
        participantsCount = 27,
        status = PostResponseDto.Status.NOT_STARTED,
        savedByCurrentUser = false,
        blockedForCurrentUser = false,
        description = "Здравейте, търся си другарче за Милано! Ще разглеждаме забележителности, ще хапваме вкусна храна и ще се наслаждаваме на атмосферата. Присъединете се!",
        maximumPeople = 50,
        fromDate = OffsetDateTime.now().plusDays(5).withHour(10).withMinute(17),
        toDate = OffsetDateTime.now().plusDays(6).withHour(21).withMinute(27),
        currency = Currency(code = "EUR", name = "EUR", symbol = "€"),
        currentUserRole = null,
        currentUserArrivalStatus = null
    )
    MeetTheme {
        EventScreen(Event.Success(samplePost, listOf("", "", "", "", "")))
    }
}
