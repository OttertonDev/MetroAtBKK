package com.ottertondev.metroatbkk.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ottertondev.metroatbkk.R
import com.ottertondev.metroatbkk.data.SchematicMapData
import com.ottertondev.metroatbkk.data.SchematicMapRepository
import com.ottertondev.metroatbkk.data.SchematicStation
import com.ottertondev.metroatbkk.ui.theme.GoogleSansFlexBodyMain
import com.ottertondev.metroatbkk.ui.theme.GoogleSansFlexHeader
import com.ottertondev.metroatbkk.ui.theme.GoogleSansFlexWeight1000

private val MorningSurface = Color(0xFFF8F1FF)
private val SearchPurple = Color(0xFF7654C8)
private val SearchPurplePressed = Color(0xFF6544B8)
private val SoftPurpleCard = Color(0xFFEADCFF)
private val SoftPurpleCardAlt = Color(0xFFF0E5FF)
private val BtsSelectedCard = Color(0xFFD9F7CE)
private val MrtSelectedCard = Color(0xFFDADFFF)
private val RailInk = Color(0xFF21312C)

@Composable
fun MetroExplorerRoute() {
    val stationListState by rememberStationListState()
    var selectedTab by remember { mutableStateOf(MetroTab.Metro) }
    val selectedStations = remember { mutableStateListOf<SchematicStation>() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MorningSurface,
        bottomBar = {
            MetroNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            MetroTab.Metro -> {
                MetroHomeStateContent(
                    state = stationListState,
                    selectedStations = selectedStations,
                    onStationSelected = { station ->
                        if (selectedStations.none { selected -> selected.selectionId == station.selectionId }) {
                            selectedStations += station
                        }
                    },
                    onStationRemoved = { station ->
                        selectedStations.removeAll { selected -> selected.selectionId == station.selectionId }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            MetroTab.Srt -> PlaceholderScreen(
                title = stringResource(R.string.nav_srt),
                message = stringResource(R.string.srt_placeholder),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            MetroTab.Preferences -> PlaceholderScreen(
                title = stringResource(R.string.nav_preferences),
                message = stringResource(R.string.preferences_placeholder),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun rememberStationListState(): State<StationListState> {
    val context = LocalContext.current
    return produceState<StationListState>(
        initialValue = StationListState.Loading,
        key1 = context
    ) {
        value = runCatching {
            SchematicMapRepository(context.assets).load()
        }.fold(
            onSuccess = { data -> StationListState.Success(data) },
            onFailure = { error -> StationListState.Error(error.message) }
        )
    }
}

private sealed interface StationListState {
    data object Loading : StationListState
    data class Success(val data: SchematicMapData) : StationListState
    data class Error(val message: String?) : StationListState
}

@Composable
private fun MetroHomeStateContent(
    state: StationListState,
    selectedStations: List<SchematicStation>,
    onStationSelected: (SchematicStation) -> Unit,
    onStationRemoved: (SchematicStation) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        StationListState.Loading -> LoadingMetroHome(modifier)
        is StationListState.Error -> ErrorMetroHome(message = state.message, modifier = modifier)
        is StationListState.Success -> MetroHomeContent(
            data = state.data,
            selectedStations = selectedStations,
            onStationSelected = onStationSelected,
            onStationRemoved = onStationRemoved,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingMetroHome(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MorningSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = SearchPurple)
            Text(
                text = stringResource(R.string.stations_loading),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorMetroHome(
    message: String?,
    modifier: Modifier = Modifier
) {
    val errorMessage = message ?: stringResource(R.string.stations_error_fallback)

    Box(
        modifier = modifier
            .background(MorningSurface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.stations_error_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun MetroHomeContent(
    data: SchematicMapData,
    selectedStations: List<SchematicStation>,
    onStationSelected: (SchematicStation) -> Unit,
    onStationRemoved: (SchematicStation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.background(MorningSurface),
        contentPadding = PaddingValues(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MetroGreetingHeader()
        }

        if (selectedStations.isNotEmpty()) {
            item {
                SelectedRoutesRow(
                    stations = selectedStations,
                    onRemove = onStationRemoved
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        items(
            items = data.stations,
            key = { station -> station.selectionId }
        ) { station ->
            StationSelectionCard(
                station = station,
                onClick = { onStationSelected(station) }
            )
        }
    }
}

@Composable
private fun MetroGreetingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.metro_greeting, stringResource(R.string.default_user_name)),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = GoogleSansFlexHeader,
                    fontWeight = GoogleSansFlexWeight1000
                ),
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.metro_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }

        StarSearchButton()
    }
}

@Composable
private fun StarSearchButton() {
    Surface(
        modifier = Modifier
            .size(58.dp)
            .shadow(8.dp, StarRoundedShape, clip = false),
        shape = StarRoundedShape,
        color = SearchPurple,
        contentColor = Color.White,
        onClick = {}
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search_content_description),
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
private fun SelectedRoutesRow(
    stations: List<SchematicStation>,
    onRemove: (SchematicStation) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stations.forEach { station ->
            SelectedRouteCard(
                station = station,
                onRemove = { onRemove(station) }
            )
        }
    }
}

@Composable
private fun SelectedRouteCard(
    station: SchematicStation,
    onRemove: () -> Unit
) {
    val isMrt = station.isMrtLine
    val cardColor = if (isMrt) MrtSelectedCard else BtsSelectedCard
    val borderColor = if (isMrt) Color(0xFF8795D8) else Color(0xFF78C65D)
    val iconRes = if (isMrt) R.drawable.ic_mrt else R.drawable.ic_bts_lg

    Surface(
        modifier = Modifier.width(286.dp),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = lineNameResource(station.lineId),
                    style = MaterialTheme.typography.labelMedium,
                    color = RailInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.selected_route_station,
                        station.localizedHeadlineName(),
                        station.key.uppercase()
                    ),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = GoogleSansFlexBodyMain,
                        fontWeight = GoogleSansFlexWeight1000
                    ),
                    color = RailInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.remove_selected_route),
                    tint = Color(0xFF123B5A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StationSelectionCard(
    station: SchematicStation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), clip = false)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = SoftPurpleCard,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StationCodeBadge(station = station)
            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = lineNameResource(station.lineId),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = station.localizedHeadlineName(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = GoogleSansFlexBodyMain,
                        fontWeight = GoogleSansFlexWeight1000
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = station.localizedSupportingName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StationCodeBadge(station: SchematicStation) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = lineColor(station.lineId),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = station.key.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MetroNavigationBar(
    selectedTab: MetroTab,
    onTabSelected: (MetroTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF9F0FF),
        tonalElevation = 0.dp
    ) {
        MetroTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == MetroTab.Preferences) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(tab.contentDescriptionRes)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_train_rounded),
                            contentDescription = stringResource(tab.contentDescriptionRes)
                        )
                    }
                },
                label = { Text(text = stringResource(tab.labelRes)) }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MorningSurface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SoftPurpleCardAlt,
            tonalElevation = 1.dp,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SchematicStation.localizedHeadlineName(): String {
    val isThaiLocale = LocalConfiguration.current.locales.get(0).language == THAI_LANGUAGE_CODE
    return if (isThaiLocale && nameTh.isNotBlank()) nameTh else nameEn
}

@Composable
private fun SchematicStation.localizedSupportingName(): String {
    val isThaiLocale = LocalConfiguration.current.locales.get(0).language == THAI_LANGUAGE_CODE
    return if (isThaiLocale && nameTh.isNotBlank()) nameEn else nameTh
}

@Composable
private fun lineNameResource(lineId: Int): String {
    return when (lineId) {
        1 -> stringResource(R.string.line_name_sukhumvit)
        2 -> stringResource(R.string.line_name_silom)
        3 -> stringResource(R.string.line_name_gold)
        4 -> stringResource(R.string.line_name_yellow)
        5 -> stringResource(R.string.line_name_pink)
        else -> stringResource(R.string.line_name_unknown, lineId)
    }
}

private fun lineColor(lineId: Int): Color {
    return when (lineId) {
        1 -> Color(0xFF65B724)
        2 -> Color(0xFF008C83)
        3 -> Color(0xFFB28B18)
        4 -> Color(0xFFFFD400)
        5 -> Color(0xFFCD4692)
        else -> Color(0xFF1964B7)
    }
}

private val SchematicStation.selectionId: String
    get() = "$lineId-$key"

private val SchematicStation.isMrtLine: Boolean
    get() = lineId >= 4

private enum class MetroTab(
    val labelRes: Int,
    val contentDescriptionRes: Int
) {
    Metro(
        labelRes = R.string.nav_metro,
        contentDescriptionRes = R.string.nav_metro_content_description
    ),
    Srt(
        labelRes = R.string.nav_srt,
        contentDescriptionRes = R.string.nav_srt_content_description
    ),
    Preferences(
        labelRes = R.string.nav_preferences,
        contentDescriptionRes = R.string.nav_preferences_content_description
    )
}

private val StarRoundedShape = RoundedCornerShape(
    topStart = 21.dp,
    topEnd = 27.dp,
    bottomEnd = 21.dp,
    bottomStart = 27.dp
)

private const val THAI_LANGUAGE_CODE = "th"
