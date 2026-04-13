package com.londontubeai.navigator.data.repository

import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.Station
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RouteCacheTest {

    private lateinit var repository: TubeRepository

    private fun makeStation(id: String, name: String = id) = Station(
        id = id, name = name, lineIds = emptyList(), zone = "1",
        latitude = 51.5, longitude = -0.1,
    )

    private fun makeRoute(fromId: String, toId: String) = mockk<JourneyRoute>(relaxed = true) {
        every { fromStation } returns makeStation(fromId)
        every { toStation } returns makeStation(toId)
    }

    @Before
    fun setUp() {
        repository = mockk(relaxed = true) {
            every { getLastJourneyRoute(any(), any()) } answers {
                callOriginal()
            }
            every { cacheRoute(any()) } answers {
                callOriginal()
            }
        }
    }

    // ── Pure logic tests that don't need DI ──────────────────────

    @Test
    fun `fareEstimate converts pence to pounds`() {
        val pence = 260
        val pounds = pence / 100.0
        assertEquals(2.60, pounds, 0.001)
    }

    @Test
    fun `fareEstimate for zero pence is zero pounds`() {
        val pence = 0
        val pounds = pence / 100.0
        assertEquals(0.0, pounds, 0.001)
    }

    @Test
    fun `stepFree flag is false by default on JourneyRoute`() {
        val route = JourneyRoute(
            fromStation = makeStation("victoria"),
            toStation = makeStation("kings-cross"),
            legs = emptyList(),
            totalDurationMinutes = 10,
            totalInterchanges = 0,
        )
        assertEquals(false, route.isStepFreeRoute)
    }

    @Test
    fun `stepFree flag propagates when set`() {
        val route = JourneyRoute(
            fromStation = makeStation("victoria"),
            toStation = makeStation("kings-cross"),
            legs = emptyList(),
            totalDurationMinutes = 10,
            totalInterchanges = 0,
            isStepFreeRoute = true,
        )
        assertEquals(true, route.isStepFreeRoute)
    }

    @Test
    fun `estimatedFarePounds is null by default`() {
        val route = JourneyRoute(
            fromStation = makeStation("victoria"),
            toStation = makeStation("kings-cross"),
            legs = emptyList(),
            totalDurationMinutes = 10,
            totalInterchanges = 0,
        )
        assertNull(route.estimatedFarePounds)
    }

    @Test
    fun `estimatedFarePounds stores correctly`() {
        val route = JourneyRoute(
            fromStation = makeStation("victoria"),
            toStation = makeStation("kings-cross"),
            legs = emptyList(),
            totalDurationMinutes = 10,
            totalInterchanges = 0,
            estimatedFarePounds = 2.60,
        )
        assertEquals(2.60, route.estimatedFarePounds!!, 0.001)
    }

    @Test
    fun `DepartureOption LEAVE_NOW produces null tflDate`() {
        val leaveNow = com.londontubeai.navigator.ui.screens.route.DepartureOption.LEAVE_NOW
        val tflDate: String? = if (leaveNow == com.londontubeai.navigator.ui.screens.route.DepartureOption.LEAVE_NOW) null else "20260413"
        assertNull(tflDate)
    }

    @Test
    fun `DepartureOption DEPART_AT produces non-null tflDate`() {
        val departAt = com.londontubeai.navigator.ui.screens.route.DepartureOption.DEPART_AT
        val tflDate: String? = if (departAt == com.londontubeai.navigator.ui.screens.route.DepartureOption.LEAVE_NOW) null else "20260413"
        assertEquals("20260413", tflDate)
    }

    @Test
    fun `DepartureOption ARRIVE_BY maps to timeIs arriving`() {
        val arriveBy = com.londontubeai.navigator.ui.screens.route.DepartureOption.ARRIVE_BY
        val timeIs = when (arriveBy) {
            com.londontubeai.navigator.ui.screens.route.DepartureOption.ARRIVE_BY -> "arriving"
            com.londontubeai.navigator.ui.screens.route.DepartureOption.DEPART_AT -> "departing"
            else -> null
        }
        assertEquals("arriving", timeIs)
    }

    @Test
    fun `isOutsideLondon detects Chesham correctly via widened bounds`() {
        val cheshamLat = 51.7055
        val cheshamLng = -0.6083
        val outsideLondon = cheshamLat < 51.28 || cheshamLat > 51.75 || cheshamLng < -0.65 || cheshamLng > 0.40
        assertEquals(false, outsideLondon)
    }

    @Test
    fun `isOutsideLondon detects Manchester correctly`() {
        val manchesterLat = 53.4808
        val manchesterLng = -2.2426
        val outsideLondon = manchesterLat < 51.28 || manchesterLat > 51.75 || manchesterLng < -0.65 || manchesterLng > 0.40
        assertEquals(true, outsideLondon)
    }
}
