package com.ottertondev.metroatbkk.data

import java.util.Locale
import kotlin.math.max

object TransitStationParser {
    private const val MAP_WIDTH = 1000f
    private const val MAP_HEIGHT = 1000f
    private const val MAP_PADDING = 80f

    private val quotedFieldRegex = Regex("'((?:[^']|'')*)'")

    private val stationCodeOverrides = mapOf(
        "BTS SIAM" to "CEN",
        "MRT SUKHUMVIT" to "BL22",
        "BTS ASOK" to "E4",
        "BTS MO CHIT" to "N8",
        "BTS SALA DAENG" to "S2",
        "ARL PHAYA THAI" to "A8",
        "ARL MAKKASAN" to "A6"
    )

    private val englishNameOverridesById = mapOf(
        "N00014636" to "MRT Wat Phra Sri Mahathat"
    )

    fun parseStations(lines: Iterable<String>): List<TransitStation> {
        return parseStations(lines.asSequence())
    }

    fun parseStations(lines: Sequence<String>): List<TransitStation> {
        val rawStations = lines.mapIndexedNotNull { index, row ->
            parseRawStation(index, parseQuotedFields(row))
        }.toList()

        return normalizeCoordinates(rawStations)
    }

    fun parseQuotedFields(row: String): List<String> {
        return quotedFieldRegex.findAll(row)
            .map { match -> match.groupValues[1].replace("''", "'").trim() }
            .toList()
    }

    private fun parseRawStation(index: Int, fields: List<String>): RawTransitStation? {
        if (fields.size < 14) return null

        val latitude = fields[3].toDoubleOrNull() ?: return null
        val longitude = fields[4].toDoubleOrNull() ?: return null
        if (!BangkokMetroBounds.contains(latitude, longitude)) return null

        val id = fields[0].ifBlank { "station-$index" }
        val nameTh = fields[1].ifBlank { fields[2] }
        val nameEn = cleanEnglishStationName(id = id, nameEn = fields[2], addressEn = fields.getOrNull(7).orEmpty())
        if (nameTh.isBlank() && nameEn.isBlank()) return null
        val lineKinds = TransitLineCatalog.lineKindsFor(nameEn = nameEn, assetName = fields[13])
        if (lineKinds.isEmpty()) return null

        return RawTransitStation(
            id = id,
            nameTh = nameTh,
            nameEn = nameEn,
            stationCode = stationCodeFor(id = id, nameEn = nameEn, lineKind = lineKinds.first()),
            latitude = latitude,
            longitude = longitude,
            lineKind = lineKinds.first(),
            lineKinds = lineKinds.toSet(),
            addressTh = fields.getOrNull(6).orEmpty(),
            addressEn = fields.getOrNull(7).orEmpty()
        )
    }

    private fun cleanEnglishStationName(id: String, nameEn: String, addressEn: String): String {
        englishNameOverridesById[id]?.let { return it }
        if (nameEn.isNotBlank() && nameEn != "MRT") return nameEn
        return addressEn.substringBefore("Phahon Yothin Rd.").trim().ifBlank { nameEn }
    }

    private fun stationCodeFor(id: String, nameEn: String, lineKind: TransitLineKind): String {
        val normalizedName = nameEn.trim().uppercase(Locale.US)
        return stationCodeOverrides[normalizedName] ?: buildFallbackStationCode(id, lineKind)
    }

    private fun buildFallbackStationCode(id: String, lineKind: TransitLineKind): String {
        val numericId = id.filter { it.isDigit() }.takeLast(4).padStart(4, '0')
        return "${lineKind.fallbackPrefix}-$numericId"
    }

    private fun normalizeCoordinates(rawStations: List<RawTransitStation>): List<TransitStation> {
        if (rawStations.isEmpty()) return emptyList()

        val minLatitude = rawStations.minOf { it.latitude }
        val maxLatitude = rawStations.maxOf { it.latitude }
        val minLongitude = rawStations.minOf { it.longitude }
        val maxLongitude = rawStations.maxOf { it.longitude }
        val latitudeRange = max(maxLatitude - minLatitude, 0.000001)
        val longitudeRange = max(maxLongitude - minLongitude, 0.000001)

        return rawStations.map { station ->
            val normalizedX = ((station.longitude - minLongitude) / longitudeRange).toFloat()
            val normalizedY = ((station.latitude - minLatitude) / latitudeRange).toFloat()
            TransitStation(
                id = station.id,
                nameTh = station.nameTh,
                nameEn = station.nameEn,
                stationCode = station.stationCode,
                platforms = emptyList(),
                exits = emptyList(),
                mapX = MAP_PADDING + normalizedX * (MAP_WIDTH - MAP_PADDING * 2f),
                mapY = MAP_PADDING + (1f - normalizedY) * (MAP_HEIGHT - MAP_PADDING * 2f),
                latitude = station.latitude,
                longitude = station.longitude,
                lineKind = station.lineKind,
                lineKinds = station.lineKinds,
                addressTh = station.addressTh,
                addressEn = station.addressEn
            )
        }
    }

    private data class RawTransitStation(
        val id: String,
        val nameTh: String,
        val nameEn: String,
        val stationCode: String,
        val latitude: Double,
        val longitude: Double,
        val lineKind: TransitLineKind,
        val lineKinds: Set<TransitLineKind>,
        val addressTh: String,
        val addressEn: String
    )

    private object BangkokMetroBounds {
        private const val MIN_LATITUDE = 13.45
        private const val MAX_LATITUDE = 14.15
        private const val MIN_LONGITUDE = 100.25
        private const val MAX_LONGITUDE = 101.00

        fun contains(latitude: Double, longitude: Double): Boolean {
            return latitude in MIN_LATITUDE..MAX_LATITUDE &&
                longitude in MIN_LONGITUDE..MAX_LONGITUDE
        }
    }
}
