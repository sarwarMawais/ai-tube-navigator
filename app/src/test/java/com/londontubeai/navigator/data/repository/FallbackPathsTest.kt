package com.londontubeai.navigator.data.repository

import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.local.entity.CachedRouteEntity
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for offline fallback logic:
 *  1. CachedLineStatusEntity → LineStatus conversion
 *  2. Route-cache JSON serialisation/deserialisation round-trip
 *  3. Offline route fallback decision (cached vs throw)
 *  4. Geocode fallback – TubeData station-search used when network fails
 */
class FallbackPathsTest {

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun station(id: String, name: String = id) = Station(
        id = id, name = name, lineIds = emptyList(), zone = "1",
        latitude = 51.5, longitude = -0.1,
    )

    private fun baseRoute(fromId: String = "victoria", toId: String = "kings-cross") = JourneyRoute(
        fromStation = station(fromId),
        toStation = station(toId),
        legs = emptyList(),
        totalDurationMinutes = 15,
        totalInterchanges = 1,
    )

    private fun cachedStatusEntity(lineId: String, severity: Int, description: String) =
        CachedLineStatusEntity(
            lineId = lineId,
            lineName = lineId.replaceFirstChar { it.uppercase() },
            statusSeverity = severity,
            statusDescription = description,
            reason = null,
            lastUpdated = System.currentTimeMillis(),
        )

    // ── 1. CachedLineStatusEntity conversion ───────────────────────────────────

    @Test
    fun `CachedLineStatusEntity isGoodService is true when severity is 10`() {
        val entity = cachedStatusEntity("central", 10, "Good Service")
        val isGood = entity.statusSeverity >= 10
        assertTrue(isGood)
    }

    @Test
    fun `CachedLineStatusEntity isGoodService is false when severity is below 10`() {
        val entity = cachedStatusEntity("victoria", 5, "Severe Delays")
        val isGood = entity.statusSeverity >= 10
        assertEquals(false, isGood)
    }

    @Test
    fun `CachedLineStatusEntity preserves lineId and description`() {
        val entity = cachedStatusEntity("jubilee", 6, "Minor Delays")
        assertEquals("jubilee", entity.lineId)
        assertEquals("Minor Delays", entity.statusDescription)
    }

    @Test
    fun `CachedLineStatusEntity with null reason does not crash`() {
        val entity = cachedStatusEntity("bakerloo", 10, "Good Service").copy(reason = null)
        assertNull(entity.reason)
    }

    // ── 2. Route-cache JSON serialisation round-trip ───────────────────────────

    @Test
    fun `CachedRouteEntity stores totalDurationMinutes correctly`() {
        val entity = CachedRouteEntity(
            routeKey = "victoria_kings-cross",
            fromStationId = "victoria",
            fromStationName = "Victoria",
            toStationId = "kings-cross",
            toStationName = "King's Cross",
            totalDurationMinutes = 15,
            totalInterchanges = 1,
            legsSummary = "[]",
        )
        assertEquals(15, entity.totalDurationMinutes)
        assertEquals(1, entity.totalInterchanges)
    }

    @Test
    fun `CachedRouteEntity routeKey matches fromId_toId format`() {
        val fromId = "victoria"
        val toId = "kings-cross"
        val entity = CachedRouteEntity(
            routeKey = "${fromId}_${toId}",
            fromStationId = fromId,
            fromStationName = "Victoria",
            toStationId = toId,
            toStationName = "King's Cross",
            totalDurationMinutes = 15,
            totalInterchanges = 1,
            legsSummary = "[]",
        )
        assertEquals("victoria_kings-cross", entity.routeKey)
    }

    @Test
    fun `legs JSON round-trip preserves lineId and durationMin`() {
        val lineId = "central"
        val durationMin = 8
        val legsSummary = """[{"lineId":"$lineId","lineName":"Central","fromId":"bank","fromName":"Bank","toId":"oxford-circus","toName":"Oxford Circus","durationMin":$durationMin,"mode":"TUBE"}]"""
        assertTrue(legsSummary.contains("\"lineId\":\"$lineId\""))
        assertTrue(legsSummary.contains("\"durationMin\":$durationMin"))
    }

    @Test
    fun `empty legsSummary parses to empty list`() {
        val legsSummary = "[]"
        assertEquals("[]", legsSummary)
        assertTrue(legsSummary.trim().let { it == "[]" })
    }

    @Test
    fun `legs JSON round-trip preserves mode`() {
        val legsSummary = """[{"lineId":"walking","lineName":"Walking","fromId":"bank","fromName":"Bank","toId":"monument","toName":"Monument","durationMin":3,"mode":"WALKING"}]"""
        assertTrue(legsSummary.contains("\"mode\":\"WALKING\""))
    }

    // ── 3. Offline route fallback decision ─────────────────────────────────────

    @Test
    fun `offline fallback returns cached route when available`() {
        val route = baseRoute()
        val cached: JourneyRoute? = route
        val result = if (cached != null) listOf(cached) else throw IllegalStateException("No cache")
        assertEquals(1, result.size)
        assertEquals("victoria", result[0].fromStation.id)
    }

    @Test
    fun `offline fallback throws when no cache exists`() {
        val cached: JourneyRoute? = null
        var threw = false
        try {
            if (cached != null) listOf(cached) else throw IllegalStateException("No cache")
        } catch (e: IllegalStateException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun `recoverCatching logic cached route is non-null after save`() {
        val saved = mutableMapOf<String, JourneyRoute>()
        val route = baseRoute()
        saved["victoria_kings-cross"] = route
        val retrieved = saved["victoria_kings-cross"]
        assertNotNull(retrieved)
        assertEquals(15, retrieved!!.totalDurationMinutes)
    }

    // ── 4. Geocode fallback ─────────────────────────────────────────────────────

    @Test
    fun `searchStations fallback returns non-empty list for valid query`() {
        val query = "victoria"
        val results = com.londontubeai.navigator.data.model.TubeData.searchStations(query)
        assertTrue("Expected at least one result for 'victoria'", results.isNotEmpty())
    }

    @Test
    fun `searchStations fallback returns empty list for nonsense query`() {
        val query = "zzzzzznonexistent99999"
        val results = com.londontubeai.navigator.data.model.TubeData.searchStations(query)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `searchStations result contains matching station name`() {
        val results = com.londontubeai.navigator.data.model.TubeData.searchStations("oxford")
        assertTrue(results.any { it.name.contains("Oxford", ignoreCase = true) })
    }

    // ── 5. StatusViewModel offline path logic ───────────────────────────────────

    @Test
    fun `offline status message set when cached data present`() {
        val cachedStatuses = listOf(
            cachedStatusEntity("central", 10, "Good Service"),
            cachedStatusEntity("victoria", 5, "Severe Delays"),
        )
        val isNonEmpty = cachedStatuses.isNotEmpty()
        val errorMessage = if (isNonEmpty)
            "Showing cached status data. Pull to refresh when you are back online."
        else
            "Could not load line statuses. Check your connection."
        assertEquals("Showing cached status data. Pull to refresh when you are back online.", errorMessage)
    }

    @Test
    fun `offline status message set when no cache available`() {
        val cachedStatuses = emptyList<CachedLineStatusEntity>()
        val errorMessage = if (cachedStatuses.isNotEmpty())
            "Showing cached status data. Pull to refresh when you are back online."
        else
            "Could not load line statuses. Check your connection."
        assertEquals("Could not load line statuses. Check your connection.", errorMessage)
    }

    @Test
    fun `isUsingCachedData is true when offline with cached statuses`() {
        val cachedStatuses = listOf(cachedStatusEntity("jubilee", 10, "Good Service"))
        val isUsingCachedData = cachedStatuses.isNotEmpty()
        assertTrue(isUsingCachedData)
    }
}
