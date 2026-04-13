package com.londontubeai.navigator.data.remote

import com.google.gson.annotations.SerializedName

data class TflLineStatusResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("modeName") val modeName: String?,
    @SerializedName("lineStatuses") val lineStatuses: List<TflLineStatusDetail>,
)

data class TflLineStatusDetail(
    @SerializedName("statusSeverity") val statusSeverity: Int,
    @SerializedName("statusSeverityDescription") val statusSeverityDescription: String,
    @SerializedName("reason") val reason: String?,
)

data class TflArrivalResponse(
    @SerializedName("id") val id: String,
    @SerializedName("stationName") val stationName: String,
    @SerializedName("lineId") val lineId: String,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("platformName") val platformName: String?,
    @SerializedName("direction") val direction: String?,
    @SerializedName("destinationName") val destinationName: String?,
    @SerializedName("timeToStation") val timeToStation: Int,
    @SerializedName("currentLocation") val currentLocation: String?,
    @SerializedName("towards") val towards: String?,
    @SerializedName("expectedArrival") val expectedArrival: String?,
)

data class TflJourneyResponse(
    @SerializedName("journeys") val journeys: List<TflJourney>?,
)

data class TflJourneyFare(
    @SerializedName("totalCost") val totalCost: Int?,
    @SerializedName("fares") val fares: List<TflFareDetail>?,
)

data class TflFareDetail(
    @SerializedName("lowZone") val lowZone: Int?,
    @SerializedName("highZone") val highZone: Int?,
    @SerializedName("cost") val cost: Int?,
    @SerializedName("chargeProfileName") val chargeProfileName: String?,
    @SerializedName("chargeLevel") val chargeLevel: String?,
    @SerializedName("isHopperFare") val isHopperFare: Boolean?,
)

data class TflJourney(
    @SerializedName("duration") val duration: Int,
    @SerializedName("legs") val legs: List<TflJourneyLeg>,
    @SerializedName("fare") val fare: TflJourneyFare? = null,
)

data class TflJourneyLeg(
    @SerializedName("duration") val duration: Int,
    @SerializedName("instruction") val instruction: TflInstruction?,
    @SerializedName("departurePoint") val departurePoint: TflPoint?,
    @SerializedName("arrivalPoint") val arrivalPoint: TflPoint?,
    @SerializedName("path") val path: TflPath?,
    @SerializedName("routeOptions") val routeOptions: List<TflRouteOption>?,
    @SerializedName("mode") val mode: TflMode?,
)

data class TflInstruction(
    @SerializedName("summary") val summary: String?,
    @SerializedName("detailed") val detailed: String?,
)

data class TflPoint(
    @SerializedName("naptanId") val naptanId: String?,
    @SerializedName("commonName") val commonName: String?,
    @SerializedName("indicator") val indicator: String?,
    @SerializedName("stopLetter") val stopLetter: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
)

data class TflPath(
    @SerializedName("lineString") val lineString: String?,
    @SerializedName("stopPoints") val stopPoints: List<TflPoint>?,
)

data class TflRouteOption(
    @SerializedName("name") val name: String?,
    @SerializedName("lineIdentifier") val lineIdentifier: TflLineIdentifier?,
)

data class TflLineIdentifier(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
)

data class TflStopPointsResponse(
    @SerializedName("centrePoint") val centrePoint: List<Double>?,
    @SerializedName("stopPoints") val stopPoints: List<TflStopPointResponse>?,
)

data class TflMode(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
)

data class TflStopPointResponse(
    @SerializedName("id") val id: String,
    @SerializedName("commonName") val commonName: String,
    @SerializedName("naptanId") val naptanId: String?,
    @SerializedName("indicator") val indicator: String?,
    @SerializedName("stopLetter") val stopLetter: String?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("distance") val distance: Double?,
    @SerializedName("modes") val modes: List<String>?,
    @SerializedName("stationNaptan") val stationNaptan: String?,
    @SerializedName("hubNaptanCode") val hubNaptanCode: String?,
    @SerializedName("lines") val lines: List<TflLineIdentifier>?,
    @SerializedName("additionalProperties") val additionalProperties: List<TflProperty>?,
)

data class TflProperty(
    @SerializedName("category") val category: String?,
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: String?,
)

data class TflCrowdingResponse(
    @SerializedName("naptan") val naptan: String?,
    @SerializedName("dataAvailable") val dataAvailable: Boolean?,
    @SerializedName("percentageOfBaseline") val percentageOfBaseline: Double?,
)

data class TflDisruptionResponse(
    @SerializedName("category") val category: String?,
    @SerializedName("categoryDescription") val categoryDescription: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("closureText") val closureText: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("affectedRoutes") val affectedRoutes: List<TflAffectedRoute>?,
    @SerializedName("affectedStops") val affectedStops: List<TflPoint>?,
)

data class TflStopPointSearchResponse(
    @SerializedName("matches") val matches: List<TflStopPointMatch>?,
    @SerializedName("total") val total: Int?,
)

data class TflStopPointMatch(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    @SerializedName("modes") val modes: List<String>?,
    @SerializedName("lines") val lines: List<TflLineIdentifier>?,
    @SerializedName("zone") val zone: String?,
)

data class TflAffectedRoute(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("routeSectionNaptanEntrySequence") val stops: List<TflPoint>?,
)
