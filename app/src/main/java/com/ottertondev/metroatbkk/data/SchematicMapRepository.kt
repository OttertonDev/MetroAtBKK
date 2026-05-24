package com.ottertondev.metroatbkk.data

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class SchematicMapData(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val bounds: SchematicBounds,
    val stations: List<SchematicStation>
)

data class SchematicBounds(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
)

data class SchematicStation(
    val lineId: Int,
    val stationId: Int,
    val key: String,
    val nameEn: String,
    val nameTh: String,
    val sourceX: Int,
    val sourceY: Int
)

class SchematicMapRepository(
    private val assetManager: AssetManager
) {
    suspend fun load(): SchematicMapData = withContext(Dispatchers.IO) {
        assetManager.open(ASSET_FILE_NAME).bufferedReader(Charsets.UTF_8).use { reader ->
            parse(reader.readText())
        }
    }

    private fun parse(rawJson: String): SchematicMapData {
        val root = JSONObject(rawJson)
        val canvas = root.getJSONArray("canvas")
        val bounds = root.getJSONArray("bounds")
        val stations = root.getJSONArray("stations")

        return SchematicMapData(
            canvasWidth = canvas.getInt(0),
            canvasHeight = canvas.getInt(1),
            bounds = SchematicBounds(
                minX = bounds.getInt(0),
                minY = bounds.getInt(1),
                maxX = bounds.getInt(2),
                maxY = bounds.getInt(3)
            ),
            stations = List(stations.length()) { index ->
                val station = stations.getJSONArray(index)
                SchematicStation(
                    lineId = station.getInt(0),
                    stationId = station.getInt(1),
                    key = station.getString(2),
                    nameEn = station.getString(3),
                    nameTh = station.getString(4),
                    sourceX = station.getInt(5),
                    sourceY = station.getInt(6)
                )
            }
        )
    }

    private companion object {
        const val ASSET_FILE_NAME = "schematic_stations.json"
    }
}
