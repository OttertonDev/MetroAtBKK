package com.ottertondev.metroatbkk

import com.ottertondev.metroatbkk.data.TransitLineKind
import com.ottertondev.metroatbkk.data.TransitStationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransitStationParserTest {
    @Test
    fun parseQuotedFields_handlesSingleQuotedCsvRows() {
        val fields = TransitStationParser.parseQuotedFields(
            "'N00000012', 'BTS Siam', 'BTS Siam', '13.7456', '100.5342', '', 'Thai address', 'Rama I Rd.', '30', 'STATUS=A', '18', '', '', 'bts.png'"
        )

        assertEquals("N00000012", fields[0])
        assertEquals("BTS Siam", fields[2])
        assertEquals("bts.png", fields[13])
    }

    @Test
    fun parseStations_mapsRailRowsAndOfficialCodeOverrides() {
        val stations = TransitStationParser.parseStations(
            listOf(
                "'N00000012', 'BTS Siam', 'BTS Siam', '13.7456', '100.5342', '', 'Thai address', 'Rama I Rd.', '30', 'STATUS=A', '18', '', '', 'bts.png'",
                "'N00000333', 'MRT Khlong Toei', 'MRT Khlong Toei', '13.7222', '100.5539', '', 'Thai address', 'Rama IV Rd.', '30', 'STATUS=A', '18', '', '', 'mrt.png'"
            )
        )

        assertEquals(2, stations.size)
        assertEquals("CEN", stations.first().stationCode)
        assertEquals(TransitLineKind.BTS_SUKHUMVIT, stations.first().lineKind)
        assertEquals("BL-0333", stations[1].stationCode)
        assertEquals(TransitLineKind.MRT_BLUE, stations[1].lineKind)
    }

    @Test
    fun parseStations_splitsMrtIntoRealColorLines() {
        val stations = TransitStationParser.parseStations(
            listOf(
                "'N00000348', 'MRT Khlong Bang Phai', 'MRT Khlong Bang Phai', '13.8924', '100.4082', '', 'Thai address', 'Purple', '30', 'STATUS=A', '18', '', '', 'mrt.png'",
                "'N00014125', 'MRT Bang Kapi', 'MRT Bang Kapi', '13.7690', '100.6397', '', 'Thai address', 'Yellow', '30', 'STATUS=A', '18', '', '', 'mrt.png'",
                "'N00014650', 'MRT Min Buri', 'MRT Min Buri', '13.8084', '100.7325', '', 'Thai address', 'Pink', '30', 'STATUS=A', '18', '', '', 'mrt.png'"
            )
        )

        assertEquals(TransitLineKind.MRT_PURPLE, stations[0].lineKind)
        assertEquals(TransitLineKind.MRT_YELLOW, stations[1].lineKind)
        assertEquals(TransitLineKind.MRT_PINK, stations[2].lineKind)
    }

    @Test
    fun parseStations_splitsBtsSukhumvitAndSilomLines() {
        val stations = TransitStationParser.parseStations(
            listOf(
                "'N00000025', 'BTS Asok', 'BTS Asok', '13.7370', '100.5603', '', 'Thai address', 'Sukhumvit', '30', 'STATUS=A', '18', '', '', 'bts.png'",
                "'N00000010', 'BTS Sala Daeng', 'BTS Sala Daeng', '13.7285', '100.5343', '', 'Thai address', 'Silom', '30', 'STATUS=A', '18', '', '', 'bts.png'",
                "'N00000012', 'BTS Siam', 'BTS Siam', '13.7456', '100.5342', '', 'Thai address', 'Interchange', '30', 'STATUS=A', '18', '', '', 'bts.png'"
            )
        )

        assertEquals(TransitLineKind.BTS_SUKHUMVIT, stations[0].lineKind)
        assertEquals(TransitLineKind.BTS_SILOM, stations[1].lineKind)
        assertTrue(TransitLineKind.BTS_SUKHUMVIT in stations[2].lineKinds)
        assertTrue(TransitLineKind.BTS_SILOM in stations[2].lineKinds)
    }

    @Test
    fun parseStations_filtersBusRowsAndOutOfBoundsRailRows() {
        val stations = TransitStationParser.parseStations(
            listOf(
                "'N00000676', 'Bus Stop', 'The Parliament', '13.7734', '100.5145', '', 'Thai address', 'Uthong Nai Rd.', '30', 'STATUS=A', '18', '', '', 'bus-stop.png'",
                "'N00017884', 'Rail Far Away', 'Rail Far Away', '9.2142', '99.8606', '', 'Thai address', 'Far Away', '30', 'STATUS=A', '18', '', '', 'train.png'"
            )
        )

        assertTrue(stations.isEmpty())
    }

    @Test
    fun parseStations_flipsLatitudeIntoCanvasYCoordinates() {
        val stations = TransitStationParser.parseStations(
            listOf(
                "'N00000001', 'BTS South', 'BTS South', '13.6000', '100.5000', '', 'Thai address', 'South', '30', 'STATUS=A', '18', '', '', 'bts.png'",
                "'N00000002', 'BTS North', 'BTS North', '13.9000', '100.5000', '', 'Thai address', 'North', '30', 'STATUS=A', '18', '', '', 'bts.png'"
            )
        )

        val south = stations.first { it.nameEn == "BTS South" }
        val north = stations.first { it.nameEn == "BTS North" }
        assertTrue(north.mapY < south.mapY)
        assertTrue(stations.all { station -> station.mapX in 0f..1000f && station.mapY in 0f..1000f })
    }

    @Test
    fun parseStations_skipsMalformedRows() {
        val stations = TransitStationParser.parseStations(
            listOf("'broken', 'row'")
        )

        assertTrue(stations.isEmpty())
    }
}
