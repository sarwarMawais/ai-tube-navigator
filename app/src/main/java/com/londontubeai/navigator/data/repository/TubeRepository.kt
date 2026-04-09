package com.londontubeai.navigator.data.repository

import com.londontubeai.navigator.data.local.dao.TubeDao
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.local.entity.UserPreferencesEntity
import com.londontubeai.navigator.data.model.AiInsight
import com.londontubeai.navigator.data.model.CarriageRecommendation
import com.londontubeai.navigator.data.model.CrowdLevel
import com.londontubeai.navigator.data.model.CrowdPrediction
import com.londontubeai.navigator.data.model.Disruption
import com.londontubeai.navigator.data.model.DisruptionSeverity
import com.londontubeai.navigator.data.model.InsightType
import com.londontubeai.navigator.data.model.JourneyLeg
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.TransportMode
import com.londontubeai.navigator.data.model.TubeLine
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.LiveArrival
import com.londontubeai.navigator.data.model.NearbyStopPoint
import com.londontubeai.navigator.data.model.NetworkLiveStatus
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.StationArrivals
import com.londontubeai.navigator.data.model.NaptanIds
import com.londontubeai.navigator.data.model.StationConnection
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.remote.TflApiService
import com.londontubeai.navigator.ml.DelayPredictionEngine
import com.londontubeai.navigator.ml.CrowdPredictionEngine
import com.londontubeai.navigator.ui.theme.TubeLineColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TubeRepository @Inject constructor(
    private val api: TflApiService,
    private val dao: TubeDao,
    private val crowdEngine: CrowdPredictionEngine,
    private val delayEngine: DelayPredictionEngine,
) {
    // ── Line Status ─────────────────────────────────────────

    suspend fun fetchLiveLineStatuses(): Result<List<LineStatus>> = runCatching {
        val response = api.getAllLineStatuses()
        
        // Validate response
        if (response.isEmpty()) {
            throw IllegalArgumentException("Empty response from TfL API")
        }
        
        val statuses = response.mapNotNull { line ->
            try {
                validateLineStatusResponse(line)?.let { validated ->
                    val detail = validated.lineStatuses.firstOrNull()
                    val tubeColor = TubeData.getLineById(validated.id)?.color ?: TubeLineColors.Jubilee
                    LineStatus(
                        lineId = validated.id,
                        lineName = validated.name,
                        lineColor = tubeColor,
                        statusSeverity = detail?.statusSeverity ?: 0,
                        statusDescription = detail?.statusSeverityDescription ?: "Unknown",
                        reason = detail?.reason,
                        isGoodService = (detail?.statusSeverity ?: 0) >= 10,
                    )
                }
            } catch (e: Exception) {
                // Log error but continue processing other lines
                null
            }
        }
        
        if (statuses.isEmpty()) {
            throw IllegalArgumentException("No valid line status data received")
        }
        
        dao.insertLineStatuses(statuses.map { it.toCachedEntity() })
        statuses
    }

    fun getCachedLineStatuses(): Flow<List<CachedLineStatusEntity>> =
        dao.getCachedLineStatuses()

    suspend fun getLastStatusUpdate(): Long? = dao.getLastStatusUpdate()

    // ── Real-Time Arrivals ───────────────────────────────────

    suspend fun fetchStationArrivals(stationId: String): Result<StationArrivals> = runCatching {
        val station = TubeData.getStationById(stationId)
            ?: throw IllegalArgumentException("Station not found: $stationId")
        val naptanId = NaptanIds.forStation(stationId)
            ?: throw IllegalArgumentException("No NapTan ID for station: $stationId")
        val arrivals = mapArrivals(api.getStationArrivals(naptanId))
        StationArrivals(
            stationId = stationId,
            stationName = station.name,
            arrivals = arrivals,
        )
    }

    suspend fun fetchStopPointArrivals(stopPointId: String): Result<List<LiveArrival>> = runCatching {
        mapArrivals(api.getStationArrivals(stopPointId))
    }

    suspend fun fetchNearbyBusStopPoints(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 350,
    ): Result<List<NearbyStopPoint>> = runCatching {
        api.getNearbyStopPoints(
            stopTypes = listOf("NaptanPublicBusCoachTram"),
            radius = radiusMeters,
            useStopPointHierarchy = false,
            modes = listOf("bus"),
            returnLines = false,
            latitude = latitude,
            longitude = longitude,
        ).stopPoints.orEmpty()
            .filter { stop -> stop.modes.orEmpty().any { it.equals("bus", ignoreCase = true) } }
            .map { stop ->
                NearbyStopPoint(
                    id = stop.id,
                    name = stop.commonName,
                    indicator = stop.indicator ?: stop.stopLetter,
                    towards = stop.additionalProperties
                        ?.firstOrNull { prop ->
                            prop.category.equals("Direction", ignoreCase = true) &&
                                prop.key.equals("Towards", ignoreCase = true)
                        }
                        ?.value,
                    latitude = stop.lat,
                    longitude = stop.lon,
                    distanceMeters = (stop.distance ?: 0.0).toInt(),
                )
            }
            .sortedBy { it.distanceMeters }
    }

    // ── Disruptions ──────────────────────────────────────────

    suspend fun fetchDisruptions(): Result<List<Disruption>> = runCatching {
        val response = api.getAllDisruptions()
        response.map { d ->
            val severity = when {
                d.categoryDescription?.contains("closure", ignoreCase = true) == true -> DisruptionSeverity.CLOSURE
                d.categoryDescription?.contains("severe", ignoreCase = true) == true -> DisruptionSeverity.SEVERE
                d.categoryDescription?.contains("part", ignoreCase = true) == true -> DisruptionSeverity.MODERATE
                else -> DisruptionSeverity.MINOR
            }
            Disruption(
                category = d.category ?: "Unknown",
                description = d.description ?: "No details available",
                closureText = d.closureText,
                type = d.type ?: "Unknown",
                affectedLineIds = d.affectedRoutes?.mapNotNull { it.id } ?: emptyList(),
                severity = severity,
            )
        }
    }

    // ── Network Live Status (combined) ───────────────────────

    suspend fun fetchNetworkLiveStatus(): NetworkLiveStatus {
        val statuses = fetchLiveLineStatuses().getOrDefault(emptyList())
        val disruptions = fetchDisruptions().getOrDefault(emptyList())
        return NetworkLiveStatus(
            lineStatuses = statuses,
            disruptions = disruptions,
            isLive = statuses.isNotEmpty(),
        )
    }

    // ── Saved Journeys ──────────────────────────────────────

    fun getRecentJourneys(limit: Int = 10): Flow<List<SavedJourneyEntity>> =
        dao.getRecentJourneys(limit)

    fun getFavouriteJourneys(): Flow<List<SavedJourneyEntity>> =
        dao.getFavouriteJourneys()

    suspend fun saveJourney(from: Station, to: Station): Long =
        dao.insertJourney(
            SavedJourneyEntity(
                fromStationId = from.id,
                fromStationName = from.name,
                toStationId = to.id,
                toStationName = to.name,
            )
        )

    suspend fun toggleFavourite(journey: SavedJourneyEntity) =
        dao.updateJourney(journey.copy(isFavourite = !journey.isFavourite))

    suspend fun deleteJourney(id: Long) = dao.deleteJourney(id)

    // ── User Preferences ────────────────────────────────────

    fun getUserPreferences(): Flow<UserPreferencesEntity?> =
        dao.getUserPreferences()

    suspend fun saveUserPreferences(prefs: UserPreferencesEntity) =
        dao.saveUserPreferences(prefs)

    suspend fun searchPlaces(query: String): Result<List<Station>> = runCatching {
        val tflResults = try {
            api.searchStopPoints(query).matches.orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        val tubeMatches = TubeData.searchStations(query)
        val tubeNamesLower = tubeMatches.map { it.name.lowercase() }.toSet()
        val extra = tflResults.mapNotNull { match ->
            val lat = match.lat ?: return@mapNotNull null
            val lon = match.lon ?: return@mapNotNull null
            if (match.name.lowercase() in tubeNamesLower) return@mapNotNull null
            Station(
                id = "place:${match.id}",
                name = match.name,
                lineIds = match.lines?.mapNotNull { it.id } ?: emptyList(),
                zone = match.zone ?: "",
                latitude = lat,
                longitude = lon,
            )
        }
        (tubeMatches + extra).take(8)
    }

    /**
     * Fetch ALL journey routes from a single TfL API call.
     * TfL typically returns 3-5 journey options per request.
     */
    suspend fun fetchAllJourneyRoutesFromTfl(
        fromStationId: String,
        toStationId: String,
        preference: String = "FASTEST",
        fromLat: Double? = null,
        fromLng: Double? = null,
        toLat: Double? = null,
        toLng: Double? = null,
        toDisplayName: String? = null,
    ): Result<List<JourneyRoute>> = runCatching {
        val resolved = resolveJourneyParams(fromStationId, toStationId, fromLat, fromLng, toLat, toLng, toDisplayName)
        val tflPreference = when (preference.uppercase()) {
            "FEWEST_CHANGES" -> "leastinterchange"
            "LEAST_WALKING" -> "leastwalking"
            else -> "leasttime"
        }
        val journeyResponse = api.planJourney(
            fromStationId = resolved.fromParam,
            toStationId = resolved.toParam,
            mode = "tube,elizabeth-line,bus,walking",
            preference = tflPreference,
        )
        journeyResponse.journeys?.take(5)?.mapNotNull { journey ->
            try { mapTflJourneyToRoute(journey, resolved.fromStation, resolved.toStation) } catch (_: Exception) { null }
        } ?: emptyList()
    }

    suspend fun fetchJourneyRouteFromTfl(
        fromStationId: String,
        toStationId: String,
        preference: String = "FASTEST",
        fromLat: Double? = null,
        fromLng: Double? = null,
        toLat: Double? = null,
        toLng: Double? = null,
        toDisplayName: String? = null,
    ): Result<JourneyRoute> = runCatching {
        val resolved = resolveJourneyParams(fromStationId, toStationId, fromLat, fromLng, toLat, toLng, toDisplayName)
        val tflPreference = when (preference.uppercase()) {
            "FEWEST_CHANGES" -> "leastinterchange"
            "LEAST_WALKING" -> "leastwalking"
            else -> "leasttime"
        }
        val journeyResponse = api.planJourney(
            fromStationId = resolved.fromParam,
            toStationId = resolved.toParam,
            mode = "tube,elizabeth-line,bus,walking",
            preference = tflPreference,
        )
        val selectedJourney = journeyResponse.journeys?.firstOrNull()
            ?: throw IllegalStateException("No TfL journey legs available")
        mapTflJourneyToRoute(selectedJourney, resolved.fromStation, resolved.toStation)
    }

    private data class ResolvedJourneyParams(
        val fromStation: Station,
        val toStation: Station,
        val fromParam: String,
        val toParam: String,
    )

    private fun resolveJourneyParams(
        fromStationId: String,
        toStationId: String,
        fromLat: Double?,
        fromLng: Double?,
        toLat: Double?,
        toLng: Double?,
        toDisplayName: String?,
    ): ResolvedJourneyParams {
        val usingCoordinates = fromLat != null && fromLng != null
        val fromStation = if (usingCoordinates) {
            Station(id = "my-location", name = "My Location", lineIds = emptyList(), zone = "", latitude = fromLat!!, longitude = fromLng!!)
        } else {
            TubeData.getStationById(fromStationId) ?: throw IllegalArgumentException("Unknown from station: $fromStationId")
        }
        val usingToCoordinates = toLat != null && toLng != null || toStationId.startsWith("place:")
        val toStation = if (usingToCoordinates) {
            Station(id = toStationId, name = toDisplayName ?: TubeData.getStationById(toStationId)?.name ?: "Destination", lineIds = emptyList(), zone = "", latitude = toLat ?: 0.0, longitude = toLng ?: 0.0)
        } else {
            TubeData.getStationById(toStationId) ?: throw IllegalArgumentException("Unknown to station: $toStationId")
        }
        val fromParam = if (usingCoordinates) "$fromLat,$fromLng" else NaptanIds.forStation(fromStationId) ?: throw IllegalArgumentException("No NapTan mapping for: $fromStationId")
        val toParam = if (usingToCoordinates) "${toLat ?: toStation.latitude},${toLng ?: toStation.longitude}" else NaptanIds.forStation(toStationId) ?: throw IllegalArgumentException("No NapTan mapping for: $toStationId")
        return ResolvedJourneyParams(fromStation, toStation, fromParam, toParam)
    }

    private fun mapTflJourneyToRoute(
        selectedJourney: com.londontubeai.navigator.data.remote.TflJourney,
        fromStation: Station,
        toStation: Station,
    ): JourneyRoute {

        val mappedLegs = selectedJourney.legs.mapIndexedNotNull { index, tflLeg ->
            val previousLeg = selectedJourney.legs.getOrNull(index - 1)
            val fallbackFrom = previousLeg?.arrivalPoint?.let { resolveStationFromPoint(it, fromStation) } ?: fromStation
            val from = resolveStationFromPoint(tflLeg.departurePoint, fallbackFrom)
            val to = resolveStationFromPoint(
                tflLeg.arrivalPoint,
                if (index == selectedJourney.legs.lastIndex) toStation else from,
            )

            val modeId = tflLeg.mode?.id?.lowercase().orEmpty()
            val mode = when {
                modeId.contains("walk") -> TransportMode.WALKING
                modeId.contains("bus") -> TransportMode.BUS
                else -> TransportMode.TUBE
            }

            val lineIdentifier = tflLeg.routeOptions?.firstOrNull()?.lineIdentifier
            val mappedLine = when (mode) {
                TransportMode.WALKING -> TubeLine(
                    id = "walking",
                    name = "Walking",
                    color = androidx.compose.ui.graphics.Color(0xFF607D8B),
                    stationIds = emptyList(),
                )
                TransportMode.BUS -> TubeLine(
                    id = lineIdentifier?.id ?: "bus",
                    name = lineIdentifier?.name ?: "Bus",
                    color = androidx.compose.ui.graphics.Color(0xFFE32017),
                    stationIds = emptyList(),
                )
                TransportMode.TUBE -> {
                    val maybeId = lineIdentifier?.id
                    val byId = maybeId?.let { TubeData.getLineById(it) }
                    byId ?: TubeLine(
                        id = maybeId ?: "tube",
                        name = lineIdentifier?.name ?: tflLeg.routeOptions?.firstOrNull()?.name ?: "Tube",
                        color = TubeLineColors.Jubilee,
                        stationIds = emptyList(),
                    )
                }
            }

            val stationIds = tflLeg.path?.stopPoints
                ?.mapNotNull { point ->
                    point.naptanId
                        ?.let { naptan -> NaptanIds.stationIdForNaptan(naptan) }
                        ?.takeIf { stationId -> TubeData.getStationById(stationId) != null }
                }
                ?.distinct()
                ?.toMutableList()
                ?: mutableListOf()

            if (stationIds.isEmpty()) {
                stationIds.add(from.id)
                stationIds.add(to.id)
            } else {
                if (stationIds.firstOrNull() != from.id) stationIds.add(0, from.id)
                if (stationIds.lastOrNull() != to.id) stationIds.add(to.id)
            }

            val instructionSummary = tflLeg.instruction?.summary.orEmpty()
            val instructionDetailed = tflLeg.instruction?.detailed.orEmpty()
            val directionText = when (mode) {
                TransportMode.BUS -> extractTowards(instructionSummary) ?: "Towards ${to.name}"
                TransportMode.TUBE -> extractTowards(instructionSummary) ?: "Towards ${to.name}"
                TransportMode.WALKING -> instructionSummary.ifEmpty { "Walk to ${to.name}" }
            }
            val platformText = extractPlatformFromInstruction(instructionDetailed.ifEmpty { instructionSummary })
                ?: if (mode == TransportMode.TUBE) getPlatformForLine(mappedLine.id, from) else ""

            JourneyLeg(
                fromStation = from,
                toStation = to,
                line = mappedLine,
                durationMinutes = tflLeg.duration.coerceAtLeast(1),
                direction = directionText,
                intermediateStops = (stationIds.size - 2).coerceAtLeast(0),
                stationIds = stationIds,
                mode = mode,
                walkingDistanceMeters = if (mode == TransportMode.WALKING) (tflLeg.duration * 80).coerceAtLeast(60) else 0,
                walkingDirections = if (mode == TransportMode.WALKING) {
                    instructionDetailed.ifEmpty { instructionSummary }
                } else { "" },
                busRouteNumber = if (mode == TransportMode.BUS) (lineIdentifier?.id ?: mappedLine.id).uppercase() else "",
                busStopName = if (mode == TransportMode.BUS) tflLeg.departurePoint?.commonName.orEmpty() else "",
                busAlightStopName = if (mode == TransportMode.BUS) tflLeg.arrivalPoint?.commonName.orEmpty() else "",
                nextDepartureMinutes = 0,
                platformNumber = platformText,
            )
        }

        if (mappedLegs.isEmpty()) {
            throw IllegalStateException("TfL journey returned no usable legs")
        }

        val totalDuration = selectedJourney.duration.takeIf { it > 0 } ?: mappedLegs.sumOf { it.durationMinutes }
        val tubeLegs = mappedLegs.filter { it.mode == TransportMode.TUBE }
        val interchanges = if (tubeLegs.size <= 1) 0 else {
            tubeLegs.zipWithNext().count { (a, b) -> a.line.id != b.line.id }
        }
        val totalStops = tubeLegs.sumOf { (it.stationIds.size - 1).coerceAtLeast(0) }
        val totalWalkingMinutes = mappedLegs.filter { it.mode == TransportMode.WALKING }.sumOf { it.durationMinutes }
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return JourneyRoute(
            fromStation = fromStation,
            toStation = toStation,
            legs = mappedLegs,
            totalDurationMinutes = totalDuration,
            totalInterchanges = interchanges,
            totalStops = totalStops,
            totalWalkingMinutes = totalWalkingMinutes,
            aiTimePredictionMinutes = totalDuration,
            carriageRecommendation = getBestExitForStation(toStation),
            crowdPrediction = predictCrowding(fromStation.id, hour),
            calorieBurned = (totalDuration * 0.7).toInt(),
            co2SavedGrams = totalDuration * 14,
        )
    }

    // ── Graph-Based Route Finding (BFS / Dijkstra) ──────────

    fun findRoute(fromId: String, toId: String, preference: String = "FASTEST"): JourneyRoute? {
        val fromStation = TubeData.getStationById(fromId) ?: return null
        val toStation = TubeData.getStationById(toId) ?: return null
        if (fromId == toId) return null

        // Dijkstra with time-based weights
        val dist = mutableMapOf<String, Int>()
        val prev = mutableMapOf<String, Pair<String, StationConnection>>()
        val visited = mutableSetOf<String>()
        val queue = LinkedList<Pair<String, Int>>()

        dist[fromId] = 0
        queue.add(fromId to 0)

        while (queue.isNotEmpty()) {
            queue.sortBy { it.second }
            val (current, currentDist) = queue.poll() ?: break
            if (current in visited) continue
            visited.add(current)
            if (current == toId) break

            val neighbours = TubeData.getNeighbours(current)
            for (conn in neighbours) {
                val neighbour = conn.toStationId
                if (neighbour in visited) continue
                // Skip non-step-free stations if step-free preference
                if (preference == "STEP_FREE") {
                    val neighbourStation = TubeData.getStationById(neighbour)
                    if (neighbourStation != null && !neighbourStation.hasStepFreeAccess) continue
                }
                // Add interchange time if switching lines
                val isInterchange = prev[current] != null && prev[current]!!.second.lineId != conn.lineId
                val baseInterchangePenalty = if (isInterchange) {
                    val station = TubeData.getStationById(current)
                    station?.interchangeTimeMinutes?.coerceAtLeast(2) ?: 3
                } else 0
                // Adjust penalties based on preference
                val interchangePenalty = when (preference) {
                    "FEWEST_CHANGES" -> if (isInterchange) baseInterchangePenalty + 15 else 0
                    "LEAST_WALKING" -> baseInterchangePenalty + (if (isInterchange) 8 else 0)
                    else -> baseInterchangePenalty
                }
                val newDist = currentDist + conn.travelTimeMinutes + interchangePenalty
                if (newDist < (dist[neighbour] ?: Int.MAX_VALUE)) {
                    dist[neighbour] = newDist
                    prev[neighbour] = current to conn
                    queue.add(neighbour to newDist)
                }
            }
        }

        if (toId !in prev && fromId != toId) return null

        // Reconstruct path
        val pathConnections = mutableListOf<StationConnection>()
        var current = toId
        while (current != fromId && current in prev) {
            val (prevStation, conn) = prev[current]!!
            pathConnections.add(0, conn)
            current = prevStation
        }
        if (pathConnections.isEmpty()) return null

        // Build journey legs (group consecutive connections on same line)
        val tubeLegs = mutableListOf<JourneyLeg>()
        var legStart = pathConnections.first().fromStationId
        var legLine = pathConnections.first().lineId
        var legDuration = 0
        var legStations = mutableListOf(legStart)
        var totalStops = 0
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        val isPeak = hour in 7..9 || hour in 17..19

        for (conn in pathConnections) {
            if (conn.lineId != legLine) {
                // Finish current leg
                val line = TubeData.getLineById(legLine)
                val legFromStation = TubeData.getStationById(legStart)
                val legToStation = TubeData.getStationById(legStations.last())
                if (line != null && legFromStation != null && legToStation != null) {
                    val freq = if (isPeak) line.peakFrequencyMinutes else line.offPeakFrequencyMinutes
                    tubeLegs.add(JourneyLeg(
                        fromStation = legFromStation,
                        toStation = legToStation,
                        line = line,
                        durationMinutes = legDuration,
                        direction = "Towards ${getTerminusDirection(legLine, legStart, legStations.last())}",
                        intermediateStops = legStations.size - 1,
                        stationIds = legStations.toList(),
                        mode = TransportMode.TUBE,
                        nextDepartureMinutes = (1..freq).random(),
                        platformNumber = getPlatformForLine(legLine, legFromStation),
                    ))
                }
                totalStops += legStations.size - 1
                // Start new leg
                legStart = conn.fromStationId
                legLine = conn.lineId
                legDuration = conn.travelTimeMinutes
                legStations = mutableListOf(legStart, conn.toStationId)
            } else {
                legDuration += conn.travelTimeMinutes
                legStations.add(conn.toStationId)
            }
        }
        // Finish last leg
        val lastLine = TubeData.getLineById(legLine)
        val lastFrom = TubeData.getStationById(legStart)
        val lastTo = TubeData.getStationById(legStations.last())
        if (lastLine != null && lastFrom != null && lastTo != null) {
            val freq = if (isPeak) lastLine.peakFrequencyMinutes else lastLine.offPeakFrequencyMinutes
            tubeLegs.add(JourneyLeg(
                fromStation = lastFrom,
                toStation = lastTo,
                line = lastLine,
                durationMinutes = legDuration,
                direction = "Towards ${getTerminusDirection(legLine, legStart, legStations.last())}",
                intermediateStops = legStations.size - 1,
                stationIds = legStations.toList(),
                mode = TransportMode.TUBE,
                nextDepartureMinutes = (1..freq).random(),
                platformNumber = getPlatformForLine(legLine, lastFrom),
            ))
        }
        totalStops += legStations.size - 1

        // Insert walking interchange legs between tube legs
        val allLegs = mutableListOf<JourneyLeg>()
        var totalWalkingMinutes = 0
        for (i in tubeLegs.indices) {
            allLegs.add(tubeLegs[i])
            if (i < tubeLegs.size - 1) {
                val interchangeStation = tubeLegs[i].toStation
                val walkTime = interchangeStation.interchangeTimeMinutes.coerceAtLeast(2)
                totalWalkingMinutes += walkTime
                val walkDistM = walkTime * 60 // ~1m/s walking speed
                val walkingLine = TubeData.getLineById("walking") ?: TubeLine(
                    id = "walking", name = "Walking", color = androidx.compose.ui.graphics.Color(0xFF607D8B),
                    stationIds = emptyList()
                )
                allLegs.add(JourneyLeg(
                    fromStation = interchangeStation,
                    toStation = tubeLegs[i + 1].fromStation,
                    line = walkingLine,
                    durationMinutes = walkTime,
                    direction = "Walk to ${tubeLegs[i + 1].line.name} platform",
                    intermediateStops = 0,
                    mode = TransportMode.WALKING,
                    walkingDistanceMeters = walkDistM,
                    walkingDirections = "Follow signs for ${tubeLegs[i + 1].line.name} at ${interchangeStation.name}",
                ))
            }
        }

        val totalDuration = dist[toId] ?: allLegs.sumOf { it.durationMinutes }
        val interchanges = (tubeLegs.size - 1).coerceAtLeast(0)
        val peakMultiplier = if (isPeak) 1.15f else 1.0f
        val aiPrediction = (totalDuration * peakMultiplier).toInt()
        val carriageRec = getBestExitForStation(toStation)
        val crowdPred = predictCrowding(fromId, hour)
        val calories = (totalDuration * 0.7).toInt()
        val co2Saved = (totalDuration * 14)

        return JourneyRoute(
            fromStation = fromStation,
            toStation = toStation,
            legs = allLegs,
            totalDurationMinutes = totalDuration,
            totalInterchanges = interchanges,
            totalStops = totalStops,
            totalWalkingMinutes = totalWalkingMinutes,
            aiTimePredictionMinutes = aiPrediction,
            carriageRecommendation = carriageRec,
            crowdPrediction = crowdPred,
            calorieBurned = calories,
            co2SavedGrams = co2Saved,
        )
    }

    private val stationPlatformData: Map<String, Map<String, String>> = mapOf(
        "oxford-circus" to mapOf("central" to "Plat. 1/2", "victoria" to "Plat. 3/4", "bakerloo" to "Plat. 5/6"),
        "bank" to mapOf("central" to "Plat. 1/2", "northern" to "Plat. 3/4", "waterloo-city" to "Plat. 1"),
        "kings-cross-st-pancras" to mapOf("piccadilly" to "Plat. 4/5", "victoria" to "Plat. 1/2", "northern" to "Plat. 6/7", "circle" to "Plat. 9/10", "hammersmith-city" to "Plat. 9/10", "metropolitan" to "Plat. 9/10"),
        "waterloo" to mapOf("jubilee" to "Plat. 1/2", "northern" to "Plat. 3/4", "bakerloo" to "Plat. 5/6", "waterloo-city" to "Plat. 8"),
        "london-bridge" to mapOf("jubilee" to "Plat. 1/2", "northern" to "Plat. 3/4"),
        "liverpool-street" to mapOf("central" to "Plat. 1/2", "circle" to "Plat. 6/7", "hammersmith-city" to "Plat. 6/7", "metropolitan" to "Plat. 5/6", "elizabeth-line" to "Plat. 10/11"),
        "victoria" to mapOf("victoria" to "Plat. 1/2", "circle" to "Plat. 4/5", "district" to "Plat. 4/5"),
        "euston" to mapOf("northern" to "Plat. 1-4", "victoria" to "Plat. 5/6"),
        "holborn" to mapOf("central" to "Plat. 1/2", "piccadilly" to "Plat. 3/4"),
        "paddington" to mapOf("district" to "Plat. 1/2", "circle" to "Plat. 1/2", "hammersmith-city" to "Plat. 3/4", "bakerloo" to "Plat. 5/6", "elizabeth-line" to "Plat. 7/8"),
        "canary-wharf" to mapOf("jubilee" to "Plat. 1/2", "elizabeth-line" to "Plat. 3/4"),
        "stratford" to mapOf("central" to "Plat. 1/2", "jubilee" to "Plat. 3/4", "elizabeth-line" to "Plat. 10/11"),
        "moorgate" to mapOf("circle" to "Plat. 1/2", "hammersmith-city" to "Plat. 1/2", "metropolitan" to "Plat. 3/4", "northern" to "Plat. 5/6"),
        "warren-street" to mapOf("northern" to "Plat. 1/2", "victoria" to "Plat. 3/4"),
        "green-park" to mapOf("jubilee" to "Plat. 1/2", "victoria" to "Plat. 3/4", "piccadilly" to "Plat. 5/6"),
        "monument" to mapOf("circle" to "Plat. 1/2", "district" to "Plat. 1/2"),
        "tottenham-court-road" to mapOf("central" to "Plat. 1/2", "northern" to "Plat. 3/4", "elizabeth-line" to "Plat. 5/6"),
        "mile-end" to mapOf("central" to "Plat. 1/2", "district" to "Plat. 3/4", "hammersmith-city" to "Plat. 3/4"),
        "hammersmith" to mapOf("district" to "Plat. 1/2", "piccadilly" to "Plat. 3/4", "circle" to "Plat. 5/6", "hammersmith-city" to "Plat. 5/6"),
        "earls-court" to mapOf("district" to "Plat. 1/2", "piccadilly" to "Plat. 3/4"),
        "finsbury-park" to mapOf("piccadilly" to "Plat. 1/2", "victoria" to "Plat. 3/4"),
        "highbury-and-islington" to mapOf("victoria" to "Plat. 1/2"),
        "west-ham" to mapOf("jubilee" to "Plat. 1/2", "district" to "Plat. 3/4", "hammersmith-city" to "Plat. 3/4"),
        "baker-street" to mapOf("jubilee" to "Plat. 1/2", "bakerloo" to "Plat. 3/4", "circle" to "Plat. 5/6", "hammersmith-city" to "Plat. 5/6", "metropolitan" to "Plat. 7/8"),
    )

    private fun getPlatformForLine(lineId: String, station: Station): String {
        return stationPlatformData[station.id]?.get(lineId) ?: ""
    }

    private fun extractPlatformFromInstruction(text: String): String? {
        if (text.isBlank()) return null
        val regex = Regex("\\bplatform\\s+([\\dA-Za-z]+)", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null
        return "Plat. ${match.groupValues[1]}"
    }

    private fun extractTowards(instruction: String?): String? {
        if (instruction.isNullOrBlank()) return null
        val regex = Regex("towards (.+?)(?:\\s+for\\s|\\s+\\(|\$)", RegexOption.IGNORE_CASE)
        val match = regex.find(instruction) ?: return null
        val towards = match.groupValues[1].trim().trimEnd('.')
        return if (towards.isNotBlank()) "Towards $towards" else null
    }

    private fun getTerminusDirection(lineId: String, from: String, to: String): String {
        val line = TubeData.getLineById(lineId) ?: return ""
        val fromIndex = line.stationIds.indexOf(from)
        val toIndex = line.stationIds.indexOf(to)
        val terminusId = if (fromIndex < 0 || toIndex < 0 || toIndex > fromIndex)
            line.stationIds.lastOrNull()
        else
            line.stationIds.firstOrNull()
        val terminusName = terminusId?.let { TubeData.getStationById(it)?.name } ?: return ""
        return "Towards $terminusName"
    }

    // ── AI Carriage Recommendation ──────────────────────────

    fun getCarriageRecommendation(station: Station, exitId: String): CarriageRecommendation? {
        val exit = station.exits.find { it.id == exitId } ?: return null
        val defaultCarriage = station.totalCarriages / 2
        val timeSaved = ((kotlin.math.abs(exit.bestCarriagePosition - defaultCarriage)) * 25)
            .coerceAtLeast(15)
        val confidence = when {
            exit.walkingTimeSeconds < 45 -> 0.92f
            exit.walkingTimeSeconds < 75 -> 0.85f
            else -> 0.78f
        }

        return CarriageRecommendation(
            carriageNumber = exit.bestCarriagePosition,
            exitName = exit.name,
            timeSavedSeconds = timeSaved,
            reason = "Board carriage ${exit.bestCarriagePosition} for quickest access to ${exit.name} (${exit.description}). " +
                "Saves ~${timeSaved}s vs random carriage. Landmarks: ${exit.nearbyLandmarks.take(2).joinToString(", ")}.",
            confidence = confidence,
        )
    }

    fun getBestExitForStation(station: Station): CarriageRecommendation? {
        val fastestExit = station.exits.minByOrNull { it.walkingTimeSeconds } ?: return null
        return getCarriageRecommendation(station, fastestExit.id)
    }

    // ── AI Crowd Prediction (ML engine) ─────────────────────

    fun predictCrowding(
        stationId: String,
        hour: Int,
        dayType: CrowdPredictionEngine.DayType = CrowdPredictionEngine.DayType.AUTO,
    ): CrowdPrediction =
        crowdEngine.predictToCrowdPrediction(stationId, hour, dayType)

    // ── AI Delay Prediction (ML engine) ───────────────────────

    fun predictDelay(lineId: String, currentStatus: LineStatus? = null) =
        delayEngine.predict(lineId, currentStatus)

    fun predictAllDelays(currentStatuses: List<LineStatus> = emptyList()) =
        delayEngine.predictAll(currentStatuses)

    // ── Enhanced AI Insights Generator ───────────────────────────────

    fun generateInsights(lineStatuses: List<LineStatus>): List<AiInsight> {
        val insights = mutableListOf<AiInsight>()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        // Disruption insights
        val disrupted = lineStatuses.filter { !it.isGoodService }
        if (disrupted.isNotEmpty()) {
            insights.add(AiInsight(
                title = "${disrupted.size} Line${if (disrupted.size > 1) "s" else ""} Disrupted",
                description = "${disrupted.joinToString(", ") { it.lineName }}: ${disrupted.first().statusDescription}. Consider alternative routes via unaffected lines.",
                type = InsightType.DELAY_WARNING,
                confidence = 0.9f,
            ))
        }

        // Peak hour crowd alert
        if (hour in 7..9 && !isWeekend) {
            insights.add(AiInsight(
                title = "Morning Rush Active",
                description = "Zone 1 stations are ~85% capacity. Oxford Circus, King's Cross, and Bank are busiest. Consider boarding from the front/rear carriages for more space.",
                type = InsightType.CROWD_ALERT,
                actionLabel = "View crowd map",
                priority = 8,
                confidence = 0.9f,
            ))
        } else if (hour in 17..19 && !isWeekend) {
            insights.add(AiInsight(
                title = "Evening Rush Hour",
                description = "Expect crowding on Victoria, Central, and Northern lines. Waterloo and London Bridge are at peak capacity. Travel after 19:30 for 40% less crowding.",
                type = InsightType.CROWD_ALERT,
                actionLabel = "Plan quieter route",
                priority = 8,
                confidence = 0.88f,
            ))
        }

        // Carriage positioning tip
        val tipStation = listOf("oxford-circus", "bank", "kings-cross", "waterloo", "victoria").random()
        val station = TubeData.getStationById(tipStation)
        if (station != null) {
            val bestExit = station.exits.minByOrNull { it.walkingTimeSeconds }
            if (bestExit != null) {
                insights.add(AiInsight(
                    title = "Carriage Tip: ${station.name}",
                    description = "Board carriage ${bestExit.bestCarriagePosition} at ${station.name} for fastest access to ${bestExit.description}. Save ~${((station.totalCarriages / 2 - bestExit.bestCarriagePosition) * 25).coerceAtLeast(15)}s exit time.",
                    type = InsightType.CARRIAGE_TIP,
                    actionLabel = "View station",
                    priority = 5,
                    confidence = 0.85f,
                ))
            }
        }

        // Time saving insight
        if (!isWeekend && hour in 10..15) {
            insights.add(AiInsight(
                title = "Off-Peak Travel Bonus",
                description = "You're travelling during off-peak hours. Trains are running every 2-4 minutes with 50% less crowding than peak. Great time for longer journeys.",
                type = InsightType.TIME_SAVING,
                priority = 3,
                confidence = 0.92f,
            ))
        }

        // Weekend insight
        if (isWeekend) {
            insights.add(AiInsight(
                title = "Weekend Service",
                description = "Some lines may have reduced service or planned engineering works. Check the Metropolitan and District lines for possible closures.",
                type = InsightType.GENERAL,
                actionLabel = "Check status",
                priority = 6,
                confidence = 0.75f,
            ))
        }

        // Network stat
        val stats = TubeData.getNetworkStats()
        insights.add(AiInsight(
            title = "Network Coverage",
            description = "${stats.totalStations} stations across ${stats.totalLines} lines. ${stats.stationsWithStepFree} stations have step-free access. The ${stats.longestLine} line is the longest at ${TubeData.getLineById(TubeData.lines.maxByOrNull { it.totalLengthKm }?.id ?: "")?.totalLengthKm ?: 0}km.",
            type = InsightType.GENERAL,
            priority = 1,
            confidence = 1.0f,
        ))

        return insights.sortedByDescending { it.priority }
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun LineStatus.toCachedEntity() = CachedLineStatusEntity(
        lineId = lineId,
        lineName = lineName,
        statusSeverity = statusSeverity,
        statusDescription = statusDescription,
        reason = reason,
    )

    private fun mapArrivals(arrivals: List<com.londontubeai.navigator.data.remote.TflArrivalResponse>): List<LiveArrival> {
        return arrivals.map { arr ->
            val lineColor = TubeData.getLineById(arr.lineId)?.color ?: TubeLineColors.Jubilee
            LiveArrival(
                lineId = arr.lineId,
                lineName = arr.lineName,
                lineColor = lineColor,
                platform = arr.platformName ?: "Unknown",
                direction = arr.direction ?: "Unknown",
                destination = arr.destinationName ?: "Unknown",
                timeToStationSeconds = arr.timeToStation,
                currentLocation = arr.currentLocation ?: "",
                expectedArrival = arr.expectedArrival ?: "",
            )
        }.sortedBy { it.timeToStationSeconds }
    }

    private fun resolveStationFromPoint(
        point: com.londontubeai.navigator.data.remote.TflPoint?,
        fallback: Station,
    ): Station {
        if (point == null) return fallback

        point.naptanId?.let { naptan ->
            val stationId = NaptanIds.stationIdForNaptan(naptan)
            val station = stationId?.let { TubeData.getStationById(it) }
            if (station != null) return station
        }

        val lat = point.lat
        val lon = point.lon
        if (lat != null && lon != null) {
            val nearest = TubeData.getNearbyStations(lat, lon, radiusKm = 0.6, limit = 1)
                .firstOrNull()?.station
            if (nearest != null) return nearest

            return Station(
                id = "dynamic-${point.naptanId ?: fallback.id}",
                name = point.commonName ?: fallback.name,
                lineIds = fallback.lineIds,
                zone = fallback.zone,
                latitude = lat,
                longitude = lon,
            )
        }

        return fallback
    }

    // ── Data Validation ──────────────────────────────────────

    private fun validateLineStatusResponse(line: com.londontubeai.navigator.data.remote.TflLineStatusResponse): com.londontubeai.navigator.data.remote.TflLineStatusResponse? {
        // Check required fields
        if (line.id.isBlank()) return null
        if (line.name.isBlank()) return null
        if (line.lineStatuses.isEmpty()) return null
        
        // Validate line ID format (should be like "bakerloo", "central", etc.)
        if (!line.id.matches(Regex("^[a-z-]+$"))) return null
        
        // Validate status severity range (0-20 typically)
        val status = line.lineStatuses.firstOrNull()
        if (status != null && (status.statusSeverity < 0 || status.statusSeverity > 20)) return null
        
        return line
    }
    
    private fun validateArrivalResponse(arrival: com.londontubeai.navigator.data.remote.TflArrivalResponse): com.londontubeai.navigator.data.remote.TflArrivalResponse? {
        // Check required fields
        if (arrival.lineId.isBlank()) return null
        if (arrival.timeToStation < 0) return null
        if (arrival.expectedArrival?.isBlank() != false) return null
        
        // Validate time to station (should be reasonable)
        if (arrival.timeToStation > 3600) return null // More than 1 hour seems wrong
        
        return arrival
    }
    
    // ── Status Screen Features ───────────────────────────────
    
    suspend fun getUserProfile(): com.londontubeai.navigator.ui.screens.status.UserProfile {
        // Load user preferences from database
        val prefs = dao.getUserPreferences().first()
        
        return com.londontubeai.navigator.ui.screens.status.UserProfile(
            favoriteLines = prefs?.favoriteLines?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            frequentRoutes = prefs?.frequentRoutes?.split(";")?.filter { it.isNotBlank() } ?: emptyList(),
            preferredStations = prefs?.preferredStations?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
        )
    }
    
    suspend fun saveUserProfile(profile: com.londontubeai.navigator.ui.screens.status.UserProfile) {
        val entity = UserPreferencesEntity(
            id = 1,
            favoriteLines = profile.favoriteLines.joinToString(","),
            frequentRoutes = profile.frequentRoutes.joinToString(";"),
            preferredStations = profile.preferredStations.joinToString(","),
        )
        dao.saveUserPreferences(entity)
    }
    
    suspend fun getNetworkAnalytics(): com.londontubeai.navigator.ui.screens.status.NetworkAnalytics {
        val cachedStatuses = dao.getCachedLineStatuses().first()
        
        val goodCount = cachedStatuses.count { it.statusSeverity >= 10 }
        val totalCount = cachedStatuses.size.coerceAtLeast(1)
        val reliabilityScore = (goodCount * 100f / totalCount)
        
        return com.londontubeai.navigator.ui.screens.status.NetworkAnalytics(
            reliabilityScore = reliabilityScore,
            averageDisruptionTime = 15, // Default estimate
            peakDisruptionHours = listOf(8, 9, 17, 18), // Rush hours
            goodServicePercentage = reliabilityScore,
            totalDisruptions = totalCount - goodCount,
            lastUpdated = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        )
    }
    
    fun predictDisruptionProbability(lineId: String): Float {
        // Simple prediction based on historical data
        // In a real app, this would use ML model
        val line = TubeData.getLineById(lineId)
        return when {
            line == null -> 0.1f
            lineId in listOf("northern", "central", "piccadilly") -> 0.25f // Busier lines
            lineId in listOf("circle", "district") -> 0.20f
            else -> 0.15f
        }
    }
}
