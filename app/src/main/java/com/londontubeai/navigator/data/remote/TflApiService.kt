package com.londontubeai.navigator.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TflApiService {

    // ── Line Status ──────────────────────────────────────────
    @GET("Line/Mode/tube,elizabeth-line/Status")
    suspend fun getAllLineStatuses(): List<TflLineStatusResponse>

    @GET("Line/{lineId}/Status")
    suspend fun getLineStatus(
        @Path("lineId") lineId: String,
    ): List<TflLineStatusResponse>

    // ── Arrivals ─────────────────────────────────────────────
    @GET("Line/{lineId}/Arrivals/{stopPointId}")
    suspend fun getArrivals(
        @Path("lineId") lineId: String,
        @Path("stopPointId") stopPointId: String,
    ): List<TflArrivalResponse>

    @GET("StopPoint/{naptanId}/Arrivals")
    suspend fun getStationArrivals(
        @Path("naptanId") naptanId: String,
    ): List<TflArrivalResponse>

    @GET("StopPoint")
    suspend fun getNearbyStopPoints(
        @Query("stopTypes") stopTypes: List<String>,
        @Query("radius") radius: Int,
        @Query("useStopPointHierarchy") useStopPointHierarchy: Boolean = false,
        @Query("modes") modes: List<String>,
        @Query("returnLines") returnLines: Boolean = false,
        @Query("location.lat") latitude: Double,
        @Query("location.lon") longitude: Double,
    ): TflStopPointsResponse

    // ── Disruptions ──────────────────────────────────────────
    @GET("Line/Mode/tube,elizabeth-line/Disruption")
    suspend fun getAllDisruptions(): List<TflDisruptionResponse>

    @GET("Line/{lineId}/Disruption")
    suspend fun getLineDisruptions(
        @Path("lineId") lineId: String,
    ): List<TflDisruptionResponse>

    // ── Journey Planning ─────────────────────────────────────
    @GET("Journey/JourneyResults/{from}/to/{to}")
    suspend fun planJourney(
        @Path("from") fromStationId: String,
        @Path("to") toStationId: String,
        @Query("mode") mode: String = "tube",
        @Query("journeyPreference") preference: String = "leasttime",
    ): TflJourneyResponse

    // ── StopPoint / Station Info ─────────────────────────────
    @GET("StopPoint/{stopPointId}")
    suspend fun getStopPoint(
        @Path("stopPointId") stopPointId: String,
    ): TflStopPointResponse

    // ── Crowding ─────────────────────────────────────────────
    @GET("crowding/{naptan}/Live")
    suspend fun getLiveCrowding(
        @Path("naptan") naptanId: String,
    ): TflCrowdingResponse
}
