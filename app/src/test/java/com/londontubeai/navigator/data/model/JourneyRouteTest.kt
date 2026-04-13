package com.londontubeai.navigator.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyRouteTest {

    private fun station(id: String) = Station(
        id = id, name = id, lineIds = emptyList(), zone = "1",
        latitude = 51.5, longitude = -0.1,
    )

    private fun baseRoute() = JourneyRoute(
        fromStation = station("victoria"),
        toStation = station("kings-cross"),
        legs = emptyList(),
        totalDurationMinutes = 12,
        totalInterchanges = 0,
    )

    @Test
    fun `default estimatedFarePounds is null`() {
        assertNull(baseRoute().estimatedFarePounds)
    }

    @Test
    fun `default isStepFreeRoute is false`() {
        assertFalse(baseRoute().isStepFreeRoute)
    }

    @Test
    fun `fare stored and retrieved correctly`() {
        val r = baseRoute().copy(estimatedFarePounds = 3.10)
        assertEquals(3.10, r.estimatedFarePounds!!, 0.001)
    }

    @Test
    fun `isStepFreeRoute stored and retrieved correctly`() {
        val r = baseRoute().copy(isStepFreeRoute = true)
        assertTrue(r.isStepFreeRoute)
    }

    @Test
    fun `calorieBurned is proportional to duration`() {
        val duration = 20
        val expected = (duration * 0.7).toInt()
        val r = baseRoute().copy(totalDurationMinutes = duration, calorieBurned = expected)
        assertEquals(14, r.calorieBurned)
    }

    @Test
    fun `co2SavedGrams is proportional to duration`() {
        val duration = 20
        val r = baseRoute().copy(totalDurationMinutes = duration, co2SavedGrams = duration * 14)
        assertEquals(280, r.co2SavedGrams)
    }

    @Test
    fun `totalInterchanges direct service is zero`() {
        val r = baseRoute().copy(totalInterchanges = 0)
        assertEquals(0, r.totalInterchanges)
    }

    @Test
    fun `totalWalkingMinutes defaults to zero`() {
        assertEquals(0, baseRoute().totalWalkingMinutes)
    }

    @Test
    fun `tfl fare pence to pounds conversion is accurate`() {
        val penceValues = listOf(160, 260, 320, 480)
        val expectedPounds = listOf(1.60, 2.60, 3.20, 4.80)
        penceValues.forEachIndexed { i, pence ->
            assertEquals(expectedPounds[i], pence / 100.0, 0.001)
        }
    }

    @Test
    fun `share text format contains from and to station names`() {
        val from = "Victoria"
        val to = "King's Cross"
        val mins = 12
        val changes = 0
        val shareText = "\uD83D\uDE87 AI Tube Navigator\n$from → $to\n⏱ $mins min · $changes changes\nVia: Victoria line\nPlan your journey: https://aitube.navigator"
        assertTrue(shareText.contains(from))
        assertTrue(shareText.contains(to))
        assertTrue(shareText.contains("$mins min"))
    }
}
