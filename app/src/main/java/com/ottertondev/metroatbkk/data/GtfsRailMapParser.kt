package com.ottertondev.metroatbkk.data

import android.content.res.AssetManager
import java.io.BufferedReader
import java.util.Locale
import kotlin.math.max

object GtfsRailMapParser {
    private const val MAP_SIZE = 1000f
    private const val MAP_PADDING = 64f

    val railRouteIds = setOf(
        "1", "2", "3", "4", "5", "179", "2025", "2026", "2027", "2224", "2436"
    )

    private val routeKindById = mapOf(
        "1" to TransitLineKind.BTS_SUKHUMVIT,
        "2" to TransitLineKind.BTS_SILOM,
        "3" to TransitLineKind.MRT_BLUE,
        "4" to TransitLineKind.MRT_PURPLE,
        "5" to TransitLineKind.AIRPORT_RAIL_LINK,
        "179" to TransitLineKind.BRT,
        "2025" to TransitLineKind.BTS_GOLD,
        "2026" to TransitLineKind.SRT_DARK_RED,
        "2027" to TransitLineKind.SRT_LIGHT_RED,
        "2224" to TransitLineKind.MRT_YELLOW,
        "2436" to TransitLineKind.MRT_PINK
    )

    private val stationCodeOverrides = mapOf(
        "bts siam" to "CEN",
        "mrt sukhumvit" to "BL22",
        "bts asok" to "E4",
        "bts mo chit" to "N8",
        "bts sala daeng" to "S2",
        "arl phaya thai" to "A8",
        "arl makkasan" to "A6"
    )

    fun load(assetManager: AssetManager): TransitMapData {
        val routes = assetManager.openText("routes.txt") { reader ->
            parseRoutes(reader)
        }
        val routeById = routes.associateBy { route -> route.id }

        val tripRouteIds = mutableMapOf<String, String>()
        val routeShapeIds = linkedMapOf<String, MutableSet<String>>()
        assetManager.openText("trips.txt") { reader ->
            parseTrips(reader, routeById.keys, tripRouteIds, routeShapeIds)
        }

        val stopRouteIds = linkedMapOf<String, MutableSet<String>>()
        assetManager.openText("stop_times.txt") { reader ->
            parseStopTimes(reader, tripRouteIds, stopRouteIds)
        }

        val enrichmentByName = assetManager.openText("DB.txt") { reader ->
            parseDbEnrichment(reader)
        }

        val rawStops = assetManager.openText("stops.txt") { reader ->
            parseStops(reader, stopRouteIds, routeById, enrichmentByName)
        }

        val shapeRouteIds = routeShapeIds
            .flatMap { (routeId, shapeIds) -> shapeIds.map { shapeId -> shapeId to routeId } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        val rawShapes = assetManager.openText("shapes.txt") { reader ->
            parseShapes(reader, shapeRouteIds)
        }

        val bounds = MapBounds.from(
            latitudes = rawStops.map { stop -> stop.latitude } + rawShapes.flatMap { shape -> shape.points.map { it.latitude } },
            longitudes = rawStops.map { stop -> stop.longitude } + rawShapes.flatMap { shape -> shape.points.map { it.longitude } }
        )

        val stations = rawStops.map { stop ->
            val normalizedPoint = bounds.normalize(latitude = stop.latitude, longitude = stop.longitude)
            TransitStation(
                id = stop.stopId,
                stopId = stop.stopId,
                nameTh = stop.nameTh,
                nameEn = stop.nameEn,
                stationCode = stationCodeFor(stop),
                platforms = emptyList(),
                exits = emptyList(),
                mapX = normalizedPoint.x,
                mapY = normalizedPoint.y,
                latitude = stop.latitude,
                longitude = stop.longitude,
                lineKind = stop.lineKinds.first(),
                lineKinds = stop.lineKinds,
                routeIds = stop.routeIds,
                addressTh = stop.addressTh,
                addressEn = stop.addressEn
            )
        }.sortedBy { station -> station.nameEn.lowercase(Locale.US) }

        val shapes = rawShapes.map { shape ->
            TransitShape(
                routeId = shape.routeId,
                shapeId = shape.shapeId,
                points = shape.points.map { point ->
                    val normalizedPoint = bounds.normalize(latitude = point.latitude, longitude = point.longitude)
                    TransitMapPoint(
                        x = normalizedPoint.x,
                        y = normalizedPoint.y,
                        latitude = point.latitude,
                        longitude = point.longitude
                    )
                }
            )
        }.filter { shape -> shape.points.size >= 2 }

        return TransitMapData(
            routes = routes,
            shapes = shapes,
            stations = stations,
            diagnostics = listOf(
                "${routes.size} rail routes",
                "${shapes.size} rail shapes",
                "${stations.size} rail stations"
            )
        )
    }

    fun parseRoutes(reader: BufferedReader): List<TransitRoute> {
        val header = GtfsCsvParser.headerIndexes(reader.readLine() ?: return emptyList())
        return reader.lineSequence().mapNotNull { line ->
            val fields = GtfsCsvParser.parseLine(line)
            val routeId = GtfsCsvParser.field(fields, header, "route_id")
            val kind = routeKindById[routeId] ?: return@mapNotNull null
            val shortName = GtfsCsvParser.field(fields, header, "route_short_name").ifBlank { kind.shortName }
            val longName = englishText(GtfsCsvParser.field(fields, header, "route_long_name")).ifBlank { kind.displayName }
            val operator = GtfsCsvParser.field(fields, header, "agency_id").ifBlank { kind.operatorName }
            val routeColor = GtfsCsvParser.field(fields, header, "route_color")

            TransitRoute(
                id = routeId,
                shortName = shortName,
                displayName = longName,
                operator = operator,
                routeKind = kind,
                colorArgb = parseRouteColor(routeColor, fallback = kind.fallbackColorArgb())
            )
        }.toList()
    }

    fun parseTrips(
        reader: BufferedReader,
        railRouteIds: Set<String>,
        tripRouteIds: MutableMap<String, String>,
        routeShapeIds: MutableMap<String, MutableSet<String>>
    ) {
        val header = GtfsCsvParser.headerIndexes(reader.readLine() ?: return)
        reader.lineSequence().forEach { line ->
            val fields = GtfsCsvParser.parseLine(line)
            val routeId = GtfsCsvParser.field(fields, header, "route_id")
            if (routeId !in railRouteIds) return@forEach

            val tripId = GtfsCsvParser.field(fields, header, "trip_id")
            val shapeId = GtfsCsvParser.field(fields, header, "shape_id")
            if (tripId.isNotBlank()) tripRouteIds[tripId] = routeId
            if (shapeId.isNotBlank()) routeShapeIds.getOrPut(routeId) { linkedSetOf() } += shapeId
        }
    }

    fun parseStopTimes(
        reader: BufferedReader,
        tripRouteIds: Map<String, String>,
        stopRouteIds: MutableMap<String, MutableSet<String>>
    ) {
        val header = GtfsCsvParser.headerIndexes(reader.readLine() ?: return)
        reader.lineSequence().forEach { line ->
            val fields = GtfsCsvParser.parseLine(line)
            val tripId = GtfsCsvParser.field(fields, header, "trip_id")
            val routeId = tripRouteIds[tripId] ?: return@forEach
            val stopId = GtfsCsvParser.field(fields, header, "stop_id")
            if (stopId.isNotBlank()) stopRouteIds.getOrPut(stopId) { linkedSetOf() } += routeId
        }
    }

    fun parseStops(
        reader: BufferedReader,
        stopRouteIds: Map<String, Set<String>>,
        routeById: Map<String, TransitRoute>,
        enrichmentByName: Map<String, DbStationEnrichment>
    ): List<RawGtfsStop> {
        val header = GtfsCsvParser.headerIndexes(reader.readLine() ?: return emptyList())
        return reader.lineSequence().mapNotNull { line ->
            val fields = GtfsCsvParser.parseLine(line)
            val stopId = GtfsCsvParser.field(fields, header, "stop_id")
            val routeIds = stopRouteIds[stopId].orEmpty()
            if (routeIds.isEmpty()) return@mapNotNull null

            val latitude = GtfsCsvParser.field(fields, header, "stop_lat").toDoubleOrNull() ?: return@mapNotNull null
            val longitude = GtfsCsvParser.field(fields, header, "stop_lon").toDoubleOrNull() ?: return@mapNotNull null
            val stopName = GtfsCsvParser.field(fields, header, "stop_name")
            val bilingualName = bilingualText(stopName)
            val lineKinds = routeIds.mapNotNull { routeId -> routeById[routeId]?.routeKind }
                .distinct()
                .toCollection(linkedSetOf())
            if (lineKinds.isEmpty()) return@mapNotNull null

            val enrichment = enrichmentByName[canonicalName(bilingualName.en)]
            RawGtfsStop(
                stopId = stopId,
                nameTh = bilingualName.th.ifBlank { bilingualName.en },
                nameEn = bilingualName.en.ifBlank { bilingualName.th },
                latitude = latitude,
                longitude = longitude,
                routeIds = routeIds.toCollection(linkedSetOf()),
                lineKinds = lineKinds,
                addressTh = enrichment?.addressTh.orEmpty(),
                addressEn = enrichment?.addressEn.orEmpty()
            )
        }.toList()
    }

    fun parseShapes(
        reader: BufferedReader,
        shapeRouteIds: Map<String, List<String>>
    ): List<RawGtfsShape> {
        val wantedShapeIds = shapeRouteIds.keys
        val header = GtfsCsvParser.headerIndexes(reader.readLine() ?: return emptyList())
        val pointsByShapeId = linkedMapOf<String, MutableList<RawShapePoint>>()

        reader.lineSequence().forEach { line ->
            val fields = GtfsCsvParser.parseLine(line)
            val shapeId = GtfsCsvParser.field(fields, header, "shape_id")
            if (shapeId !in wantedShapeIds) return@forEach

            val latitude = GtfsCsvParser.field(fields, header, "shape_pt_lat").toDoubleOrNull() ?: return@forEach
            val longitude = GtfsCsvParser.field(fields, header, "shape_pt_lon").toDoubleOrNull() ?: return@forEach
            val sequence = GtfsCsvParser.field(fields, header, "shape_pt_sequence").toIntOrNull() ?: return@forEach
            pointsByShapeId.getOrPut(shapeId) { mutableListOf() } += RawShapePoint(latitude, longitude, sequence)
        }

        return pointsByShapeId.flatMap { (shapeId, points) ->
            val sortedPoints = points.sortedBy { point -> point.sequence }
            shapeRouteIds[shapeId].orEmpty().map { routeId ->
                RawGtfsShape(
                    routeId = routeId,
                    shapeId = shapeId,
                    points = sortedPoints
                )
            }
        }
    }

    private fun parseDbEnrichment(reader: BufferedReader): Map<String, DbStationEnrichment> {
        return reader.lineSequence().mapNotNull { row ->
            val fields = TransitStationParser.parseQuotedFields(row)
            if (fields.size < 8) return@mapNotNull null
            val nameEn = fields[2]
            if (nameEn.isBlank()) return@mapNotNull null
            canonicalName(nameEn) to DbStationEnrichment(
                addressTh = fields.getOrNull(6).orEmpty(),
                addressEn = fields.getOrNull(7).orEmpty()
            )
        }.toMap()
    }

    private fun stationCodeFor(stop: RawGtfsStop): String {
        stationCodeOverrides[stop.nameEn.trim().lowercase(Locale.US)]?.let { return it }
        stationCodeOverrides[canonicalName(stop.nameEn)]?.let { return it }
        val prefix = stop.lineKinds.first().fallbackPrefix
        return "$prefix-${stop.stopId.padStart(3, '0')}"
    }

    private fun bilingualText(value: String): BilingualText {
        val parts = value.split(";", limit = 2)
        return if (parts.size == 2) {
            BilingualText(th = parts[0].trim(), en = parts[1].trim())
        } else {
            BilingualText(th = value.trim(), en = value.trim())
        }
    }

    private fun englishText(value: String): String {
        return bilingualText(value).en
    }

    fun canonicalName(value: String): String {
        return value.trim()
            .replace(Regex("^(BTS|MRT|ARL|SRT|BRT)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*[\\[(].*$"), "")
            .lowercase(Locale.US)
            .replace("khrung thon buri", "krung thon buri")
            .replace("bang bau", "bang bua")
            .replace("bangwa", "bang wa")
            .replace("bangphai", "bang phai")
            .replace("lad phrao", "lat phrao")
            .replace("yeak", "yaek")
            .replace("pakkret", "pak kret")
            .replace("kak si", "lak si")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }

    private fun parseRouteColor(hex: String, fallback: Int): Int {
        val normalized = hex.trim().removePrefix("#")
        if (normalized.length != 6) return fallback
        return normalized.toIntOrNull(16)?.let { rgb -> 0xFF000000.toInt() or rgb } ?: fallback
    }

    private fun TransitLineKind.fallbackColorArgb(): Int {
        return when (this) {
            TransitLineKind.BTS_SUKHUMVIT -> 0xFF65B724.toInt()
            TransitLineKind.BTS_SILOM -> 0xFF246B5B.toInt()
            TransitLineKind.BTS_GOLD -> 0xFFA3862A.toInt()
            TransitLineKind.MRT_BLUE -> 0xFF1964B7.toInt()
            TransitLineKind.MRT_PURPLE -> 0xFF660066.toInt()
            TransitLineKind.MRT_YELLOW -> 0xFFFFE547.toInt()
            TransitLineKind.MRT_PINK -> 0xFFCD4692.toInt()
            TransitLineKind.AIRPORT_RAIL_LINK -> 0xFFE32020.toInt()
            TransitLineKind.BRT -> 0xFF009900.toInt()
            TransitLineKind.SRT_DARK_RED -> 0xFFE10506.toInt()
            TransitLineKind.SRT_LIGHT_RED -> 0xFFFD5353.toInt()
        }
    }

    private fun <T> AssetManager.openText(fileName: String, block: (BufferedReader) -> T): T {
        return open(fileName).bufferedReader(Charsets.UTF_8).use(block)
    }

    data class RawGtfsStop(
        val stopId: String,
        val nameTh: String,
        val nameEn: String,
        val latitude: Double,
        val longitude: Double,
        val routeIds: Set<String>,
        val lineKinds: Set<TransitLineKind>,
        val addressTh: String,
        val addressEn: String
    )

    data class RawGtfsShape(
        val routeId: String,
        val shapeId: String,
        val points: List<RawShapePoint>
    )

    data class RawShapePoint(
        val latitude: Double,
        val longitude: Double,
        val sequence: Int
    )

    data class DbStationEnrichment(
        val addressTh: String,
        val addressEn: String
    )

    private data class BilingualText(
        val th: String,
        val en: String
    )

    private data class MapBounds(
        val minLatitude: Double,
        val maxLatitude: Double,
        val minLongitude: Double,
        val maxLongitude: Double
    ) {
        fun normalize(latitude: Double, longitude: Double): TransitMapPoint {
            val latitudeRange = max(maxLatitude - minLatitude, 0.000001)
            val longitudeRange = max(maxLongitude - minLongitude, 0.000001)
            val normalizedX = ((longitude - minLongitude) / longitudeRange).toFloat()
            val normalizedY = ((latitude - minLatitude) / latitudeRange).toFloat()
            return TransitMapPoint(
                x = MAP_PADDING + normalizedX * (MAP_SIZE - MAP_PADDING * 2f),
                y = MAP_PADDING + (1f - normalizedY) * (MAP_SIZE - MAP_PADDING * 2f),
                latitude = latitude,
                longitude = longitude
            )
        }

        companion object {
            fun from(latitudes: List<Double>, longitudes: List<Double>): MapBounds {
                return MapBounds(
                    minLatitude = latitudes.minOrNull() ?: 13.5,
                    maxLatitude = latitudes.maxOrNull() ?: 14.0,
                    minLongitude = longitudes.minOrNull() ?: 100.3,
                    maxLongitude = longitudes.maxOrNull() ?: 100.8
                )
            }
        }
    }
}
