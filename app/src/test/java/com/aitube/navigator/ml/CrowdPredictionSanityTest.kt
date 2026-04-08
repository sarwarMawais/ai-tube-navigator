package com.aitube.navigator.ml

import com.londontubeai.navigator.ml.CrowdPredictionEngine
import org.junit.Assert.assertTrue
import org.junit.Test

class CrowdPredictionSanityTest {

    private val engine = CrowdPredictionEngine()

    @Test
    fun `sanity snapshot for representative stations and hours`() {
        val stations = listOf("oxford-circus", "waterloo", "stratford", "morden")
        val hours = listOf(3, 8, 12, 17, 22)

        stations.forEach { stationId ->
            val values = hours.associateWith { hour -> engine.predict(stationId, hour) }

            val snapshot = hours.joinToString(" | ") { hour ->
                val result = values.getValue(hour)
                "%02d:00=%d%%".format(hour, result.crowdPercentage)
            }
            println("[CROWD_SANITY] $stationId -> $snapshot")

            values.values.forEach { result ->
                assertTrue("Percentage should stay calibrated", result.crowdPercentage in 6..97)
                assertTrue("Confidence should be bounded", result.confidence in 0.55f..0.96f)
            }

            val morning = values.getValue(8).crowdPercentage
            val lateNight = values.getValue(3).crowdPercentage
            assertTrue(
                "Morning should be >= late night for $stationId ($morning vs $lateNight)",
                morning >= lateNight,
            )
        }

        val centralPeak = engine.predict("oxford-circus", 8).crowdPercentage
        val outerPeak = engine.predict("morden", 8).crowdPercentage
        assertTrue(
            "Central station peak should be >= outer station peak ($centralPeak vs $outerPeak)",
            centralPeak >= outerPeak,
        )
    }

    @Test
    fun `weekday and weekend curves are deterministic`() {
        val stationId = "oxford-circus"

        val weekdayMorning = engine.predict(stationId, 8, CrowdPredictionEngine.DayType.WEEKDAY).crowdPercentage
        val weekdayLateNight = engine.predict(stationId, 3, CrowdPredictionEngine.DayType.WEEKDAY).crowdPercentage
        assertTrue(
            "Weekday morning should be >= weekday late night ($weekdayMorning vs $weekdayLateNight)",
            weekdayMorning >= weekdayLateNight,
        )

        val weekdayRush = engine.predict(stationId, 8, CrowdPredictionEngine.DayType.WEEKDAY).crowdPercentage
        val weekendSameHour = engine.predict(stationId, 8, CrowdPredictionEngine.DayType.WEEKEND).crowdPercentage
        assertTrue(
            "Weekday 08:00 should be >= weekend 08:00 ($weekdayRush vs $weekendSameHour)",
            weekdayRush >= weekendSameHour,
        )

        val weekendMidday = engine.predict(stationId, 12, CrowdPredictionEngine.DayType.WEEKEND).crowdPercentage
        val weekendEarly = engine.predict(stationId, 8, CrowdPredictionEngine.DayType.WEEKEND).crowdPercentage
        assertTrue(
            "Weekend midday should be >= weekend early morning ($weekendMidday vs $weekendEarly)",
            weekendMidday >= weekendEarly,
        )
    }
}
