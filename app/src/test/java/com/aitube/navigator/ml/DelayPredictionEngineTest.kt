package com.aitube.navigator.ml

import com.londontubeai.navigator.ml.DelayPredictionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DelayPredictionEngineTest {

    private lateinit var engine: DelayPredictionEngine

    @Before
    fun setup() {
        engine = DelayPredictionEngine()
    }

    @Test
    fun `predict returns valid delay range`() {
        val result = engine.predict("victoria")
        assertTrue("Delay should be >= 0", result.expectedDelayMinutes >= 0)
        assertTrue("Delay should be <= 45", result.expectedDelayMinutes <= 45)
    }

    @Test
    fun `predict returns valid risk score range`() {
        val result = engine.predict("central")
        assertTrue("Risk should be >= 0", result.riskScore >= 0f)
        assertTrue("Risk should be <= 1", result.riskScore <= 1f)
    }

    @Test
    fun `predict returns valid confidence`() {
        val result = engine.predict("jubilee")
        assertTrue("Confidence should be > 0.5", result.confidence > 0.5f)
        assertTrue("Confidence should be <= 1", result.confidence <= 1f)
    }

    @Test
    fun `more reliable line has lower delay prediction`() {
        val reliable = engine.predict("waterloo-city")
        val lessReliable = engine.predict("district")
        assertTrue(
            "Waterloo & City (${reliable.expectedDelayMinutes}m) should have <= delay than District (${lessReliable.expectedDelayMinutes}m)",
            reliable.expectedDelayMinutes <= lessReliable.expectedDelayMinutes
        )
    }

    @Test
    fun `model version is set`() {
        val result = engine.predict("northern")
        assertEquals(DelayPredictionEngine.MODEL_VERSION, result.modelVersion)
    }

    @Test
    fun `recommendation is not blank`() {
        val result = engine.predict("piccadilly")
        assertTrue(result.recommendation.isNotBlank())
        assertTrue(result.lineName.isNotBlank())
    }

    @Test
    fun `predictAll returns predictions for all lines`() {
        val results = engine.predictAll()
        assertTrue("Should have predictions for multiple lines", results.size >= 10)
        assertTrue("Should be sorted by risk descending", results.first().riskScore >= results.last().riskScore)
    }
}
