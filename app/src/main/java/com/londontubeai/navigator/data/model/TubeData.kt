package com.londontubeai.navigator.data.model

import com.londontubeai.navigator.ui.theme.TubeLineColors

object TubeData {

    val lines: List<TubeLine> = listOf(
        TubeLine("bakerloo", "Bakerloo", TubeLineColors.Bakerloo,
            listOf("elephant-castle","lambeth-north","waterloo","embankment","charing-cross","piccadilly-circus","oxford-circus","regents-park","baker-street","marylebone","edgware-road-bakerloo","paddington","warwick-avenue","maida-vale","kilburn-park","queens-park","kensal-green","harlesden","stonebridge-park","wembley-central","north-wembley","south-kenton","kenton","harrow-wealdstone"),
            averageSpeedKmh = 33, peakFrequencyMinutes = 3, offPeakFrequencyMinutes = 5, totalLengthKm = 36.2),
        // Central line — runs Ealing Broadway / West Ruislip in the west to
        // Epping / Hainault loop in the east. We expose two key branches so
        // the Status screen can show each terminal properly.
        TubeLine("central", "Central", TubeLineColors.Central,
            listOf("ealing-broadway","north-acton","white-city","shepherds-bush","holland-park","notting-hill-gate","queensway","lancaster-gate","marble-arch","bond-street","oxford-circus","tottenham-court-road","holborn","chancery-lane","st-pauls","bank","liverpool-street","bethnal-green","mile-end","stratford","leyton","leytonstone"),
            averageSpeedKmh = 34, peakFrequencyMinutes = 2, offPeakFrequencyMinutes = 4, totalLengthKm = 74.0,
            branches = listOf(
                TubeLineBranch(
                    "central-main", "Ealing Broadway → Leytonstone",
                    listOf("ealing-broadway","north-acton","white-city","shepherds-bush","holland-park","notting-hill-gate","queensway","lancaster-gate","marble-arch","bond-street","oxford-circus","tottenham-court-road","holborn","chancery-lane","st-pauls","bank","liverpool-street","bethnal-green","mile-end","stratford","leyton","leytonstone"),
                ),
            ),
        ),
        TubeLine("circle", "Circle", TubeLineColors.Circle,
            listOf("edgware-road","baker-street","great-portland-street","euston-square","kings-cross","farringdon","barbican","moorgate","liverpool-street","aldgate","tower-hill","monument","cannon-street","mansion-house","blackfriars","temple","embankment","westminster","st-james-park","victoria","sloane-square","south-kensington","gloucester-road","high-street-kensington","notting-hill-gate","bayswater","paddington","edgware-road"),
            averageSpeedKmh = 33, peakFrequencyMinutes = 3, offPeakFrequencyMinutes = 7, totalLengthKm = 27.2),
        TubeLine("district", "District", TubeLineColors.District,
            listOf("ealing-broadway","acton-town","hammersmith","barons-court","earls-court","gloucester-road","south-kensington","sloane-square","victoria","st-james-park","westminster","embankment","temple","blackfriars","mansion-house","cannon-street","monument","tower-hill","aldgate-east","whitechapel","mile-end","west-ham","barking"),
            averageSpeedKmh = 33, peakFrequencyMinutes = 2, offPeakFrequencyMinutes = 5, totalLengthKm = 64.0),
        TubeLine("hammersmith-city", "Hammersmith & City", TubeLineColors.HammersmithCity,
            listOf("hammersmith","shepherds-bush-market","wood-lane","ladbroke-grove","westbourne-park","paddington","edgware-road","baker-street","great-portland-street","euston-square","kings-cross","farringdon","barbican","moorgate","liverpool-street","aldgate-east","whitechapel","mile-end","west-ham","barking"),
            averageSpeedKmh = 33, peakFrequencyMinutes = 3, offPeakFrequencyMinutes = 7, totalLengthKm = 25.5),
        TubeLine("jubilee", "Jubilee", TubeLineColors.Jubilee,
            listOf("stanmore","canons-park","wembley-park","finchley-road","swiss-cottage","st-johns-wood","baker-street","bond-street","green-park","westminster","waterloo","southwark","london-bridge","bermondsey","canada-water","canary-wharf","north-greenwich","canning-town","west-ham","stratford"),
            averageSpeedKmh = 36, peakFrequencyMinutes = 2, offPeakFrequencyMinutes = 4, totalLengthKm = 36.2),
        // Metropolitan — expose the northern branch split so users can see
        // both Amersham and Chesham terminals (they share track until
        // Chalfont & Latimer). Using stations present in our dataset only.
        TubeLine("metropolitan", "Metropolitan", TubeLineColors.Metropolitan,
            listOf("aldgate","liverpool-street","moorgate","barbican","farringdon","kings-cross","euston-square","great-portland-street","baker-street","finchley-road","wembley-park","harrow-on-the-hill","amersham","chesham"),
            averageSpeedKmh = 41, peakFrequencyMinutes = 3, offPeakFrequencyMinutes = 6, totalLengthKm = 66.7,
            branches = listOf(
                TubeLineBranch(
                    "met-amersham", "Aldgate → Amersham",
                    listOf("aldgate","liverpool-street","moorgate","barbican","farringdon","kings-cross","euston-square","great-portland-street","baker-street","finchley-road","wembley-park","harrow-on-the-hill","amersham"),
                ),
                TubeLineBranch(
                    "met-chesham", "Aldgate → Chesham",
                    listOf("aldgate","liverpool-street","moorgate","barbican","farringdon","kings-cross","euston-square","great-portland-street","baker-street","finchley-road","wembley-park","harrow-on-the-hill","chesham"),
                ),
            ),
        ),
        // Northern — the previous flat list had duplicates because it crams
        // 5 route segments (2 central branches + 2 northern branches + south
        // section) into one sequence. We now express the two complete
        // end-to-end services as proper branches, and the flat `stationIds`
        // is a deduplicated union used by the graph + map.
        TubeLine("northern", "Northern", TubeLineColors.Northern,
            listOf("morden","south-wimbledon","colliers-wood","tooting-broadway","tooting-bec","balham","clapham-south","clapham-common","clapham-north","stockwell","oval","kennington","elephant-castle","borough","london-bridge","bank","moorgate","old-street","angel","kings-cross","euston","warren-street","goodge-street","tottenham-court-road","leicester-square","charing-cross","embankment","waterloo","camden-town","mornington-crescent","kentish-town","tufnell-park","archway","highgate","east-finchley","finchley-central","high-barnet","chalk-farm","hampstead","golders-green","edgware"),
            averageSpeedKmh = 33, peakFrequencyMinutes = 2, offPeakFrequencyMinutes = 4, totalLengthKm = 58.2,
            branches = listOf(
                TubeLineBranch(
                    "north-hb-bank", "Morden → High Barnet (via Bank)",
                    listOf("morden","south-wimbledon","colliers-wood","tooting-broadway","tooting-bec","balham","clapham-south","clapham-common","clapham-north","stockwell","oval","kennington","elephant-castle","borough","london-bridge","bank","moorgate","old-street","angel","kings-cross","euston","camden-town","kentish-town","tufnell-park","archway","highgate","east-finchley","finchley-central","high-barnet"),
                ),
                TubeLineBranch(
                    "north-edg-charing", "Morden → Edgware (via Charing Cross)",
                    listOf("morden","south-wimbledon","colliers-wood","tooting-broadway","tooting-bec","balham","clapham-south","clapham-common","clapham-north","stockwell","oval","kennington","waterloo","embankment","charing-cross","leicester-square","tottenham-court-road","goodge-street","warren-street","euston","mornington-crescent","camden-town","chalk-farm","hampstead","golders-green","edgware"),
                ),
            ),
        ),
        // Piccadilly — westbound splits at Acton Town into two long branches:
        // Uxbridge (via Rayners Lane) and Heathrow (T5 / T2-3).
        TubeLine("piccadilly", "Piccadilly", TubeLineColors.Piccadilly,
            listOf("cockfosters","oakwood","southgate","arnos-grove","bounds-green","wood-green","turnpike-lane","manor-house","finsbury-park","arsenal","holloway-road","caledonian-road","kings-cross","russell-square","holborn","covent-garden","leicester-square","piccadilly-circus","green-park","hyde-park-corner","knightsbridge","south-kensington","gloucester-road","earls-court","barons-court","hammersmith","acton-town","rayners-lane","eastcote","ruislip-manor","ruislip","ickenham","hillingdon","uxbridge","heathrow-t123","heathrow-t5"),
            averageSpeedKmh = 33, peakFrequencyMinutes = 2, offPeakFrequencyMinutes = 5, totalLengthKm = 71.0,
            branches = listOf(
                TubeLineBranch(
                    "picc-uxbridge", "Cockfosters → Uxbridge",
                    listOf("cockfosters","oakwood","southgate","arnos-grove","bounds-green","wood-green","turnpike-lane","manor-house","finsbury-park","arsenal","holloway-road","caledonian-road","kings-cross","russell-square","holborn","covent-garden","leicester-square","piccadilly-circus","green-park","hyde-park-corner","knightsbridge","south-kensington","gloucester-road","earls-court","barons-court","hammersmith","acton-town","rayners-lane","eastcote","ruislip-manor","ruislip","ickenham","hillingdon","uxbridge"),
                ),
                TubeLineBranch(
                    "picc-heathrow", "Cockfosters → Heathrow Terminal 5",
                    listOf("cockfosters","oakwood","southgate","arnos-grove","bounds-green","wood-green","turnpike-lane","manor-house","finsbury-park","arsenal","holloway-road","caledonian-road","kings-cross","russell-square","holborn","covent-garden","leicester-square","piccadilly-circus","green-park","hyde-park-corner","knightsbridge","south-kensington","gloucester-road","earls-court","barons-court","hammersmith","acton-town","heathrow-t123","heathrow-t5"),
                ),
            ),
        ),
        TubeLine("victoria", "Victoria", TubeLineColors.Victoria,
            listOf("brixton","stockwell","vauxhall","pimlico","victoria","green-park","oxford-circus","warren-street","euston","kings-cross","highbury-islington","finsbury-park","seven-sisters","tottenham-hale","blackhorse-road","walthamstow-central"),
            averageSpeedKmh = 40, peakFrequencyMinutes = 2, offPeakFrequencyMinutes = 4, totalLengthKm = 21.0),
        TubeLine("waterloo-city", "Waterloo & City", TubeLineColors.WaterlooCity,
            listOf("waterloo","bank"),
            averageSpeedKmh = 40, peakFrequencyMinutes = 4, offPeakFrequencyMinutes = 4, totalLengthKm = 2.5),
        TubeLine("elizabeth", "Elizabeth", TubeLineColors.Elizabeth,
            listOf("heathrow-t5","heathrow-t123","ealing-broadway","paddington","bond-street","tottenham-court-road","farringdon","liverpool-street","whitechapel","canary-wharf","woolwich","abbey-wood"),
            averageSpeedKmh = 50, peakFrequencyMinutes = 3, offPeakFrequencyMinutes = 5, totalLengthKm = 118.0),
    )

    val stations: Map<String, Station> = buildStations()

    val connections: List<StationConnection> by lazy { buildConnectionGraph() }

    val adjacencyMap: Map<String, List<StationConnection>> by lazy {
        connections.flatMap { c -> listOf(c, c.copy(fromStationId = c.toStationId, toStationId = c.fromStationId)) }
            .groupBy { it.fromStationId }
    }

    fun getStationById(id: String): Station? = stations[id]
    fun getLineById(id: String): TubeLine? = lines.find { it.id == id }
    fun getLinesForStation(stationId: String): List<TubeLine> {
        val station = stations[stationId] ?: return emptyList()
        return station.lineIds.mapNotNull { lineId -> lines.find { it.id == lineId } }
    }
    fun searchStations(query: String): List<Station> {
        if (query.isBlank()) return stations.values.toList().sortedBy { it.name }
        val q = query.lowercase().trim()
        return stations.values.filter { it.name.lowercase().contains(q) }.sortedBy { it.name }
    }
    fun getAllStationsSorted(): List<Station> = stations.values.sortedBy { it.name }
    fun getNeighbours(stationId: String): List<StationConnection> = adjacencyMap[stationId] ?: emptyList()

    /**
     * Returns rich detail about a tube line: route endpoints, station names,
     * connecting lines, and key interchange stations.
     */
    fun getLineDetail(lineId: String): LineDetail? {
        val line = getLineById(lineId) ?: return null
        val stationNames = line.stationIds.mapNotNull { stations[it]?.name }
        val firstStation = stationNames.firstOrNull() ?: return null
        val lastStation = stationNames.lastOrNull() ?: return null

        // Find all other lines that share at least one station with this line
        val interchangeStations = mutableListOf<InterchangeInfo>()
        val connectingLineIds = mutableSetOf<String>()

        for (stationId in line.stationIds) {
            val station = stations[stationId] ?: continue
            val otherLines = station.lineIds.filter { it != lineId }
            if (otherLines.isNotEmpty()) {
                connectingLineIds.addAll(otherLines)
                interchangeStations.add(
                    InterchangeInfo(
                        stationName = station.name,
                        stationId = stationId,
                        connectingLineNames = otherLines.mapNotNull { getLineById(it)?.name },
                        hasStepFreeAccess = station.hasStepFreeAccess,
                        zone = station.zone,
                    )
                )
            }
        }

        val connectingLines = connectingLineIds.mapNotNull { id ->
            getLineById(id)?.let { ConnectingLine(it.id, it.name, it.color) }
        }.sortedBy { it.name }

        val orderedStations = line.stationIds.mapIndexedNotNull { index, stationId ->
            val station = stations[stationId] ?: return@mapIndexedNotNull null
            val otherLines = station.lineIds
                .filter { it != lineId }
                .mapNotNull { getLineById(it)?.name }
                .sorted()
            LineStationStop(
                stationId = stationId,
                stationName = station.name,
                zone = station.zone,
                isTerminal = index == 0 || index == line.stationIds.lastIndex,
                hasStepFreeAccess = station.hasStepFreeAccess,
                connectingLineNames = otherLines,
            )
        }

        // Build rich per-branch detail when the line declares multiple routes
        // (Northern, Piccadilly, Metropolitan, Central today). Each branch
        // reuses the same station-enrichment logic so terminals and step-free
        // flags are accurate for that specific route.
        val branchDetails: List<LineBranchDetail> = line.branches.map { branch ->
            val branchStops = branch.stationIds.mapIndexedNotNull { index, stationId ->
                val station = stations[stationId] ?: return@mapIndexedNotNull null
                val otherLines = station.lineIds
                    .filter { it != lineId }
                    .mapNotNull { getLineById(it)?.name }
                    .sorted()
                LineStationStop(
                    stationId = stationId,
                    stationName = station.name,
                    zone = station.zone,
                    isTerminal = index == 0 || index == branch.stationIds.lastIndex,
                    hasStepFreeAccess = station.hasStepFreeAccess,
                    connectingLineNames = otherLines,
                )
            }
            LineBranchDetail(
                branchId = branch.id,
                branchName = branch.name,
                orderedStations = branchStops,
            )
        }

        val stepFreeCount = orderedStations.count { it.hasStepFreeAccess }

        return LineDetail(
            lineId = lineId,
            lineName = line.name,
            lineColor = line.color,
            firstStation = firstStation,
            lastStation = lastStation,
            stationCount = line.stationIds.size,
            totalLengthKm = line.totalLengthKm,
            averageSpeedKmh = line.averageSpeedKmh,
            peakFrequencyMin = line.peakFrequencyMinutes,
            offPeakFrequencyMin = line.offPeakFrequencyMinutes,
            firstTrain = line.firstTrain,
            lastTrain = line.lastTrain,
            connectingLines = connectingLines,
            interchanges = interchangeStations,
            orderedStations = orderedStations,
            stationNames = stationNames,
            branches = branchDetails,
            stepFreeStationCount = stepFreeCount,
        )
    }

    /** All unique zones found across stations, sorted. */
    val allZones: List<String> by lazy {
        stations.values.map { it.zone }.distinct().sortedBy {
            it.replace("-", ".").split(".").first().toIntOrNull() ?: 99
        }
    }

    /** Stations filtered by zone. */
    fun getStationsByZone(zone: String): List<Station> =
        stations.values.filter { it.zone == zone }.sortedBy { it.name }

    /** Stations that serve a given line. */
    fun getStationsForLine(lineId: String): List<Station> =
        stations.values.filter { lineId in it.lineIds }.sortedBy { it.name }

    /** Step-free only stations. */
    fun getStepFreeStations(): List<Station> =
        stations.values.filter { it.hasStepFreeAccess }.sortedBy { it.name }

    /**
     * Returns stations near a given lat/lng, sorted by distance.
     * Uses Haversine formula. Returns up to [limit] results within [radiusKm].
     */
    fun getNearbyStations(
        lat: Double, lng: Double,
        radiusKm: Double = 2.0, limit: Int = 10,
    ): List<NearbyStation> {
        return stations.values.mapNotNull { station ->
            val dist = haversineKm(lat, lng, station.latitude, station.longitude)
            if (dist <= radiusKm) NearbyStation(station, dist) else null
        }.sortedBy { it.distanceKm }.take(limit)
    }

    /**
     * Returns stations reachable from [stationId] on the same line(s) within [maxStops] stops.
     */
    fun getConnectingStations(stationId: String, maxStops: Int = 3): List<ConnectingStation> {
        val result = mutableListOf<ConnectingStation>()
        val station = stations[stationId] ?: return result
        for (lineId in station.lineIds) {
            val line = getLineById(lineId) ?: continue
            val idx = line.stationIds.indexOf(stationId)
            if (idx < 0) continue
            // forward direction
            for (i in 1..maxStops) {
                val nextIdx = idx + i
                if (nextIdx >= line.stationIds.size) break
                val s = stations[line.stationIds[nextIdx]] ?: continue
                result.add(ConnectingStation(s, line.name, line.color, i))
            }
            // backward direction
            for (i in 1..maxStops) {
                val prevIdx = idx - i
                if (prevIdx < 0) break
                val s = stations[line.stationIds[prevIdx]] ?: continue
                result.add(ConnectingStation(s, line.name, line.color, i))
            }
        }
        return result.distinctBy { it.station.id }.sortedBy { it.stops }
    }

    // ── Haversine ────────────────────────────────────────────
    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    /** Search by landmark — matches exit nearbyLandmarks and streetName. */
    fun searchByLandmark(query: String): List<Station> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase().trim()
        return stations.values.filter { station ->
            station.exits.any { exit ->
                exit.nearbyLandmarks.any { it.lowercase().contains(q) } ||
                    exit.streetName.lowercase().contains(q)
            }
        }.sortedBy { it.name }
    }

    /** Advanced search: matches name, zone, line name, landmarks, facilities. */
    fun advancedSearch(query: String): List<Station> {
        if (query.isBlank()) return getAllStationsSorted()
        val q = query.lowercase().trim()
        return stations.values.filter { station ->
            station.name.lowercase().contains(q) ||
                station.zone.contains(q) ||
                station.lineIds.any { getLineById(it)?.name?.lowercase()?.contains(q) == true } ||
                station.exits.any { exit ->
                    exit.nearbyLandmarks.any { it.lowercase().contains(q) } ||
                        exit.streetName.lowercase().contains(q)
                } ||
                station.facilities.any { it.label.lowercase().contains(q) }
        }.sortedBy {
            // Prioritize name matches
            if (it.name.lowercase().contains(q)) 0 else 1
        }
    }

    /** Generate crowd heatmap for a station across 24h. */
    fun generateCrowdHeatmap(stationId: String, peakMultiplier: Float = 1.0f): List<CrowdHeatmapEntry> {
        return (0..23).map { hour ->
            val basePct = when (hour) {
                in 0..4 -> 5
                5 -> 15
                6 -> 35
                7 -> 65
                8 -> 90
                9 -> 75
                in 10..11 -> 45
                in 12..13 -> 55
                in 14..15 -> 50
                16 -> 60
                17 -> 85
                18 -> 80
                19 -> 55
                20 -> 35
                21 -> 25
                22 -> 15
                23 -> 10
                else -> 20
            }
            val adjusted = (basePct * peakMultiplier).toInt().coerceIn(0, 100)
            val level = when {
                adjusted < 25 -> CrowdLevel.LOW
                adjusted < 50 -> CrowdLevel.MODERATE
                adjusted < 70 -> CrowdLevel.HIGH
                adjusted < 85 -> CrowdLevel.VERY_HIGH
                else -> CrowdLevel.EXTREME
            }
            CrowdHeatmapEntry(hour, level, adjusted)
        }
    }

    /** Generate journey suggestions from a station. */
    fun getJourneySuggestions(stationId: String): List<JourneySuggestion> {
        val station = stations[stationId] ?: return emptyList()
        val suggestions = mutableListOf<JourneySuggestion>()

        // Popular destinations (major stations on same lines)
        val majorStationIds = listOf("waterloo", "kings-cross", "oxford-circus", "bank", "victoria",
            "liverpool-street", "paddington", "london-bridge", "canary-wharf", "stratford")
        for (destId in majorStationIds) {
            if (destId == stationId) continue
            val dest = stations[destId] ?: continue
            val sharedLine = station.lineIds.intersect(dest.lineIds.toSet()).isNotEmpty()
            val interchanges = if (sharedLine) 0 else 1
            val est = if (sharedLine) (3..12).random() else (10..25).random()
            suggestions.add(JourneySuggestion(dest, est, interchanges, "Major hub", SuggestionCategory.POPULAR))
        }

        // Quick routes (same-line destinations within 5 stops)
        val connecting = getConnectingStations(stationId, maxStops = 5)
        for (cs in connecting.take(4)) {
            suggestions.add(JourneySuggestion(cs.station, cs.stops * 2, 0,
                "Direct on ${cs.lineName}", SuggestionCategory.QUICK))
        }

        return suggestions.distinctBy { it.destinationStation.id }.take(12)
    }

    /** Generate community tips for a station. */
    fun generateCommunityTips(stationId: String): List<CommunityTip> {
        val station = stations[stationId] ?: return emptyList()
        val tips = mutableListOf<CommunityTip>()

        if (station.lineIds.size >= 3) {
            tips.add(CommunityTip("t1-$stationId", stationId,
                "Follow signs carefully — interchange between lines can take ${station.interchangeTimeMinutes} min during rush hour",
                "TubeExpert", 42, TipCategory.PLATFORM_TIP))
        }
        val fastestExit = station.exits.minByOrNull { it.walkingTimeSeconds }
        if (fastestExit != null) {
            tips.add(CommunityTip("t2-$stationId", stationId,
                "${fastestExit.name} is the fastest exit (${fastestExit.walkingTimeSeconds}s). Board carriage ${fastestExit.bestCarriagePosition} for best access.",
                "DailyCommuter", 38, TipCategory.EXIT_TIP))
        }
        if (station.peakCrowdMultiplier > 1.2f) {
            tips.add(CommunityTip("t3-$stationId", stationId,
                "Very crowded 8-9am. Try arriving before 7:45 or after 9:15 for a much quieter experience.",
                "MorningRider", 56, TipCategory.CROWD_AVOIDANCE))
        }
        if (station.hasStepFreeAccess) {
            tips.add(CommunityTip("t4-$stationId", stationId,
                "Step-free access available — lifts are near the main entrance. Allow extra time during peak hours.",
                "AccessHelper", 29, TipCategory.ACCESSIBILITY))
        }
        if (station.facilities.contains(StationFacility.NATIONAL_RAIL)) {
            tips.add(CommunityTip("t5-$stationId", stationId,
                "National Rail interchange available. Follow overhead signs — it's about a 3-min walk to mainline platforms.",
                "RailFan", 33, TipCategory.GENERAL))
        }
        tips.add(CommunityTip("t6-$stationId", stationId,
            "The rear carriages are usually less crowded. Great for getting a seat on longer journeys.",
            "LocalTraveller", 21, TipCategory.CROWD_AVOIDANCE))

        return tips
    }

    /** Generate station reviews (simulated). */
    fun generateStationReviews(stationId: String): List<StationReview> {
        val station = stations[stationId] ?: return emptyList()
        val baseRating = when {
            station.hasStepFreeAccess && station.facilities.size >= 4 -> 4.2f
            station.hasStepFreeAccess -> 3.8f
            station.facilities.size >= 3 -> 3.5f
            else -> 3.2f
        }
        return listOf(
            StationReview("r1-$stationId", stationId, "Alex M.", (baseRating + 0.3f).coerceIn(1.0f, 5.0f),
                "Clean and well-maintained. Good signage for interchanges.", helpful = 12,
                tags = listOf("Clean", "Well signed")),
            StationReview("r2-$stationId", stationId, "Sarah K.", baseRating.coerceIn(1.0f, 5.0f),
                "Gets very busy during rush hour but manageable. WiFi works well.", helpful = 8,
                tags = listOf("Busy peak", "Good WiFi")),
            StationReview("r3-$stationId", stationId, "James W.", (baseRating - 0.2f).coerceIn(1.0f, 5.0f),
                if (station.hasStepFreeAccess) "Great step-free access, lifts always working." else "Could use better accessibility options.",
                helpful = 15, tags = if (station.hasStepFreeAccess) listOf("Accessible") else listOf("Needs improvement")),
            StationReview("r4-$stationId", stationId, "Priya R.", (baseRating + 0.1f).coerceIn(1.0f, 5.0f),
                "Convenient location with good bus connections nearby.", helpful = 6,
                tags = listOf("Good connections")),
        )
    }

    /** Generate contextual insights for a station. */
    fun generateStationInsights(stationId: String, hour: Int): List<StationInsight> {
        val station = stations[stationId] ?: return emptyList()
        val insights = mutableListOf<StationInsight>()

        // Time-based insight
        when (hour) {
            in 7..9 -> insights.add(StationInsight("Morning Rush", "Currently peak hours — expect ${(station.peakCrowdMultiplier * 80).toInt()}% capacity. Try rear carriages for more space.", "schedule", StationInsightType.WARNING))
            in 17..19 -> insights.add(StationInsight("Evening Rush", "Peak evening hours. Platform crowding expected. Consider travelling after 19:30.", "schedule", StationInsightType.WARNING))
            in 10..16 -> insights.add(StationInsight("Off-Peak", "Off-peak hours — trains every ${station.lineIds.firstOrNull()?.let { getLineById(it)?.offPeakFrequencyMinutes } ?: 5} min. Great time to travel!", "thumb_up", StationInsightType.TIP))
            in 22..23, in 0..5 -> insights.add(StationInsight("Late Night", "Limited service. Check last train times before travelling.", "nightlight", StationInsightType.WARNING))
        }

        // Station-specific insights
        if (station.annualPassengers > 50) {
            val passengerText = if (station.annualPassengers >= 100) "${station.annualPassengers.toInt()}M" else "${String.format("%.1f", station.annualPassengers)}M"
            insights.add(StationInsight("Busy Station", "$passengerText passengers/year — one of London's busiest. Allow extra time.", "trending_up", StationInsightType.INFO))
        }
        if (station.lineIds.size >= 3) {
            insights.add(StationInsight("Major Interchange", "${station.lineIds.size} lines converge here. Interchange takes ~${station.interchangeTimeMinutes} min. Follow colour-coded signs.", "swap_horiz", StationInsightType.TIP))
        }
        if (station.exits.size >= 3) {
            val fastest = station.exits.minByOrNull { it.walkingTimeSeconds }
            insights.add(StationInsight("Multiple Exits", "${station.exits.size} exits available. ${fastest?.name} is fastest at ${fastest?.walkingTimeSeconds}s.", "exit_to_app", StationInsightType.TIP))
        }

        return insights
    }

    /** Get platform info for a station. */
    fun getPlatformInfo(stationId: String): List<PlatformInfo> {
        val station = stations[stationId] ?: return emptyList()
        return station.lineIds.flatMapIndexed { index, lineId ->
            val line = getLineById(lineId) ?: return@flatMapIndexed emptyList()
            val stIdx = line.stationIds.indexOf(stationId)
            val directions = mutableListOf<PlatformInfo>()
            if (stIdx > 0) {
                val towards = stations[line.stationIds.last()]?.name ?: "Unknown"
                directions.add(PlatformInfo(lineId, line.name, line.color, "Platform ${index * 2 + 1}", "Towards $towards"))
            }
            if (stIdx < line.stationIds.size - 1) {
                val towards = stations[line.stationIds.first()]?.name ?: "Unknown"
                directions.add(PlatformInfo(lineId, line.name, line.color, "Platform ${index * 2 + 2}", "Towards $towards"))
            }
            directions
        }
    }

    fun getNetworkStats(): NetworkStats = NetworkStats(
        totalStations = stations.size,
        totalLines = lines.size,
        totalConnectionsKm = lines.sumOf { it.totalLengthKm },
        stationsWithStepFree = stations.values.count { it.hasStepFreeAccess },
        busiestStation = stations.values.maxByOrNull { it.annualPassengers }?.name ?: "Waterloo",
        longestLine = lines.maxByOrNull { it.totalLengthKm }?.name ?: "Elizabeth",
    )

    // ── Station builder ──────────────────────────────────────────

    private fun buildStations(): Map<String, Station> {
        val map = mutableMapOf<String, Station>()
        fun s(id: String, name: String, lineIds: List<String>, zone: String, lat: Double, lng: Double,
               stepFree: Boolean = false, carriages: Int = 8, interchangeMin: Int = 0,
               annualM: Double = 0.0, crowdMult: Float = 1.0f,
               facilities: Set<StationFacility> = emptySet(), toilets: Boolean = false,
               exits: List<StationExit> = emptyList()) {
            map[id] = Station(id, name, lineIds, zone, lat, lng, exits, stepFree, carriages, facilities, interchangeMin, annualM, true, toilets, crowdMult)
        }

        // ── Zone 1 Major Interchanges ──────────────────────────
        s("oxford-circus", "Oxford Circus", listOf("bakerloo","central","victoria"), "1", 51.5152, -0.1415,
            interchangeMin = 4, annualM = 98.0, crowdMult = 1.5f,
            facilities = setOf(StationFacility.WIFI, StationFacility.ESCALATORS, StationFacility.OYSTER_VALIDATOR),
            exits = listOf(
                StationExit("ox-1", "Exit 1", "Argyll Street / Oxford Street North", 2, 45, nearbyLandmarks = listOf("Liberty London", "Palladium Theatre"), streetName = "Argyll Street"),
                StationExit("ox-2", "Exit 2", "Oxford Street East", 5, 30, nearbyLandmarks = listOf("John Lewis", "BBC Broadcasting House"), streetName = "Oxford Street"),
                StationExit("ox-3", "Exit 3", "Regent Street South", 6, 60, nearbyLandmarks = listOf("Apple Store Regent St", "Hamleys"), streetName = "Regent Street"),
                StationExit("ox-4", "Exit 4", "Oxford Street West", 8, 40, nearbyLandmarks = listOf("Selfridges", "Marble Arch"), streetName = "Oxford Street")))

        s("kings-cross", "King's Cross St Pancras", listOf("victoria","piccadilly","northern","metropolitan","hammersmith-city","circle"), "1", 51.5308, -0.1238,
            stepFree = true, interchangeMin = 5, annualM = 93.0, crowdMult = 1.4f,
            facilities = setOf(StationFacility.WIFI, StationFacility.TOILETS, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.TAXI_RANK, StationFacility.NATIONAL_RAIL, StationFacility.BIKE_PARKING),
            toilets = true,
            exits = listOf(
                StationExit("kx-a", "Exit A", "King's Cross Mainline", 1, 90, isStepFree = true, nearbyLandmarks = listOf("King's Cross Station", "Platform 9¾"), streetName = "Euston Road"),
                StationExit("kx-b", "Exit B", "St Pancras International", 4, 120, isStepFree = true, nearbyLandmarks = listOf("Eurostar Terminal", "British Library"), streetName = "Pancras Road"),
                StationExit("kx-c", "Exit C", "Euston Road South", 6, 60, nearbyLandmarks = listOf("Google London", "The Guardian"), streetName = "Euston Road"),
                StationExit("kx-d", "Exit D", "York Way North", 8, 75, nearbyLandmarks = listOf("Coal Drops Yard", "Granary Square"), streetName = "York Way")))

        s("bank", "Bank", listOf("central","northern","waterloo-city"), "1", 51.5133, -0.0886,
            stepFree = true, interchangeMin = 6, annualM = 67.0, crowdMult = 1.4f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.CASH_MACHINE),
            exits = listOf(
                StationExit("bk-1", "Exit 1", "Threadneedle Street", 3, 90, nearbyLandmarks = listOf("Bank of England", "Royal Exchange"), streetName = "Threadneedle St"),
                StationExit("bk-2", "Exit 2", "Poultry / Cheapside", 5, 75, nearbyLandmarks = listOf("One New Change", "St Paul's Cathedral"), streetName = "Poultry"),
                StationExit("bk-3", "Exit 3", "King William Street", 7, 60, nearbyLandmarks = listOf("Monument", "London Bridge"), streetName = "King William St"),
                StationExit("bk-4", "Exit 4", "Walbrook (Step-free)", 1, 120, isStepFree = true, nearbyLandmarks = listOf("Bloomberg HQ", "Cannon Street Station"), streetName = "Walbrook")))

        s("green-park", "Green Park", listOf("victoria","piccadilly","jubilee"), "1", 51.5067, -0.1428,
            stepFree = true, interchangeMin = 3, annualM = 46.0, crowdMult = 1.2f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS),
            exits = listOf(
                StationExit("gp-a", "Exit A", "Piccadilly North", 2, 45, nearbyLandmarks = listOf("The Ritz London", "Fortnum & Mason"), streetName = "Piccadilly"),
                StationExit("gp-b", "Exit B", "Piccadilly South", 5, 50, isStepFree = true, nearbyLandmarks = listOf("Green Park", "Buckingham Palace"), streetName = "Piccadilly"),
                StationExit("gp-c", "Exit C", "Stratton Street", 7, 70, nearbyLandmarks = listOf("Berkeley Square", "Mayfair"), streetName = "Stratton Street")))

        s("waterloo", "Waterloo", listOf("bakerloo","jubilee","northern","waterloo-city"), "1", 51.5036, -0.1143,
            stepFree = true, interchangeMin = 4, annualM = 100.5, crowdMult = 1.5f,
            facilities = setOf(StationFacility.WIFI, StationFacility.TOILETS, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.TAXI_RANK, StationFacility.NATIONAL_RAIL, StationFacility.CASH_MACHINE),
            toilets = true,
            exits = listOf(
                StationExit("wl-1", "Exit 1", "Waterloo Mainline Station", 3, 90, isStepFree = true, nearbyLandmarks = listOf("Waterloo Station", "The Old Vic"), streetName = "Station Approach"),
                StationExit("wl-2", "Exit 2", "South Bank / Riverside", 6, 60, nearbyLandmarks = listOf("London Eye", "Southbank Centre", "National Theatre"), streetName = "Belvedere Road"),
                StationExit("wl-3", "Exit 3", "York Road", 1, 45, nearbyLandmarks = listOf("IMAX Cinema", "Lower Marsh Market"), streetName = "York Road")))

        s("victoria", "Victoria", listOf("victoria","circle","district"), "1", 51.4965, -0.1447,
            stepFree = true, interchangeMin = 3, annualM = 82.0, crowdMult = 1.3f,
            facilities = setOf(StationFacility.WIFI, StationFacility.TOILETS, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.TAXI_RANK, StationFacility.NATIONAL_RAIL, StationFacility.BUS_STOP),
            toilets = true,
            exits = listOf(
                StationExit("vc-a", "Exit A", "Victoria Mainline Station", 4, 60, isStepFree = true, nearbyLandmarks = listOf("Victoria Coach Station", "Apollo Victoria Theatre"), streetName = "Terminus Place"),
                StationExit("vc-b", "Exit B", "Cardinal Place", 1, 45, nearbyLandmarks = listOf("Cardinal Place Shopping", "Westminster Cathedral"), streetName = "Victoria Street"),
                StationExit("vc-c", "Exit C", "Buckingham Palace Road", 7, 75, nearbyLandmarks = listOf("Buckingham Palace", "Royal Mews"), streetName = "Buckingham Palace Road")))

        s("london-bridge", "London Bridge", listOf("jubilee","northern"), "1", 51.5052, -0.0864,
            stepFree = true, interchangeMin = 3, annualM = 71.0, crowdMult = 1.3f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.NATIONAL_RAIL, StationFacility.TAXI_RANK),
            exits = listOf(
                StationExit("lb-1", "Exit 1", "Borough High Street", 2, 75, nearbyLandmarks = listOf("The Shard", "Borough Market"), streetName = "Borough High Street"),
                StationExit("lb-2", "Exit 2", "Tooley Street", 5, 60, isStepFree = true, nearbyLandmarks = listOf("HMS Belfast", "City Hall"), streetName = "Tooley Street"),
                StationExit("lb-3", "Exit 3", "London Bridge Mainline", 7, 90, isStepFree = true, nearbyLandmarks = listOf("London Bridge", "Southwark Cathedral"), streetName = "Station Approach")))

        s("paddington", "Paddington", listOf("bakerloo","circle","district","hammersmith-city","elizabeth"), "1", 51.5154, -0.1755,
            stepFree = true, interchangeMin = 4, annualM = 50.0, crowdMult = 1.2f,
            facilities = setOf(StationFacility.WIFI, StationFacility.TOILETS, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.TAXI_RANK, StationFacility.NATIONAL_RAIL, StationFacility.BIKE_PARKING),
            toilets = true,
            exits = listOf(
                StationExit("pd-a", "Exit A", "Paddington Mainline / Heathrow Express", 4, 90, isStepFree = true, nearbyLandmarks = listOf("Paddington Station", "Paddington Bear Statue"), streetName = "Praed Street"),
                StationExit("pd-b", "Exit B", "Praed Street", 1, 45, nearbyLandmarks = listOf("St Mary's Hospital", "Edgware Road shops"), streetName = "Praed Street"),
                StationExit("pd-c", "Exit C", "Eastbourne Terrace", 7, 60, nearbyLandmarks = listOf("Little Venice", "Heathrow Express lounge"), streetName = "Eastbourne Terrace")))

        s("baker-street", "Baker Street", listOf("bakerloo","jubilee","metropolitan","hammersmith-city","circle"), "1", 51.5226, -0.1571,
            interchangeMin = 5, annualM = 32.0, crowdMult = 1.1f,
            facilities = setOf(StationFacility.WIFI, StationFacility.ESCALATORS, StationFacility.CASH_MACHINE),
            exits = listOf(
                StationExit("bs-1", "Exit 1", "Baker Street (221B)", 3, 45, nearbyLandmarks = listOf("Sherlock Holmes Museum", "Madame Tussauds"), streetName = "Baker Street"),
                StationExit("bs-2", "Exit 2", "Marylebone Road", 6, 60, nearbyLandmarks = listOf("Regent's Park", "London Business School"), streetName = "Marylebone Road")))

        s("bond-street", "Bond Street", listOf("central","jubilee","elizabeth"), "1", 51.5142, -0.1494,
            stepFree = true, interchangeMin = 3, annualM = 36.0, crowdMult = 1.2f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS),
            exits = listOf(
                StationExit("bnd-a", "Exit A", "Oxford Street", 3, 45, isStepFree = true, nearbyLandmarks = listOf("Selfridges", "Oxford Street"), streetName = "Oxford Street"),
                StationExit("bnd-b", "Exit B", "Davies Street", 6, 55, nearbyLandmarks = listOf("Claridge's Hotel", "Brook Street"), streetName = "Davies Street")))

        s("piccadilly-circus", "Piccadilly Circus", listOf("bakerloo","piccadilly"), "1", 51.5100, -0.1347,
            interchangeMin = 2, annualM = 41.0, crowdMult = 1.3f,
            facilities = setOf(StationFacility.WIFI, StationFacility.ESCALATORS),
            exits = listOf(
                StationExit("pc-1", "Exit 1", "Piccadilly Circus (Eros)", 4, 60, nearbyLandmarks = listOf("Eros Statue", "Criterion Theatre", "Piccadilly Lights"), streetName = "Piccadilly Circus"),
                StationExit("pc-2", "Exit 2", "Shaftesbury Avenue", 2, 45, nearbyLandmarks = listOf("Chinatown", "Leicester Square"), streetName = "Shaftesbury Avenue"),
                StationExit("pc-3", "Exit 3", "Regent Street", 7, 50, nearbyLandmarks = listOf("Regent Street Shops", "Waterstones Piccadilly"), streetName = "Regent Street")))

        s("leicester-square", "Leicester Square", listOf("northern","piccadilly"), "1", 51.5113, -0.1281,
            interchangeMin = 2, annualM = 39.0, crowdMult = 1.3f,
            exits = listOf(
                StationExit("ls-1", "Exit 1", "Leicester Square", 3, 45, nearbyLandmarks = listOf("Leicester Square Gardens", "Odeon Luxe", "TKTS Booth"), streetName = "Cranbourn Street"),
                StationExit("ls-2", "Exit 2", "Charing Cross Road", 6, 55, nearbyLandmarks = listOf("National Gallery", "Trafalgar Square"), streetName = "Charing Cross Road")))

        s("westminster", "Westminster", listOf("jubilee","circle","district"), "1", 51.5010, -0.1254,
            stepFree = true, interchangeMin = 3, annualM = 28.0, crowdMult = 1.1f, carriages = 7,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS),
            exits = listOf(
                StationExit("wm-1", "Exit 1", "Bridge Street / Parliament", 2, 60, isStepFree = true, nearbyLandmarks = listOf("Big Ben", "Houses of Parliament", "Westminster Abbey"), streetName = "Bridge Street"),
                StationExit("wm-2", "Exit 2", "Parliament Street", 5, 75, nearbyLandmarks = listOf("Downing Street", "Cenotaph", "Churchill War Rooms"), streetName = "Parliament Street"),
                StationExit("wm-3", "Exit 3", "Westminster Bridge", 7, 90, isStepFree = true, nearbyLandmarks = listOf("London Eye", "Westminster Bridge"), streetName = "Westminster Bridge Road")))

        s("canary-wharf", "Canary Wharf", listOf("jubilee"), "2", 51.5055, -0.0196,
            stepFree = true, carriages = 7, annualM = 52.0, crowdMult = 1.4f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.CASH_MACHINE, StationFacility.TAXI_RANK),
            exits = listOf(
                StationExit("cw-a", "Exit A", "Canada Square West", 3, 60, isStepFree = true, nearbyLandmarks = listOf("One Canada Square", "Jubilee Park"), streetName = "Canada Square"),
                StationExit("cw-b", "Exit B", "Churchill Place East", 6, 45, isStepFree = true, nearbyLandmarks = listOf("Barclays HQ", "Crossrail Place Roof Garden"), streetName = "Churchill Place")))

        s("stratford", "Stratford", listOf("central","jubilee","elizabeth"), "3", 51.5416, -0.0033,
            stepFree = true, interchangeMin = 3, annualM = 30.0, crowdMult = 1.1f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.ESCALATORS, StationFacility.NATIONAL_RAIL, StationFacility.BUS_STOP, StationFacility.BIKE_PARKING, StationFacility.TAXI_RANK),
            exits = listOf(
                StationExit("st-a", "Exit A", "Westfield Stratford City", 5, 60, isStepFree = true, nearbyLandmarks = listOf("Westfield Shopping Centre", "John Lewis"), streetName = "Montfichet Road"),
                StationExit("st-b", "Exit B", "Olympic Park", 2, 90, isStepFree = true, nearbyLandmarks = listOf("London Stadium", "ArcelorMittal Orbit"), streetName = "International Way"),
                StationExit("st-c", "Exit C", "Stratford High Street", 7, 45, nearbyLandmarks = listOf("Theatre Royal Stratford East"), streetName = "The Broadway")))

        // ── Zone 1 Additional ──────────────────────────────────
        s("embankment", "Embankment", listOf("bakerloo","northern","circle","district"), "1", 51.5074, -0.1223, interchangeMin = 3, annualM = 17.0,
            exits = listOf(
                StationExit("emb-1", "Exit 1", "Victoria Embankment", 3, 45, nearbyLandmarks = listOf("River Thames", "Cleopatra's Needle"), streetName = "Villiers Street"),
                StationExit("emb-2", "Exit 2", "Villiers Street", 6, 40, nearbyLandmarks = listOf("Charing Cross Station", "Embankment Gardens"), streetName = "Villiers Street")))

        s("charing-cross", "Charing Cross", listOf("bakerloo","northern"), "1", 51.5081, -0.1246, interchangeMin = 2, annualM = 19.0,
            exits = listOf(
                StationExit("cc-1", "Exit 1", "Trafalgar Square", 3, 60, nearbyLandmarks = listOf("Trafalgar Square", "Nelson's Column", "National Gallery"), streetName = "Strand"),
                StationExit("cc-2", "Exit 2", "Strand / Charing Cross Station", 6, 45, nearbyLandmarks = listOf("Charing Cross Station", "The Strand"), streetName = "Strand")))

        s("tottenham-court-road", "Tottenham Court Road", listOf("central","northern","elizabeth"), "1", 51.5165, -0.1310,
            stepFree = true, interchangeMin = 3, annualM = 34.0, crowdMult = 1.2f,
            exits = listOf(
                StationExit("tcr-1", "Exit 1", "Oxford Street / Centre Point", 3, 45, isStepFree = true, nearbyLandmarks = listOf("Centre Point", "Crossrail Place"), streetName = "Oxford Street"),
                StationExit("tcr-2", "Exit 2", "Charing Cross Road", 6, 55, nearbyLandmarks = listOf("Soho", "Denmark Street", "Foyles"), streetName = "Charing Cross Road")))

        s("holborn", "Holborn", listOf("central","piccadilly"), "1", 51.5174, -0.1201, interchangeMin = 2, annualM = 28.0,
            exits = listOf(
                StationExit("hb-1", "Exit 1", "High Holborn / British Museum", 3, 45, nearbyLandmarks = listOf("British Museum", "Sir John Soane's Museum"), streetName = "High Holborn"),
                StationExit("hb-2", "Exit 2", "Kingsway", 6, 50, nearbyLandmarks = listOf("Lincoln's Inn Fields", "LSE"), streetName = "Kingsway")))

        s("covent-garden", "Covent Garden", listOf("piccadilly"), "1", 51.5129, -0.1243, annualM = 18.0, crowdMult = 1.3f,
            exits = listOf(StationExit("cg-1", "Exit 1", "Long Acre / Covent Garden Market", 4, 45, nearbyLandmarks = listOf("Covent Garden Market", "Royal Opera House", "London Transport Museum"), streetName = "Long Acre")))

        s("south-kensington", "South Kensington", listOf("piccadilly","circle","district"), "1", 51.4941, -0.1738, interchangeMin = 2, annualM = 26.0,
            exits = listOf(
                StationExit("sk-1", "Exit 1", "Museum Tunnel", 3, 90, nearbyLandmarks = listOf("Natural History Museum", "V&A Museum", "Science Museum"), streetName = "Thurloe Street"),
                StationExit("sk-2", "Exit 2", "Pelham Street", 6, 45, nearbyLandmarks = listOf("South Ken restaurants", "Imperial College"), streetName = "Pelham Street")))

        s("knightsbridge", "Knightsbridge", listOf("piccadilly"), "1", 51.5015, -0.1607, annualM = 19.0,
            exits = listOf(
                StationExit("kb-1", "Exit 1", "Harrods", 3, 45, nearbyLandmarks = listOf("Harrods", "Harvey Nichols"), streetName = "Brompton Road"),
                StationExit("kb-2", "Exit 2", "Sloane Street", 6, 55, nearbyLandmarks = listOf("One Hyde Park", "Mandarin Oriental"), streetName = "Sloane Street")))

        s("hyde-park-corner", "Hyde Park Corner", listOf("piccadilly"), "1", 51.5027, -0.1527, annualM = 13.0,
            exits = listOf(StationExit("hpc-1", "Exit 1", "Hyde Park Corner / Wellington Arch", 4, 60, nearbyLandmarks = listOf("Wellington Arch", "Hyde Park", "Apsley House"), streetName = "Knightsbridge")))

        s("liverpool-street", "Liverpool Street", listOf("central","metropolitan","hammersmith-city","circle","elizabeth"), "1", 51.5178, -0.0823,
            stepFree = true, interchangeMin = 4, annualM = 71.0, crowdMult = 1.4f,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.NATIONAL_RAIL, StationFacility.TAXI_RANK, StationFacility.TOILETS),
            toilets = true,
            exits = listOf(
                StationExit("lvs-a", "Exit A", "Liverpool Street Mainline", 3, 75, isStepFree = true, nearbyLandmarks = listOf("Liverpool Street Station", "Broadgate Circle"), streetName = "Liverpool Street"),
                StationExit("lvs-b", "Exit B", "Bishopsgate", 6, 55, nearbyLandmarks = listOf("Bishopsgate", "Spitalfields Market"), streetName = "Bishopsgate")))

        s("farringdon", "Farringdon", listOf("metropolitan","hammersmith-city","circle","elizabeth"), "1", 51.5203, -0.1053,
            stepFree = true, interchangeMin = 3, annualM = 16.0,
            facilities = setOf(StationFacility.WIFI, StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.NATIONAL_RAIL),
            exits = listOf(
                StationExit("fg-1", "Exit 1", "Cowcross Street / Smithfield", 3, 45, isStepFree = true, nearbyLandmarks = listOf("Smithfield Market", "Clerkenwell"), streetName = "Cowcross Street"),
                StationExit("fg-2", "Exit 2", "Farringdon Road / Hatton Garden", 6, 50, nearbyLandmarks = listOf("Hatton Garden", "The Guardian offices"), streetName = "Farringdon Road")))

        // Zone 1 smaller stations
        s("warren-street", "Warren Street", listOf("northern","victoria"), "1", 51.5247, -0.1384, interchangeMin = 2, annualM = 18.0)
        s("euston", "Euston", listOf("northern","victoria"), "1", 51.5282, -0.1337, stepFree = true, interchangeMin = 3, annualM = 42.0,
            facilities = setOf(StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.NATIONAL_RAIL, StationFacility.TAXI_RANK, StationFacility.TOILETS), toilets = true,
            exits = listOf(StationExit("eu-1", "Exit 1", "Euston Mainline Station", 3, 75, isStepFree = true, nearbyLandmarks = listOf("Euston Station", "Wellcome Collection"), streetName = "Euston Road")))
        s("moorgate", "Moorgate", listOf("northern","metropolitan","hammersmith-city","circle"), "1", 51.5186, -0.0886, interchangeMin = 3, annualM = 22.0)
        s("angel", "Angel", listOf("northern"), "1", 51.5322, -0.1058, annualM = 20.0,
            exits = listOf(StationExit("ag-1", "Exit 1", "Islington High Street", 4, 45, nearbyLandmarks = listOf("Angel Shopping Centre", "Camden Passage"), streetName = "Islington High Street")))
        s("old-street", "Old Street", listOf("northern"), "1", 51.5258, -0.0875, annualM = 18.0)
        s("goodge-street", "Goodge Street", listOf("northern"), "1", 51.5205, -0.1347)
        s("mornington-crescent", "Mornington Crescent", listOf("northern"), "2", 51.5342, -0.1387)
        s("borough", "Borough", listOf("northern"), "1", 51.5011, -0.0943,
            exits = listOf(StationExit("bor-1", "Exit 1", "Borough High Street", 4, 40, nearbyLandmarks = listOf("Borough Market", "The Shard (walk)"), streetName = "Borough High Street")))
        s("elephant-castle", "Elephant & Castle", listOf("bakerloo","northern"), "1-2", 51.4943, -0.1001, interchangeMin = 2)
        s("lambeth-north", "Lambeth North", listOf("bakerloo"), "1", 51.4986, -0.1118)
        s("regents-park", "Regent's Park", listOf("bakerloo"), "1", 51.5234, -0.1466)
        s("marylebone", "Marylebone", listOf("bakerloo"), "1", 51.5225, -0.1631)
        s("edgware-road-bakerloo", "Edgware Road (Bakerloo)", listOf("bakerloo"), "1", 51.5199, -0.1679)
        s("pimlico", "Pimlico", listOf("victoria"), "1", 51.4893, -0.1334)
        s("vauxhall", "Vauxhall", listOf("victoria"), "1-2", 51.4861, -0.1233, stepFree = true,
            facilities = setOf(StationFacility.STEP_FREE, StationFacility.NATIONAL_RAIL, StationFacility.BUS_STOP))
        s("st-pauls", "St. Paul's", listOf("central"), "1", 51.5146, -0.0973,
            exits = listOf(StationExit("stp-1", "Exit 1", "St Paul's Cathedral", 4, 45, nearbyLandmarks = listOf("St Paul's Cathedral", "Millennium Bridge"), streetName = "Newgate Street")))
        s("chancery-lane", "Chancery Lane", listOf("central"), "1", 51.5185, -0.1111)
        s("marble-arch", "Marble Arch", listOf("central"), "1", 51.5136, -0.1586,
            exits = listOf(StationExit("ma-1", "Exit 1", "Oxford Street / Hyde Park", 4, 45, nearbyLandmarks = listOf("Marble Arch", "Hyde Park", "Speakers' Corner"), streetName = "Oxford Street")))
        s("lancaster-gate", "Lancaster Gate", listOf("central"), "1", 51.5119, -0.1756)
        s("queensway", "Queensway", listOf("central"), "1", 51.5107, -0.1871)
        s("russell-square", "Russell Square", listOf("piccadilly"), "1", 51.5233, -0.1244)
        s("monument", "Monument", listOf("circle","district"), "1", 51.5108, -0.0861, interchangeMin = 4, annualM = 14.0)
        s("tower-hill", "Tower Hill", listOf("circle","district"), "1", 51.5098, -0.0766, annualM = 18.0,
            exits = listOf(StationExit("th-1", "Exit 1", "Tower of London", 4, 45, nearbyLandmarks = listOf("Tower of London", "Tower Bridge"), streetName = "Tower Hill")))
        s("cannon-street", "Cannon Street", listOf("circle","district"), "1", 51.5113, -0.0904)
        s("mansion-house", "Mansion House", listOf("circle","district"), "1", 51.5122, -0.0940)
        s("blackfriars", "Blackfriars", listOf("circle","district"), "1", 51.5117, -0.1037, stepFree = true)
        s("temple", "Temple", listOf("circle","district"), "1", 51.5111, -0.1141)
        s("sloane-square", "Sloane Square", listOf("circle","district"), "1", 51.4924, -0.1565,
            exits = listOf(StationExit("ss-1", "Exit 1", "Sloane Square / King's Road", 4, 40, nearbyLandmarks = listOf("Peter Jones", "Royal Court Theatre", "King's Road"), streetName = "Sloane Square")))
        s("st-james-park", "St. James's Park", listOf("circle","district"), "1", 51.4994, -0.1345)
        s("gloucester-road", "Gloucester Road", listOf("piccadilly","circle","district"), "1", 51.4945, -0.1829, interchangeMin = 2)
        s("earls-court", "Earl's Court", listOf("district","piccadilly"), "1-2", 51.4913, -0.1935, interchangeMin = 2, annualM = 18.0)
        s("aldgate", "Aldgate", listOf("metropolitan","circle"), "1", 51.5143, -0.0755)
        s("aldgate-east", "Aldgate East", listOf("district","hammersmith-city"), "1", 51.5152, -0.0726)
        s("edgware-road", "Edgware Road", listOf("circle","district","hammersmith-city"), "1", 51.5199, -0.1679)
        s("great-portland-street", "Great Portland Street", listOf("metropolitan","hammersmith-city","circle"), "1", 51.5238, -0.1440)
        s("euston-square", "Euston Square", listOf("metropolitan","hammersmith-city","circle"), "1", 51.5260, -0.1359)
        s("barbican", "Barbican", listOf("metropolitan","hammersmith-city","circle"), "1", 51.5204, -0.0979,
            exits = listOf(StationExit("bar-1", "Exit 1", "Barbican Centre / Aldersgate St", 4, 50, nearbyLandmarks = listOf("Barbican Centre", "Museum of London"), streetName = "Aldersgate Street")))
        s("notting-hill-gate", "Notting Hill Gate", listOf("central","circle","district"), "1-2", 51.5094, -0.1967, interchangeMin = 2,
            exits = listOf(StationExit("nhg-1", "Exit 1", "Notting Hill Gate / Portobello Rd", 4, 40, nearbyLandmarks = listOf("Portobello Road Market", "Notting Hill"), streetName = "Notting Hill Gate")))

        // ── Zone 2-3 ──────────────────────────────────────────
        s("camden-town", "Camden Town", listOf("northern"), "2", 51.5392, -0.1426, interchangeMin = 2, annualM = 28.0, crowdMult = 1.2f,
            exits = listOf(StationExit("ct-1", "Exit 1", "Camden High Street / Markets", 4, 45, nearbyLandmarks = listOf("Camden Market", "Camden Lock", "Regent's Canal"), streetName = "Camden High Street")))
        s("brixton", "Brixton", listOf("victoria"), "2", 51.4627, -0.1145, annualM = 30.0,
            exits = listOf(StationExit("bx-1", "Exit 1", "Brixton Road / Markets", 4, 40, nearbyLandmarks = listOf("Brixton Market", "O2 Academy Brixton"), streetName = "Brixton Road")))
        s("stockwell", "Stockwell", listOf("northern","victoria"), "2", 51.4723, -0.1230, interchangeMin = 2, annualM = 14.0)
        s("kennington", "Kennington", listOf("northern"), "1-2", 51.4884, -0.1053)
        s("oval", "Oval", listOf("northern"), "1-2", 51.4819, -0.1128)
        s("highbury-islington", "Highbury & Islington", listOf("victoria"), "2", 51.5462, -0.1039)
        s("finsbury-park", "Finsbury Park", listOf("victoria","piccadilly"), "2", 51.5642, -0.1065, stepFree = true, interchangeMin = 2, annualM = 29.0,
            facilities = setOf(StationFacility.STEP_FREE, StationFacility.NATIONAL_RAIL))
        s("canada-water", "Canada Water", listOf("jubilee"), "2", 51.4982, -0.0502, stepFree = true)
        s("bermondsey", "Bermondsey", listOf("jubilee"), "2", 51.4979, -0.0637, stepFree = true)
        s("southwark", "Southwark", listOf("jubilee"), "1", 51.5040, -0.1050, stepFree = true)
        s("north-greenwich", "North Greenwich", listOf("jubilee"), "2-3", 51.5005, 0.0039, stepFree = true, annualM = 18.0,
            exits = listOf(StationExit("ng-1", "Exit 1", "The O2 Arena", 4, 50, isStepFree = true, nearbyLandmarks = listOf("The O2 Arena", "Emirates Air Line"), streetName = "Millennium Way")))
        s("canning-town", "Canning Town", listOf("jubilee"), "3", 51.5147, 0.0082, stepFree = true, interchangeMin = 2)
        s("west-ham", "West Ham", listOf("jubilee","district","hammersmith-city"), "3", 51.5287, 0.0056, stepFree = true, interchangeMin = 2)
        s("mile-end", "Mile End", listOf("central","district","hammersmith-city"), "2", 51.5249, -0.0332, interchangeMin = 2)
        s("whitechapel", "Whitechapel", listOf("district","hammersmith-city","elizabeth"), "2", 51.5194, -0.0596, stepFree = true, interchangeMin = 2)
        s("hammersmith", "Hammersmith", listOf("piccadilly","district","hammersmith-city","circle"), "2", 51.4936, -0.2251, interchangeMin = 3, annualM = 25.0)
        s("barons-court", "Barons Court", listOf("district","piccadilly"), "2", 51.4905, -0.2139)
        s("acton-town", "Acton Town", listOf("district","piccadilly"), "3", 51.5028, -0.2801, interchangeMin = 2)
        s("bethnal-green", "Bethnal Green", listOf("central"), "2", 51.5270, -0.0549)
        s("clapham-north", "Clapham North", listOf("northern"), "2", 51.4649, -0.1299)
        s("clapham-common", "Clapham Common", listOf("northern"), "2", 51.4618, -0.1384)
        s("clapham-south", "Clapham South", listOf("northern"), "2", 51.4527, -0.1480)
        s("kentish-town", "Kentish Town", listOf("northern"), "2", 51.5507, -0.1402)
        s("tufnell-park", "Tufnell Park", listOf("northern"), "2", 51.5567, -0.1380)
        s("caledonian-road", "Caledonian Road", listOf("piccadilly"), "2", 51.5481, -0.1188)
        s("holloway-road", "Holloway Road", listOf("piccadilly"), "2", 51.5526, -0.1132)
        s("arsenal", "Arsenal", listOf("piccadilly"), "2", 51.5586, -0.1059)
        s("wembley-park", "Wembley Park", listOf("jubilee","metropolitan"), "4", 51.5635, -0.2795, stepFree = true, interchangeMin = 2, annualM = 18.0,
            exits = listOf(StationExit("wp-1", "Exit 1", "Wembley Stadium / Arena", 4, 60, isStepFree = true, nearbyLandmarks = listOf("Wembley Stadium", "SSE Arena"), streetName = "Engineers Way")))
        s("finchley-road", "Finchley Road", listOf("jubilee","metropolitan"), "2", 51.5472, -0.1803, interchangeMin = 2)
        s("swiss-cottage", "Swiss Cottage", listOf("jubilee"), "2", 51.5432, -0.1745)
        s("st-johns-wood", "St. John's Wood", listOf("jubilee"), "2", 51.5347, -0.1738,
            exits = listOf(StationExit("sjw-1", "Exit 1", "Wellington Road / Lord's Cricket", 4, 45, nearbyLandmarks = listOf("Lord's Cricket Ground", "Abbey Road Studios"), streetName = "Wellington Road")))

        // ── Zone 3+ ──────────────────────────────────────────
        s("archway", "Archway", listOf("northern"), "2-3", 51.5653, -0.1353)
        s("highgate", "Highgate", listOf("northern"), "3", 51.5777, -0.1466)
        s("east-finchley", "East Finchley", listOf("northern"), "3", 51.5874, -0.1650)
        s("finchley-central", "Finchley Central", listOf("northern"), "4", 51.6012, -0.1927)
        s("high-barnet", "High Barnet", listOf("northern"), "5", 51.6503, -0.1943)
        s("balham", "Balham", listOf("northern"), "3", 51.4431, -0.1525)
        s("tooting-bec", "Tooting Bec", listOf("northern"), "3", 51.4355, -0.1591)
        s("tooting-broadway", "Tooting Broadway", listOf("northern"), "3", 51.4275, -0.1682)
        s("colliers-wood", "Colliers Wood", listOf("northern"), "3-4", 51.4180, -0.1773)
        s("south-wimbledon", "South Wimbledon", listOf("northern"), "3-4", 51.4154, -0.1862)
        s("morden", "Morden", listOf("northern"), "4", 51.4022, -0.1948)
        s("seven-sisters", "Seven Sisters", listOf("victoria"), "3", 51.5822, -0.0749)
        s("tottenham-hale", "Tottenham Hale", listOf("victoria"), "3", 51.5882, -0.0604, stepFree = true)
        s("blackhorse-road", "Blackhorse Road", listOf("victoria"), "3", 51.5867, -0.0417)
        s("walthamstow-central", "Walthamstow Central", listOf("victoria"), "3", 51.5830, -0.0197)
        s("stanmore", "Stanmore", listOf("jubilee"), "5", 51.6194, -0.3028)
        s("canons-park", "Canons Park", listOf("jubilee"), "5", 51.6078, -0.2947)
        s("ealing-broadway", "Ealing Broadway", listOf("central","district","elizabeth"), "3", 51.5148, -0.3013, stepFree = true, interchangeMin = 2)
        s("barking", "Barking", listOf("district","hammersmith-city"), "4", 51.5396, 0.0809)
        s("heathrow-t123", "Heathrow T2 & 3", listOf("piccadilly","elizabeth"), "6", 51.4713, -0.4524, stepFree = true,
            facilities = setOf(StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.TOILETS), toilets = true)
        s("heathrow-t5", "Heathrow Terminal 5", listOf("piccadilly","elizabeth"), "6", 51.4723, -0.4901, stepFree = true,
            facilities = setOf(StationFacility.STEP_FREE, StationFacility.LIFTS, StationFacility.TOILETS), toilets = true)
        s("woolwich", "Woolwich", listOf("elizabeth"), "4", 51.4917, 0.0715, stepFree = true)
        s("abbey-wood", "Abbey Wood", listOf("elizabeth"), "4", 51.4907, 0.1204, stepFree = true)

        // ── Missing stations referenced in line routes / connections ──
        // Central line
        s("shepherds-bush", "Shepherd's Bush", listOf("central"), "2", 51.5046, -0.2187)
        s("holland-park", "Holland Park", listOf("central"), "2", 51.5075, -0.2060)
        s("north-acton", "North Acton", listOf("central"), "2-3", 51.5237, -0.2597)
        s("white-city", "White City", listOf("central"), "2", 51.5120, -0.2243)
        s("leyton", "Leyton", listOf("central"), "3", 51.5566, -0.0053)
        s("leytonstone", "Leytonstone", listOf("central"), "3-4", 51.5683, 0.0083)

        // Circle / H&C western section
        s("shepherds-bush-market", "Shepherd's Bush Market", listOf("circle","hammersmith-city"), "2", 51.5058, -0.2265)
        s("wood-lane", "Wood Lane", listOf("circle","hammersmith-city"), "2", 51.5098, -0.2245, stepFree = true)
        s("ladbroke-grove", "Ladbroke Grove", listOf("circle","hammersmith-city"), "2", 51.5172, -0.2107)
        s("westbourne-park", "Westbourne Park", listOf("circle","hammersmith-city"), "2", 51.5210, -0.2010)
        s("bayswater", "Bayswater", listOf("circle"), "1", 51.5122, -0.1875)
        s("high-street-kensington", "High Street Kensington", listOf("circle","district"), "1", 51.5009, -0.1925)

        // Northern line outer
        s("edgware", "Edgware", listOf("northern"), "5", 51.6137, -0.2750)
        s("golders-green", "Golders Green", listOf("northern"), "3", 51.5722, -0.1942)
        s("hampstead", "Hampstead", listOf("northern"), "2-3", 51.5568, -0.1782)
        s("chalk-farm", "Chalk Farm", listOf("northern"), "2", 51.5441, -0.1538)

        // Metropolitan line outer
        s("harrow-on-the-hill", "Harrow-on-the-Hill", listOf("metropolitan"), "5", 51.5793, -0.3370, stepFree = true,
            facilities = setOf(StationFacility.NATIONAL_RAIL))
        s("amersham", "Amersham", listOf("metropolitan"), "9", 51.6740, -0.6073, stepFree = true)
        s("chesham", "Chesham", listOf("metropolitan"), "9", 51.7052, -0.6110)
        s("uxbridge", "Uxbridge", listOf("metropolitan","piccadilly"), "6", 51.5467, -0.4782, stepFree = true, interchangeMin = 2)

        // Piccadilly outer (Finsbury Park → Cockfosters)
        s("cockfosters", "Cockfosters", listOf("piccadilly"), "5", 51.6517, -0.1496)
        s("oakwood", "Oakwood", listOf("piccadilly"), "5", 51.6476, -0.1337)
        s("southgate", "Southgate", listOf("piccadilly"), "4", 51.6324, -0.1279)
        s("arnos-grove", "Arnos Grove", listOf("piccadilly"), "4", 51.6161, -0.1337)
        s("bounds-green", "Bounds Green", listOf("piccadilly"), "3-4", 51.6064, -0.1253)
        s("wood-green", "Wood Green", listOf("piccadilly"), "3", 51.5976, -0.1116)
        s("turnpike-lane", "Turnpike Lane", listOf("piccadilly"), "3", 51.5904, -0.1031)
        s("manor-house", "Manor House", listOf("piccadilly"), "3", 51.5714, -0.0957)

        // Bakerloo northern (Paddington → Harrow & Wealdstone)
        s("warwick-avenue", "Warwick Avenue", listOf("bakerloo"), "2", 51.5234, -0.1836)
        s("maida-vale", "Maida Vale", listOf("bakerloo"), "2", 51.5297, -0.1854)
        s("kilburn-park", "Kilburn Park", listOf("bakerloo"), "2", 51.5353, -0.1940)
        s("queens-park", "Queen's Park", listOf("bakerloo"), "2", 51.5343, -0.2043)
        s("kensal-green", "Kensal Green", listOf("bakerloo"), "2-3", 51.5307, -0.2249)
        s("harlesden", "Harlesden", listOf("bakerloo"), "3", 51.5363, -0.2572)
        s("stonebridge-park", "Stonebridge Park", listOf("bakerloo"), "3", 51.5444, -0.2762)
        s("wembley-central", "Wembley Central", listOf("bakerloo"), "4", 51.5521, -0.2963)
        s("north-wembley", "North Wembley", listOf("bakerloo"), "4", 51.5634, -0.3041)
        s("south-kenton", "South Kenton", listOf("bakerloo"), "5", 51.5700, -0.3084)
        s("kenton", "Kenton", listOf("bakerloo"), "5", 51.5797, -0.3158)
        s("harrow-wealdstone", "Harrow & Wealdstone", listOf("bakerloo"), "5", 51.5925, -0.3358)

        // Metropolitan intermediate (Harrow → Amersham spine)
        s("north-harrow", "North Harrow", listOf("metropolitan"), "5", 51.5856, -0.3614)
        s("pinner", "Pinner", listOf("metropolitan"), "5", 51.5928, -0.3800)
        s("northwood-hills", "Northwood Hills", listOf("metropolitan"), "6", 51.6020, -0.4009)
        s("northwood", "Northwood", listOf("metropolitan"), "6", 51.6092, -0.4238)
        s("chorleywood", "Chorleywood", listOf("metropolitan"), "7", 51.6557, -0.5172)
        s("chalfont-latimer", "Chalfont & Latimer", listOf("metropolitan"), "8-9", 51.6686, -0.5700)

        // Metropolitan/Piccadilly shared Uxbridge branch
        s("rayners-lane", "Rayners Lane", listOf("metropolitan","piccadilly"), "5", 51.5752, -0.3716)
        s("eastcote", "Eastcote", listOf("metropolitan","piccadilly"), "5", 51.5782, -0.3968)
        s("ruislip-manor", "Ruislip Manor", listOf("metropolitan","piccadilly"), "6", 51.5721, -0.4205)
        s("ruislip", "Ruislip", listOf("metropolitan","piccadilly"), "6", 51.5711, -0.4330)
        s("ickenham", "Ickenham", listOf("metropolitan","piccadilly"), "6", 51.5581, -0.4465)
        s("hillingdon", "Hillingdon", listOf("metropolitan","piccadilly"), "6", 51.5568, -0.4607)

        return map
    }

    // ── Connection Graph (real travel times between adjacent stations) ──

    private fun buildConnectionGraph(): List<StationConnection> {
        val conn = mutableListOf<StationConnection>()
        fun c(from: String, to: String, line: String, min: Int) { conn.add(StationConnection(from, to, line, min)) }

        // Bakerloo
        c("elephant-castle","lambeth-north","bakerloo",2); c("lambeth-north","waterloo","bakerloo",2)
        c("waterloo","embankment","bakerloo",2); c("embankment","charing-cross","bakerloo",1)
        c("charing-cross","piccadilly-circus","bakerloo",2); c("piccadilly-circus","oxford-circus","bakerloo",2)
        c("oxford-circus","regents-park","bakerloo",1); c("regents-park","baker-street","bakerloo",2)
        c("baker-street","marylebone","bakerloo",2); c("marylebone","edgware-road-bakerloo","bakerloo",2)
        c("edgware-road-bakerloo","paddington","bakerloo",2)

        // Victoria
        c("brixton","stockwell","victoria",2); c("stockwell","vauxhall","victoria",2)
        c("vauxhall","pimlico","victoria",2); c("pimlico","victoria","victoria",1)
        c("victoria","green-park","victoria",2); c("green-park","oxford-circus","victoria",1)
        c("oxford-circus","warren-street","victoria",1); c("warren-street","euston","victoria",1)
        c("euston","kings-cross","victoria",2); c("kings-cross","highbury-islington","victoria",3)
        c("highbury-islington","finsbury-park","victoria",2); c("finsbury-park","seven-sisters","victoria",3)
        c("seven-sisters","tottenham-hale","victoria",2); c("tottenham-hale","blackhorse-road","victoria",2)
        c("blackhorse-road","walthamstow-central","victoria",2)

        // Central
        c("ealing-broadway","north-acton","central",8); c("north-acton","white-city","central",3)
        c("white-city","shepherds-bush","central",1); c("shepherds-bush","holland-park","central",2)
        c("holland-park","notting-hill-gate","central",1); c("notting-hill-gate","queensway","central",1)
        c("queensway","lancaster-gate","central",1); c("lancaster-gate","marble-arch","central",2)
        c("marble-arch","bond-street","central",1); c("bond-street","oxford-circus","central",1)
        c("oxford-circus","tottenham-court-road","central",1); c("tottenham-court-road","holborn","central",2)
        c("holborn","chancery-lane","central",1); c("chancery-lane","st-pauls","central",2)
        c("st-pauls","bank","central",2); c("bank","liverpool-street","central",2)
        c("liverpool-street","bethnal-green","central",3); c("bethnal-green","mile-end","central",2)
        c("mile-end","stratford","central",4)

        // Northern (south Charing Cross branch)
        c("morden","south-wimbledon","northern",2); c("south-wimbledon","colliers-wood","northern",1)
        c("colliers-wood","tooting-broadway","northern",2); c("tooting-broadway","tooting-bec","northern",1)
        c("tooting-bec","balham","northern",2); c("balham","clapham-south","northern",2)
        c("clapham-south","clapham-common","northern",1); c("clapham-common","clapham-north","northern",1)
        c("clapham-north","stockwell","northern",2); c("stockwell","oval","northern",1)
        c("oval","kennington","northern",2)
        // Charing Cross branch
        c("kennington","waterloo","northern",2); c("waterloo","embankment","northern",1)
        c("embankment","charing-cross","northern",1); c("charing-cross","leicester-square","northern",1)
        c("leicester-square","tottenham-court-road","northern",1)
        c("tottenham-court-road","goodge-street","northern",1); c("goodge-street","warren-street","northern",1)
        c("warren-street","euston","northern",1); c("euston","mornington-crescent","northern",1)
        c("mornington-crescent","camden-town","northern",1); c("euston","camden-town","northern",3)
        // Bank branch
        c("kennington","elephant-castle","northern",2); c("elephant-castle","borough","northern",2)
        c("borough","london-bridge","northern",1); c("london-bridge","bank","northern",2)
        c("bank","moorgate","northern",2); c("moorgate","old-street","northern",2)
        c("old-street","angel","northern",2); c("angel","kings-cross","northern",3)
        c("kings-cross","euston","northern",2)
        // High Barnet branch
        c("camden-town","kentish-town","northern",2); c("kentish-town","tufnell-park","northern",1)
        c("tufnell-park","archway","northern",2); c("archway","highgate","northern",3)
        c("highgate","east-finchley","northern",2); c("east-finchley","finchley-central","northern",3)
        c("finchley-central","high-barnet","northern",6)

        // Jubilee
        c("stanmore","canons-park","jubilee",2); c("canons-park","wembley-park","jubilee",6)
        c("wembley-park","finchley-road","jubilee",7); c("finchley-road","swiss-cottage","jubilee",1)
        c("swiss-cottage","st-johns-wood","jubilee",2); c("st-johns-wood","baker-street","jubilee",2)
        c("baker-street","bond-street","jubilee",2); c("bond-street","green-park","jubilee",2)
        c("green-park","westminster","jubilee",2); c("westminster","waterloo","jubilee",2)
        c("waterloo","southwark","jubilee",1); c("southwark","london-bridge","jubilee",2)
        c("london-bridge","bermondsey","jubilee",2); c("bermondsey","canada-water","jubilee",2)
        c("canada-water","canary-wharf","jubilee",3); c("canary-wharf","north-greenwich","jubilee",2)
        c("north-greenwich","canning-town","jubilee",2); c("canning-town","west-ham","jubilee",2)
        c("west-ham","stratford","jubilee",3)

        // Piccadilly
        c("heathrow-t5","heathrow-t123","piccadilly",4); c("heathrow-t123","acton-town","piccadilly",25)
        c("acton-town","hammersmith","piccadilly",4); c("hammersmith","barons-court","piccadilly",2)
        c("barons-court","earls-court","piccadilly",2); c("earls-court","gloucester-road","piccadilly",1)
        c("gloucester-road","south-kensington","piccadilly",1); c("south-kensington","knightsbridge","piccadilly",2)
        c("knightsbridge","hyde-park-corner","piccadilly",1); c("hyde-park-corner","green-park","piccadilly",1)
        c("green-park","piccadilly-circus","piccadilly",1); c("piccadilly-circus","leicester-square","piccadilly",1)
        c("leicester-square","covent-garden","piccadilly",1); c("covent-garden","holborn","piccadilly",2)
        c("holborn","russell-square","piccadilly",1); c("russell-square","kings-cross","piccadilly",2)
        c("kings-cross","caledonian-road","piccadilly",2); c("caledonian-road","holloway-road","piccadilly",2)
        c("holloway-road","arsenal","piccadilly",2); c("arsenal","finsbury-park","piccadilly",2)

        // Central line east
        c("stratford","leyton","central",3); c("leyton","leytonstone","central",2)

        // Circle / H&C western section
        c("hammersmith","shepherds-bush-market","circle",2); c("shepherds-bush-market","wood-lane","circle",1)
        c("wood-lane","ladbroke-grove","circle",2); c("ladbroke-grove","westbourne-park","circle",2)
        c("westbourne-park","paddington","circle",2)
        c("notting-hill-gate","bayswater","circle",1); c("bayswater","paddington","circle",3)
        c("gloucester-road","high-street-kensington","circle",2); c("high-street-kensington","notting-hill-gate","circle",2)
        c("hammersmith","shepherds-bush-market","hammersmith-city",2); c("shepherds-bush-market","wood-lane","hammersmith-city",1)
        c("wood-lane","ladbroke-grove","hammersmith-city",2); c("ladbroke-grove","westbourne-park","hammersmith-city",2)
        c("westbourne-park","paddington","hammersmith-city",2)

        // Circle / H&C / Met (shared inner)
        c("paddington","edgware-road","circle",2); c("edgware-road","baker-street","circle",3)
        c("baker-street","great-portland-street","circle",2); c("great-portland-street","euston-square","circle",1)
        c("euston-square","kings-cross","circle",2); c("kings-cross","farringdon","circle",3)
        c("farringdon","barbican","circle",2); c("barbican","moorgate","circle",2)
        c("moorgate","liverpool-street","circle",2); c("liverpool-street","aldgate","circle",2)
        // Circle south
        c("aldgate","tower-hill","circle",2); c("tower-hill","monument","circle",1)
        c("monument","cannon-street","circle",1); c("cannon-street","mansion-house","circle",1)
        c("mansion-house","blackfriars","circle",1); c("blackfriars","temple","circle",2)
        c("temple","embankment","circle",1); c("embankment","westminster","circle",2)
        c("westminster","st-james-park","circle",2); c("st-james-park","victoria","circle",1)
        c("victoria","sloane-square","circle",2); c("sloane-square","south-kensington","circle",2)
        c("south-kensington","gloucester-road","circle",1); c("gloucester-road","notting-hill-gate","circle",4)
        c("notting-hill-gate","paddington","circle",4)

        // District
        c("ealing-broadway","acton-town","district",6); c("acton-town","hammersmith","district",4)
        c("hammersmith","barons-court","district",2); c("barons-court","earls-court","district",2)
        c("earls-court","gloucester-road","district",1); c("gloucester-road","south-kensington","district",1)
        c("south-kensington","sloane-square","district",2); c("sloane-square","victoria","district",2)
        c("victoria","st-james-park","district",1); c("st-james-park","westminster","district",2)
        c("westminster","embankment","district",2); c("embankment","temple","district",1)
        c("temple","blackfriars","district",2); c("blackfriars","mansion-house","district",1)
        c("mansion-house","cannon-street","district",1); c("cannon-street","monument","district",1)
        c("monument","tower-hill","district",1); c("tower-hill","aldgate-east","district",2)
        c("aldgate-east","whitechapel","district",1); c("whitechapel","mile-end","district",3)
        c("mile-end","west-ham","district",5); c("west-ham","barking","district",7)

        // Waterloo & City
        c("waterloo","bank","waterloo-city",4)

        // H&C shared inner (duplicate on hammersmith-city line)
        c("paddington","edgware-road","hammersmith-city",2); c("edgware-road","baker-street","hammersmith-city",3)
        c("baker-street","great-portland-street","hammersmith-city",2); c("great-portland-street","euston-square","hammersmith-city",1)
        c("euston-square","kings-cross","hammersmith-city",2); c("kings-cross","farringdon","hammersmith-city",3)
        c("farringdon","barbican","hammersmith-city",2); c("barbican","moorgate","hammersmith-city",2)
        c("moorgate","liverpool-street","hammersmith-city",2); c("liverpool-street","aldgate-east","hammersmith-city",2)
        c("aldgate-east","whitechapel","hammersmith-city",1); c("whitechapel","mile-end","hammersmith-city",3)
        c("mile-end","west-ham","hammersmith-city",5); c("west-ham","barking","hammersmith-city",7)

        // Metropolitan outer (Amersham spine)
        c("baker-street","finchley-road","metropolitan",5); c("finchley-road","wembley-park","metropolitan",7)
        c("wembley-park","harrow-on-the-hill","metropolitan",6)
        c("harrow-on-the-hill","north-harrow","metropolitan",2); c("north-harrow","pinner","metropolitan",3)
        c("pinner","northwood-hills","metropolitan",4); c("northwood-hills","northwood","metropolitan",4)
        c("northwood","chorleywood","metropolitan",8); c("chorleywood","chalfont-latimer","metropolitan",4)
        c("chalfont-latimer","amersham","metropolitan",4); c("chalfont-latimer","chesham","metropolitan",8)

        // Metropolitan Uxbridge branch
        c("harrow-on-the-hill","rayners-lane","metropolitan",4)
        c("rayners-lane","eastcote","metropolitan",3); c("eastcote","ruislip-manor","metropolitan",2)
        c("ruislip-manor","ruislip","metropolitan",2); c("ruislip","ickenham","metropolitan",3)
        c("ickenham","hillingdon","metropolitan",2); c("hillingdon","uxbridge","metropolitan",3)
        c("aldgate","liverpool-street","metropolitan",2); c("liverpool-street","moorgate","metropolitan",2)
        c("moorgate","barbican","metropolitan",2); c("barbican","farringdon","metropolitan",2)
        c("farringdon","kings-cross","metropolitan",3); c("kings-cross","euston-square","metropolitan",2)
        c("euston-square","great-portland-street","metropolitan",1); c("great-portland-street","baker-street","metropolitan",2)

        // Bakerloo northern (Paddington → Harrow & Wealdstone)
        c("paddington","warwick-avenue","bakerloo",2); c("warwick-avenue","maida-vale","bakerloo",1)
        c("maida-vale","kilburn-park","bakerloo",2); c("kilburn-park","queens-park","bakerloo",2)
        c("queens-park","kensal-green","bakerloo",2); c("kensal-green","harlesden","bakerloo",3)
        c("harlesden","stonebridge-park","bakerloo",2); c("stonebridge-park","wembley-central","bakerloo",3)
        c("wembley-central","north-wembley","bakerloo",2); c("north-wembley","south-kenton","bakerloo",2)
        c("south-kenton","kenton","bakerloo",2); c("kenton","harrow-wealdstone","bakerloo",3)

        // Northern outer branches
        c("camden-town","chalk-farm","northern",1); c("chalk-farm","hampstead","northern",3)
        c("hampstead","golders-green","northern",3); c("golders-green","edgware","northern",10)

        // Piccadilly outer (Cockfosters branch)
        c("finsbury-park","manor-house","piccadilly",2); c("manor-house","turnpike-lane","piccadilly",2)
        c("turnpike-lane","wood-green","piccadilly",2); c("wood-green","bounds-green","piccadilly",3)
        c("bounds-green","arnos-grove","piccadilly",2); c("arnos-grove","southgate","piccadilly",3)
        c("southgate","oakwood","piccadilly",3); c("oakwood","cockfosters","piccadilly",2)

        // Piccadilly Uxbridge branch
        c("acton-town","rayners-lane","piccadilly",10)
        c("rayners-lane","eastcote","piccadilly",3); c("eastcote","ruislip-manor","piccadilly",2)
        c("ruislip-manor","ruislip","piccadilly",2); c("ruislip","ickenham","piccadilly",3)
        c("ickenham","hillingdon","piccadilly",2); c("hillingdon","uxbridge","piccadilly",3)

        // Elizabeth line
        c("heathrow-t5","heathrow-t123","elizabeth",4)
        c("heathrow-t123","ealing-broadway","elizabeth",18); c("ealing-broadway","paddington","elizabeth",10)
        c("paddington","bond-street","elizabeth",3); c("bond-street","tottenham-court-road","elizabeth",2)
        c("tottenham-court-road","farringdon","elizabeth",3); c("farringdon","liverpool-street","elizabeth",4)
        c("liverpool-street","whitechapel","elizabeth",3); c("whitechapel","canary-wharf","elizabeth",6)
        c("canary-wharf","woolwich","elizabeth",7); c("woolwich","abbey-wood","elizabeth",5)

        return conn
    }
}
