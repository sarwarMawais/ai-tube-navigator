package com.aitube.navigator.ml

import com.londontubeai.navigator.ml.CrowdPredictionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrowdPredictionEngineTest {

    private lateinit var engine: CrowdPredictionEngine

    @Before
    fun setup() {
        engine = CrowdPredictionEngine()
    }

    @Test
    fun `predict returns valid percentage range`() {
        val result = engine.predict("oxford-circus", 8)
        assertTrue("Percentage should be >= 3", result.crowdPercentage >= 3)
        assertTrue("Percentage should be <= 98", result.crowdPercentage <= 98)
    }

    @Test
    fun `predict returns valid confidence range`() {
        val result = engine.predict("oxford-circus", 8)
        assertTrue("Confidence should be >= 0", result.confidence >= 0f)
        assertTrue("Confidence should be <= 1", result.confidence <= 1f)
    }

    @Test
    fun `morning demand is higher than late night`() {
        val peak = engine.predict("oxford-circus", 8)
        val offPeak = engine.predict("oxford-circus", 3)
        assertTrue(
            "Morning (${peak.crowdPercentage}%) should be >= late night (${offPeak.crowdPercentage}%)",
            peak.crowdPercentage >= offPeak.crowdPercentage
        )
    }

    @Test
    fun `zone 1 station is busier than outer zone`() {
        val zone1 = engine.predict("oxford-circus", 8)
        val outerZone = engine.predict("morden", 8)
        assertTrue(
            "Zone 1 (${zone1.crowdPercentage}%) should be >= outer zone (${outerZone.crowdPercentage}%)",
            zone1.crowdPercentage >= outerZone.crowdPercentage
        )
    }

    @Test
    fun `model version is set`() {
        val result = engine.predict("oxford-circus", 12)
        assertEquals(CrowdPredictionEngine.MODEL_VERSION, result.modelVersion)
    }

    @Test
    fun `predictToCrowdPrediction returns valid CrowdPrediction`() {
        val prediction = engine.predictToCrowdPrediction("waterloo", 17)
        assertEquals("waterloo", prediction.stationId)
        assertTrue(prediction.percentageFull in 3..98)
        assertTrue(prediction.recommendation.isNotBlank())
        assertTrue(prediction.timeSlot.contains("–"))
    }

    @Test
    fun `unknown station returns fallback prediction`() {
        val result = engine.predict("nonexistent-station", 12)
        assertTrue(result.crowdPercentage in 3..98)
        assertTrue(result.confidence >= 0.4f)
    }
}
