package com.ottertondev.metroatbkk

import com.ottertondev.metroatbkk.data.GtfsCsvParser
import com.ottertondev.metroatbkk.data.GtfsRailMapParser
import com.ottertondev.metroatbkk.data.TransitLineKind
import com.ottertondev.metroatbkk.data.TransitRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GtfsRailMapParserTest {
    @Test
    fun csvParser_handlesQuotedCommasAndEscapedQuotes() {
        val fields = GtfsCsvParser.parseLine("\"1\",\"BTSC\",\"Sukhumvit\",\"Line, with comma\",\"escaped \"\"quote\"\"\"")

        assertEquals("1", fields[0])
        assertEquals("Line, with comma", fields[3])
        assertEquals("escaped \"quote\"", fields[4])
    }

    @Test
    fun parseRoutes_keepsOnlyConfiguredRailRoutes() {
        val routes = GtfsRailMapParser.parseRoutes(
            """
            route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_color
            "1","BTSC","Sukhumvit","สายสุขุมวิท;Sukhumvit (Light green Line)","BTS","0","65b724"
            "2224","MRTA","Yellow","สายสีเหลือง;Yellow Line","MRT","1","FFE547"
            "9999","BUS","Bus","Bus route","Bus","3","ff0000"
            """.trimIndent().byteInputStream().bufferedReader()
        )

        assertEquals(listOf("1", "2224"), routes.map { route -> route.id })
        assertEquals(TransitLineKind.BTS_SUKHUMVIT, routes[0].routeKind)
        assertEquals(TransitLineKind.MRT_YELLOW, routes[1].routeKind)
    }

    @Test
    fun parseTrips_collectsTripRoutesAndShapeIds() {
        val tripRouteIds = mutableMapOf<String, String>()
        val routeShapeIds = mutableMapOf<String, MutableSet<String>>()

        GtfsRailMapParser.parseTrips(
            reader = """
            route_id,service_id,trip_id,trip_headsign,direction_id,shape_id,wheelchair_accessible
            "1","1","trip-a","Khu Khot","0","shape-a","1"
            "1","1","trip-b","Kheha","1","shape-b","1"
            "9999","1","trip-bus","Nowhere","0","shape-bus","1"
            """.trimIndent().byteInputStream().bufferedReader(),
            railRouteIds = setOf("1"),
            tripRouteIds = tripRouteIds,
            routeShapeIds = routeShapeIds
        )

        assertEquals("1", tripRouteIds["trip-a"])
        assertEquals(setOf("shape-a", "shape-b"), routeShapeIds["1"])
        assertTrue("trip-bus" !in tripRouteIds)
    }

    @Test
    fun parseStopTimes_linksStopsToRailRoutes() {
        val stopRouteIds = mutableMapOf<String, MutableSet<String>>()

        GtfsRailMapParser.parseStopTimes(
            reader = """
            trip_id,arrival_time,departure_time,stop_id,stop_sequence,timepoint
            "trip-a","00:00:00","00:00:00","12","1","0"
            "trip-a","00:01:00","00:01:30","13","2","0"
            "trip-bus","00:01:00","00:01:30","999","1","0"
            """.trimIndent().byteInputStream().bufferedReader(),
            tripRouteIds = mapOf("trip-a" to "1"),
            stopRouteIds = stopRouteIds
        )

        assertEquals(setOf("1"), stopRouteIds["12"])
        assertEquals(setOf("1"), stopRouteIds["13"])
        assertTrue("999" !in stopRouteIds)
    }

    @Test
    fun parseStops_buildsRouteMembershipFromGtfsStops() {
        val route = TransitRoute(
            id = "1",
            shortName = "Sukhumvit",
            displayName = "Sukhumvit (Light green Line)",
            operator = "BTSC",
            routeKind = TransitLineKind.BTS_SUKHUMVIT,
            colorArgb = 0xFF65B724.toInt()
        )

        val stops = GtfsRailMapParser.parseStops(
            reader = """
            stop_id,stop_name,stop_lat,stop_lon,zone_id,wheelchair_boarding
            "12","BTS สยาม;BTS Siam","13.7456","100.5342","12","1"
            "999","Bus Stop;Bus Stop","13.7","100.5","999","1"
            """.trimIndent().byteInputStream().bufferedReader(),
            stopRouteIds = mapOf("12" to setOf("1")),
            routeById = mapOf("1" to route),
            enrichmentByName = emptyMap()
        )

        assertEquals(1, stops.size)
        assertEquals("12", stops.first().stopId)
        assertEquals(setOf("1"), stops.first().routeIds)
        assertEquals(setOf(TransitLineKind.BTS_SUKHUMVIT), stops.first().lineKinds)
    }

    @Test
    fun parseShapes_keepsRequestedShapesAndSortsPointOrder() {
        val shapes = GtfsRailMapParser.parseShapes(
            reader = """
            shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence
            "shape-a","13.2","100.2","2"
            "shape-b","13.9","100.9","1"
            "shape-a","13.1","100.1","1"
            """.trimIndent().byteInputStream().bufferedReader(),
            shapeRouteIds = mapOf("shape-a" to listOf("1"))
        )

        assertEquals(1, shapes.size)
        assertEquals("shape-a", shapes.first().shapeId)
        assertEquals(listOf(1, 2), shapes.first().points.map { point -> point.sequence })
    }
}
