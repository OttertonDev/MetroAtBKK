package com.ottertondev.metroatbkk.data

import androidx.compose.runtime.Immutable

@Immutable
data class TransitStation(
    val id: String,
    val stopId: String = id,
    val nameTh: String,
    val nameEn: String,
    val stationCode: String,
    val platforms: List<String>,
    val exits: List<String>,
    val mapX: Float,
    val mapY: Float,
    val latitude: Double,
    val longitude: Double,
    val lineKind: TransitLineKind,
    val lineKinds: Set<TransitLineKind> = setOf(lineKind),
    val routeIds: Set<String> = emptySet(),
    val addressTh: String,
    val addressEn: String
)

@Immutable
data class TransitRoute(
    val id: String,
    val shortName: String,
    val displayName: String,
    val operator: String,
    val routeKind: TransitLineKind,
    val colorArgb: Int
)

@Immutable
data class TransitShape(
    val routeId: String,
    val shapeId: String,
    val points: List<TransitMapPoint>
)

@Immutable
data class TransitMapPoint(
    val x: Float,
    val y: Float,
    val latitude: Double,
    val longitude: Double
)

@Immutable
data class TransitMapData(
    val routes: List<TransitRoute>,
    val shapes: List<TransitShape>,
    val stations: List<TransitStation>,
    val diagnostics: List<String> = emptyList()
)

enum class TransitLineKind(
    val displayName: String,
    val shortName: String,
    val operatorName: String,
    val fallbackPrefix: String
) {
    BTS_SUKHUMVIT("BTS Sukhumvit Line", "BTS Sukhumvit", "BTS", "E"),
    BTS_SILOM("BTS Silom Line", "BTS Silom", "BTS", "S"),
    BTS_GOLD("BTS Gold Line", "BTS Gold", "BTS", "G"),
    MRT_BLUE("MRT Blue Line", "MRT Blue", "MRT", "BL"),
    MRT_PURPLE("MRT Purple Line", "MRT Purple", "MRT", "PP"),
    MRT_YELLOW("MRT Yellow Line", "MRT Yellow", "MRT", "YL"),
    MRT_PINK("MRT Pink Line", "MRT Pink", "MRT", "PK"),
    AIRPORT_RAIL_LINK("Airport Rail Link", "ARL", "ARL", "A"),
    BRT("Bangkok BRT", "BRT", "BRT", "BRT"),
    SRT_DARK_RED("SRT Dark Red Line", "SRT Dark Red", "SRT", "RN"),
    SRT_LIGHT_RED("SRT Light Red Line", "SRT Light Red", "SRT", "RW")
}
