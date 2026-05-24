package com.ottertondev.metroatbkk.data

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransitStationRepository(
    private val assetManager: AssetManager
) {
    suspend fun loadMapData(): TransitMapData = withContext(Dispatchers.IO) {
        GtfsRailMapParser.load(assetManager)
    }

    suspend fun loadStations(): List<TransitStation> = withContext(Dispatchers.IO) {
        assetManager.open(DB_FILE_NAME).bufferedReader(Charsets.UTF_8).useLines { lines ->
            TransitStationParser.parseStations(lines)
        }
    }

    private companion object {
        const val DB_FILE_NAME = "DB.txt"
    }
}
