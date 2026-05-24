package com.ottertondev.metroatbkk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottertondev.metroatbkk.data.TransitLineKind
import com.ottertondev.metroatbkk.data.TransitMapData
import com.ottertondev.metroatbkk.data.TransitRoute
import com.ottertondev.metroatbkk.data.TransitStation
import com.ottertondev.metroatbkk.data.TransitStationRepository
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.min

private const val MAP_WIDTH = 1000f
private const val MAP_HEIGHT = 1000f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroExplorerRoute() {
    val mapLoadState by rememberMapLoadState()
    var query by rememberSaveable { mutableStateOf("") }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var selectedStationId by rememberSaveable { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val mapData = (mapLoadState as? MapLoadState.Success)?.data
    val stations = mapData?.stations.orEmpty()
    val routesById = remember(mapData?.routes) {
        mapData?.routes.orEmpty().associateBy { route -> route.id }
    }
    val selectedStation = remember(stations, selectedStationId) {
        stations.firstOrNull { station -> station.id == selectedStationId }
    }

    LaunchedEffect(stations) {
        if (selectedStationId != null && selectedStation == null) selectedStationId = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            TransitMapExplorer(
                mapData = mapData,
                selectedStationId = selectedStationId,
                onStationSelected = { station ->
                    selectedStationId = station.id
                    query = station.stationCode
                    searchActive = false
                },
                modifier = Modifier.fillMaxSize()
            )

            FloatingStationSearchBar(
                query = query,
                onQueryChange = { query = it },
                active = searchActive,
                onActiveChange = { searchActive = it },
                mapData = mapData,
                onStationSelected = { station ->
                    selectedStationId = station.id
                    query = station.stationCode
                    searchActive = false
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            )

            RailLegend(
                routes = mapData?.routes.orEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )

            LoadStateOverlay(
                loadState = mapLoadState,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    if (selectedStation != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedStationId = null },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            StationDetailSheet(station = selectedStation, routesById = routesById)
        }
    }
}

@Composable
private fun rememberMapLoadState(): State<MapLoadState> {
    val context = LocalContext.current
    return produceState<MapLoadState>(
        initialValue = MapLoadState.Loading,
        key1 = context
    ) {
        val repository = TransitStationRepository(context.assets)
        value = runCatching {
            repository.loadMapData()
        }.fold(
            onSuccess = { data -> MapLoadState.Success(data) },
            onFailure = { error ->
                MapLoadState.Error(error.message ?: "Unable to load GTFS rail data")
            }
        )
    }
}

private sealed interface MapLoadState {
    data object Loading : MapLoadState
    data class Success(val data: TransitMapData) : MapLoadState
    data class Error(val message: String) : MapLoadState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingStationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    mapData: TransitMapData?,
    onStationSelected: (TransitStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val routesById = remember(mapData?.routes) {
        mapData?.routes.orEmpty().associateBy { route -> route.id }
    }
    val results by remember(query, mapData) {
        derivedStateOf {
            stationSearchResults(
                query = query,
                stations = mapData?.stations.orEmpty(),
                routesById = routesById
            )
        }
    }

    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { submittedQuery ->
            stationSearchResults(submittedQuery, mapData?.stations.orEmpty(), routesById)
                .firstOrNull()
                ?.let(onStationSelected)
        },
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search station, code, or rail line") },
        shape = RoundedCornerShape(32.dp),
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        windowInsets = WindowInsets.statusBars
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 420.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (results.isEmpty()) {
                item {
                    Text(
                        text = if (query.isBlank()) "Try Siam, CEN, Yellow, Pink, Sukhumvit, or ARL" else "No rail station found",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(results, key = { station -> station.id }) { station ->
                    StationSearchResult(
                        station = station,
                        routesById = routesById,
                        onClick = { onStationSelected(station) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StationSearchResult(
    station: TransitStation,
    routesById: Map<String, TransitRoute>,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = station.displayNameEn(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${station.stationCode} - ${station.lineSummary(routesById)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            LineDot(color = station.primaryRouteColor(routesById))
        },
        trailingContent = {
            Text(
                text = station.stationCode,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun TransitMapExplorer(
    mapData: TransitMapData?,
    selectedStationId: String?,
    onStationSelected: (TransitStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedLabelStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Black,
        fontSize = 18.sp,
        color = colorScheme.onPrimaryContainer
    )
    val textMeasurer = rememberTextMeasurer()
    val routesById = remember(mapData?.routes) {
        mapData?.routes.orEmpty().associateBy { route -> route.id }
    }
    val routePaths = remember(mapData, routesById) {
        buildDrawableRoutePaths(mapData = mapData, routesById = routesById)
    }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by rememberSaveable { mutableStateOf(1f) }
    var offsetX by rememberSaveable { mutableStateOf(0f) }
    var offsetY by rememberSaveable { mutableStateOf(0f) }
    val stations = mapData?.stations.orEmpty()

    Canvas(
        modifier = modifier
            .onSizeChanged { viewportSize = it }
            .pointerInput(stations, viewportSize, zoom, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    val mapOffset = tapOffset.toMapOffset(
                        viewportSize = viewportSize,
                        zoom = zoom,
                        offsetX = offsetX,
                        offsetY = offsetY
                    )
                    val tappedStation = stations.minByOrNull { station ->
                        hypot(
                            (station.mapX - mapOffset.x).toDouble(),
                            (station.mapY - mapOffset.y).toDouble()
                        )
                    }
                    val distance = tappedStation?.let { station ->
                        hypot(
                            (station.mapX - mapOffset.x).toDouble(),
                            (station.mapY - mapOffset.y).toDouble()
                        )
                    } ?: Double.MAX_VALUE

                    if (tappedStation != null && distance <= 28f) onStationSelected(tappedStation)
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    zoom = (zoom * zoomChange).coerceIn(0.75f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        val transform = MapTransform.forViewport(size = viewportSize, zoom = zoom, offsetX = offsetX, offsetY = offsetY)

        drawRect(color = colorScheme.surfaceContainerLowest)

        withTransform({
            translate(left = transform.originX, top = transform.originY)
            scale(scaleX = transform.scale, scaleY = transform.scale)
        }) {
            drawRoundRect(
                color = colorScheme.surfaceContainerLow,
                topLeft = Offset.Zero,
                size = Size(MAP_WIDTH, MAP_HEIGHT),
                cornerRadius = CornerRadius(42f, 42f)
            )
            drawSchematicGrid(color = colorScheme.outlineVariant.copy(alpha = 0.35f))

            routePaths.forEach { routePath ->
                drawPath(
                    path = routePath.path,
                    color = routePath.color.copy(alpha = 0.84f),
                    style = Stroke(
                        width = routePath.width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            stations.forEach { station ->
                drawStationNode(
                    station = station,
                    selected = station.id == selectedStationId,
                    lineColor = station.primaryRouteColor(routesById),
                    surfaceColor = colorScheme.surface,
                    selectedContainer = colorScheme.primaryContainer,
                    onSelectedContainer = colorScheme.onPrimaryContainer
                )
            }

            stations.firstOrNull { station -> station.id == selectedStationId }?.let { station ->
                drawSelectedStationLabel(
                    station = station,
                    textMeasurer = textMeasurer,
                    style = selectedLabelStyle,
                    containerColor = colorScheme.primaryContainer,
                    outlineColor = station.primaryRouteColor(routesById)
                )
            }
        }
    }
}

private fun DrawScope.drawSchematicGrid(color: Color) {
    val gridStep = 100f
    var x = 0f
    while (x <= MAP_WIDTH) {
        val isMajorLine = x % 500f == 0f
        drawLine(
            color = color.copy(alpha = if (isMajorLine) 0.95f else 0.55f),
            start = Offset(x, 0f),
            end = Offset(x, MAP_HEIGHT),
            strokeWidth = if (isMajorLine) 2.2f else 1.2f
        )
        x += gridStep
    }

    var y = 0f
    while (y <= MAP_HEIGHT) {
        val isMajorLine = y % 500f == 0f
        drawLine(
            color = color.copy(alpha = if (isMajorLine) 0.95f else 0.55f),
            start = Offset(0f, y),
            end = Offset(MAP_WIDTH, y),
            strokeWidth = if (isMajorLine) 2.2f else 1.2f
        )
        y += gridStep
    }
}

private fun DrawScope.drawStationNode(
    station: TransitStation,
    selected: Boolean,
    lineColor: Color,
    surfaceColor: Color,
    selectedContainer: Color,
    onSelectedContainer: Color
) {
    val center = Offset(station.mapX, station.mapY)
    val isInterchange = station.routeIds.size > 1
    val outerRadius = when {
        selected -> 13f
        isInterchange -> 8.5f
        else -> 5.8f
    }
    val strokeWidth = when {
        selected -> 4.5f
        isInterchange -> 3.6f
        else -> 2.8f
    }

    if (isInterchange) {
        val size = outerRadius * 2.2f
        val topLeft = Offset(center.x - size / 2f, center.y - size / 2f)
        drawRoundRect(
            color = if (selected) selectedContainer else surfaceColor,
            topLeft = topLeft,
            size = Size(size, size),
            cornerRadius = CornerRadius(5.5f, 5.5f)
        )
        drawRoundRect(
            color = lineColor,
            topLeft = topLeft,
            size = Size(size, size),
            cornerRadius = CornerRadius(5.5f, 5.5f),
            style = Stroke(width = strokeWidth)
        )
    } else {
        drawCircle(
            color = if (selected) selectedContainer else surfaceColor,
            radius = outerRadius,
            center = center
        )
        drawCircle(
            color = lineColor,
            radius = outerRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
    }

    if (selected) {
        drawCircle(color = onSelectedContainer, radius = 3.5f, center = center)
    }
}

private fun DrawScope.drawSelectedStationLabel(
    station: TransitStation,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    style: TextStyle,
    containerColor: Color,
    outlineColor: Color
) {
    val label = "${station.stationCode}  ${station.displayNameEn()}"
    val layoutResult = textMeasurer.measure(AnnotatedString(label), style = style)
    val horizontalPadding = 16f
    val verticalPadding = 10f
    val labelWidth = layoutResult.size.width + horizontalPadding * 2f
    val labelHeight = layoutResult.size.height + verticalPadding * 2f
    val topLeft = Offset(
        x = (station.mapX + 22f).coerceAtMost(MAP_WIDTH - labelWidth - 20f),
        y = (station.mapY - labelHeight - 22f).coerceAtLeast(20f)
    )

    drawRoundRect(
        color = containerColor,
        topLeft = topLeft,
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(18f, 18f)
    )
    drawRoundRect(
        color = outlineColor,
        topLeft = topLeft,
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(18f, 18f),
        style = Stroke(width = 2.5f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        style = style,
        topLeft = topLeft + Offset(horizontalPadding, verticalPadding)
    )
}

@Composable
private fun RailLegend(
    routes: List<TransitRoute>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = routes.isNotEmpty(), modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 250.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "GTFS rail network",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(routes, key = { route -> route.id }) { route ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineDot(color = route.toColor())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = route.shortName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LineDot(color: Color) {
    Surface(
        modifier = Modifier.size(14.dp),
        shape = CircleShape,
        color = color,
        content = {}
    )
}

@Composable
private fun LoadStateOverlay(
    loadState: MapLoadState,
    modifier: Modifier = Modifier
) {
    when (loadState) {
        MapLoadState.Loading -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Preparing GTFS rail map",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Reading local routes, stops, and shapes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is MapLoadState.Error -> {
            Surface(
                modifier = modifier.padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Could not load GTFS rail data",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        is MapLoadState.Success -> {
            if (loadState.data.stations.isEmpty() || loadState.data.shapes.isEmpty()) {
                Surface(
                    modifier = modifier.padding(24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 8.dp
                ) {
                    Text(
                        text = "No rail routes were found in the GTFS assets",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun StationDetailSheet(
    station: TransitStation,
    routesById: Map<String, TransitRoute>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.66f)
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = station.stationCode,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = station.nameTh,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = station.displayNameEn(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            station.routes(routesById).forEach { route ->
                AssistChip(
                    onClick = {},
                    label = { Text(route.shortName) },
                    leadingIcon = { LineDot(color = route.toColor()) }
                )
            }
        }

        HorizontalDivider()

        ListItem(
            headlineContent = { Text("Lines") },
            supportingContent = { Text(station.lineSummary(routesById)) }
        )

        ListItem(
            headlineContent = { Text("Access area") },
            supportingContent = {
                Text(
                    text = station.addressEn.ifBlank { station.addressTh.ifBlank { "Address not listed in DB.txt" } },
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        ListItem(
            headlineContent = { Text("Coordinates") },
            supportingContent = {
                Text("%.5f, %.5f".format(station.latitude, station.longitude))
            }
        )
    }
}

private fun stationSearchResults(
    query: String,
    stations: List<TransitStation>,
    routesById: Map<String, TransitRoute>
): List<TransitStation> {
    val trimmedQuery = query.trim()
    val featuredCodes = setOf("CEN", "BL22", "E4", "A8", "A6", "N8")
    val source = if (trimmedQuery.isBlank()) {
        stations.filter { station -> station.stationCode in featuredCodes }.ifEmpty {
            stations.take(12)
        }
    } else {
        stations.filter { station ->
            station.nameEn.contains(trimmedQuery, ignoreCase = true) ||
                station.nameTh.contains(trimmedQuery, ignoreCase = true) ||
                station.stationCode.contains(trimmedQuery, ignoreCase = true) ||
                station.routes(routesById).any { route ->
                    route.shortName.contains(trimmedQuery, ignoreCase = true) ||
                        route.displayName.contains(trimmedQuery, ignoreCase = true)
                }
        }
    }

    return source.sortedWith(
        compareBy<TransitStation> { station -> station.stationCode.length }
            .thenBy { station -> station.stationCode }
            .thenBy { station -> station.nameEn }
    ).take(18)
}

private data class MapTransform(
    val originX: Float,
    val originY: Float,
    val scale: Float
) {
    companion object {
        fun forViewport(
            size: IntSize,
            zoom: Float,
            offsetX: Float,
            offsetY: Float
        ): MapTransform {
            val viewportWidth = size.width.toFloat().coerceAtLeast(1f)
            val viewportHeight = size.height.toFloat().coerceAtLeast(1f)
            val baseScale = min(viewportWidth / MAP_WIDTH, viewportHeight / MAP_HEIGHT) * 0.86f
            val originX = (viewportWidth - MAP_WIDTH * baseScale) / 2f + offsetX
            val originY = (viewportHeight - MAP_HEIGHT * baseScale) / 2f + offsetY
            return MapTransform(originX = originX, originY = originY, scale = baseScale * zoom)
        }
    }
}

private data class DrawableRoutePath(
    val path: Path,
    val color: Color,
    val width: Float
)

private fun buildDrawableRoutePaths(
    mapData: TransitMapData?,
    routesById: Map<String, TransitRoute>
): List<DrawableRoutePath> {
    return mapData?.shapes.orEmpty().mapNotNull { shape ->
        val route = routesById[shape.routeId]
        val points = shape.points.map { point -> Offset(point.x, point.y) }
            .simplify(minDistanceSquared = 18f)
        if (points.size < 2) return@mapNotNull null

        DrawableRoutePath(
            path = points.toSmoothPath(),
            color = route?.toColor() ?: Color(0xFF1964B7),
            width = route?.strokeWidth() ?: 10f
        )
    }
}

private fun List<Offset>.simplify(minDistanceSquared: Float): List<Offset> {
    if (size <= 2) return this

    val simplified = ArrayList<Offset>(size)
    var lastKept = first()
    simplified += lastKept

    for (index in 1 until lastIndex) {
        val point = this[index]
        val dx = point.x - lastKept.x
        val dy = point.y - lastKept.y
        if (dx * dx + dy * dy >= minDistanceSquared) {
            simplified += point
            lastKept = point
        }
    }

    simplified += last()
    return simplified
}

private fun List<Offset>.toSmoothPath(): Path {
    val points = this
    return Path().apply {
        moveTo(points.first().x, points.first().y)

        if (points.size == 2) {
            lineTo(points[1].x, points[1].y)
            return@apply
        }

        for (index in 0 until points.lastIndex) {
            val p0 = points.getOrElse(index - 1) { points[index] }
            val p1 = points[index]
            val p2 = points[index + 1]
            val p3 = points.getOrElse(index + 2) { p2 }
            val smoothness = 0.72f
            val control1 = Offset(
                x = p1.x + (p2.x - p0.x) * smoothness / 6f,
                y = p1.y + (p2.y - p0.y) * smoothness / 6f
            )
            val control2 = Offset(
                x = p2.x - (p3.x - p1.x) * smoothness / 6f,
                y = p2.y - (p3.y - p1.y) * smoothness / 6f
            )
            cubicTo(
                x1 = control1.x,
                y1 = control1.y,
                x2 = control2.x,
                y2 = control2.y,
                x3 = p2.x,
                y3 = p2.y
            )
        }
    }
}

private fun Offset.toMapOffset(
    viewportSize: IntSize,
    zoom: Float,
    offsetX: Float,
    offsetY: Float
): Offset {
    val transform = MapTransform.forViewport(viewportSize, zoom, offsetX, offsetY)
    return Offset(
        x = (x - transform.originX) / transform.scale,
        y = (y - transform.originY) / transform.scale
    )
}

private fun TransitStation.displayNameEn(): String {
    return nameEn
        .replace(Regex("^(BTS|MRT|ARL|SRT|BRT)\\s+", RegexOption.IGNORE_CASE), "")
        .ifBlank { nameEn }
}

private fun TransitStation.routes(routesById: Map<String, TransitRoute>): List<TransitRoute> {
    return routeIds.mapNotNull { routeId -> routesById[routeId] }
        .distinctBy { route -> route.id }
}

private fun TransitStation.lineSummary(routesById: Map<String, TransitRoute>): String {
    return routes(routesById).joinToString { route -> route.displayName }
        .ifBlank {
            lineKinds.sortedBy { lineKind -> lineKind.ordinal }
                .joinToString { lineKind -> lineKind.displayName }
        }
}

private fun TransitStation.primaryRouteColor(routesById: Map<String, TransitRoute>): Color {
    return routes(routesById).firstOrNull()?.toColor() ?: lineKind.fallbackColor()
}

private fun TransitRoute.toColor(): Color {
    return Color(colorArgb)
}

private fun TransitRoute.strokeWidth(): Float {
    return when (routeKind.operatorName.lowercase(Locale.US)) {
        "bts" -> 13f
        "mrt" -> 12f
        "srt" -> 10f
        else -> 10f
    }
}

private fun TransitLineKind.fallbackColor(): Color {
    return when (this) {
        TransitLineKind.BTS_SUKHUMVIT -> Color(0xFF65B724)
        TransitLineKind.BTS_SILOM -> Color(0xFF246B5B)
        TransitLineKind.BTS_GOLD -> Color(0xFFA3862A)
        TransitLineKind.MRT_BLUE -> Color(0xFF1964B7)
        TransitLineKind.MRT_PURPLE -> Color(0xFF660066)
        TransitLineKind.MRT_YELLOW -> Color(0xFFFFE547)
        TransitLineKind.MRT_PINK -> Color(0xFFCD4692)
        TransitLineKind.AIRPORT_RAIL_LINK -> Color(0xFFE32020)
        TransitLineKind.BRT -> Color(0xFF009900)
        TransitLineKind.SRT_DARK_RED -> Color(0xFFE10506)
        TransitLineKind.SRT_LIGHT_RED -> Color(0xFFFD5353)
    }
}

private fun TransitLineKind.isMrtLine(): Boolean {
    return operatorName.lowercase(Locale.US) == "mrt"
}
