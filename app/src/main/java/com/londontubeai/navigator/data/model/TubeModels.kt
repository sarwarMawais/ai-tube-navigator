package com.londontubeai.navigator.data.model

import androidx.compose.ui.graphics.Color

data class TubeLine(
    val id: String,
    val name: String,
    val color: Color,
    val stationIds: List<String>,
    val averageSpeedKmh: Int = 33,
    val peakFrequencyMinutes: Int = 2,
    val offPeakFrequencyMinutes: Int = 5,
    val firstTrain: String = "05:30",
    val lastTrain: String = "00:30",
    val totalLengthKm: Double = 0.0,
)

data class Station(
    val id: String,
    val name: String,
    val lineIds: List<String>,
    val zone: String,
    val latitude: Double,
    val longitude: Double,
    val exits: List<StationExit> = emptyList(),
    val hasStepFreeAccess: Boolean = false,
    val totalCarriages: Int = 8,
    val facilities: Set<StationFacility> = emptySet(),
    val interchangeTimeMinutes: Int = 0,
    val annualPassengers: Double = 0.0,
    val wifiAvailable: Boolean = true,
    val toiletsAvailable: Boolean = false,
    val peakCrowdMultiplier: Float = 1.0f,
)

enum class StationFacility(val label: String, val icon: String) {
    WIFI("WiFi", "wifi"),
    TOILETS("Toilets", "wc"),
    STEP_FREE("Step-free", "accessible"),
    LIFTS("Lifts", "elevator"),
    ESCALATORS("Escalators", "escalator"),
    TAXI_RANK("Taxi Rank", "local_taxi"),
    BUS_STOP("Bus Stop", "directions_bus"),
    BIKE_PARKING("Cycle Parking", "pedal_bike"),
    CASH_MACHINE("Cash Machine", "atm"),
    NATIONAL_RAIL("National Rail", "train"),
    OYSTER_VALIDATOR("Oyster Validator", "contactless"),
}

data class StationExit(
    val id: String,
    val name: String,
    val description: String,
    val bestCarriagePosition: Int,
    val walkingTimeSeconds: Int,
    val isStepFree: Boolean = false,
    val nearbyLandmarks: List<String> = emptyList(),
    val streetName: String = "",
)

data class StationConnection(
    val fromStationId: String,
    val toStationId: String,
    val lineId: String,
    val travelTimeMinutes: Int,
    val distanceKm: Double = 0.0,
)

data class CarriageRecommendation(
    val carriageNumber: Int,
    val exitName: String,
    val timeSavedSeconds: Int,
    val reason: String,
    val confidence: Float = 0.85f,
)

data class LineStatus(
    val lineId: String,
    val lineName: String,
    val lineColor: Color,
    val statusSeverity: Int,
    val statusDescription: String,
    val reason: String? = null,
    val isGoodService: Boolean = true,
)

// Rich detail for a single tube line (computed from TubeData)
data class LineDetail(
    val lineId: String,
    val lineName: String,
    val lineColor: Color,
    val firstStation: String,
    val lastStation: String,
    val stationCount: Int,
    val totalLengthKm: Double,
    val averageSpeedKmh: Int,
    val peakFrequencyMin: Int,
    val offPeakFrequencyMin: Int,
    val firstTrain: String,
    val lastTrain: String,
    val connectingLines: List<ConnectingLine>,
    val interchanges: List<InterchangeInfo>,
    val orderedStations: List<LineStationStop>,
    val stationNames: List<String>,
)

data class ConnectingLine(
    val lineId: String,
    val name: String,
    val color: Color,
)

data class InterchangeInfo(
    val stationName: String,
    val stationId: String,
    val connectingLineNames: List<String>,
    val hasStepFreeAccess: Boolean,
    val zone: String,
)

data class LineStationStop(
    val stationId: String,
    val stationName: String,
    val zone: String,
    val isTerminal: Boolean,
    val hasStepFreeAccess: Boolean,
    val connectingLineNames: List<String>,
) {
    val isInterchange: Boolean get() = connectingLineNames.isNotEmpty()
}

data class NearbyStation(
    val station: Station,
    val distanceKm: Double,
) {
    val walkingMinutes: Int get() = (distanceKm / 0.08).toInt().coerceAtLeast(1) // ~5 km/h
    val displayDistance: String get() = if (distanceKm < 1.0)
        "${(distanceKm * 1000).toInt()}m" else String.format("%.1f km", distanceKm)
}

data class ConnectingStation(
    val station: Station,
    val lineName: String,
    val lineColor: Color,
    val stops: Int,
)

data class CrowdPrediction(
    val stationId: String,
    val timeSlot: String,
    val crowdLevel: CrowdLevel,
    val percentageFull: Int,
    val recommendation: String,
    val bestAlternativeTime: String? = null,
)

enum class CrowdLevel(val label: String, val emoji: String) {
    LOW("Quiet", "🟢"),
    MODERATE("Moderate", "🟡"),
    HIGH("Busy", "🟠"),
    VERY_HIGH("Very Busy", "🔴"),
    EXTREME("Extremely Crowded", "⛔"),
}

enum class TransportMode {
    TUBE, WALKING, BUS
}

data class JourneyRoute(
    val fromStation: Station,
    val toStation: Station,
    val legs: List<JourneyLeg>,
    val totalDurationMinutes: Int,
    val totalInterchanges: Int,
    val totalStops: Int = 0,
    val totalWalkingMinutes: Int = 0,
    val aiTimePredictionMinutes: Int = 0,
    val carriageRecommendation: CarriageRecommendation? = null,
    val crowdPrediction: CrowdPrediction? = null,
    val calorieBurned: Int = 0,
    val co2SavedGrams: Int = 0,
)

data class JourneyLeg(
    val fromStation: Station,
    val toStation: Station,
    val line: TubeLine,
    val durationMinutes: Int,
    val direction: String,
    val intermediateStops: Int,
    val stationIds: List<String> = emptyList(),
    val mode: TransportMode = TransportMode.TUBE,
    val walkingDistanceMeters: Int = 0,
    val walkingDirections: String = "",
    val busRouteNumber: String = "",
    val busStopName: String = "",
    val busAlightStopName: String = "",
    val nextDepartureMinutes: Int = 0,
    val platformNumber: String = "",
)

data class AiInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val actionLabel: String? = null,
    val priority: Int = 0,
    val confidence: Float = 0.8f,
    val isPersonalized: Boolean = false,
    val isLive: Boolean = false,
    val expiresAt: Long? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val userImpact: UserImpact = UserImpact.MEDIUM,
)

enum class InsightType {
    TIME_SAVING,
    CROWD_ALERT,
    DELAY_WARNING,
    CARRIAGE_TIP,
    WEATHER_IMPACT,
    EVENT_ALERT,
    GENERAL,
    
    // Enhanced personalized types
    PERSONAL_ROUTE,
    STATION_HABIT,
    TIME_PREFERENCE,
    DISRUPTION_IMPACT,
    SAVINGS_OPPORTUNITY,
    SOCIAL_INSIGHT,
    LEARNING_PATTERN,
}

enum class UserImpact {
    LOW,      // Nice to know
    MEDIUM,   // Useful information
    HIGH,     // Affects your journey
    CRITICAL, // Urgent action needed
}

data class UserContext(
    val currentLocation: String? = null,
    val frequentRoutes: List<String> = emptyList(),
    val travelTimes: List<Int> = emptyList(),
    val preferredLines: List<String> = emptyList(),
    val lastDestinations: List<String> = emptyList(),
    val timeOfDay: TimeOfDay = TimeOfDay.UNKNOWN,
    val dayType: DayType = DayType.WEEKDAY,
    val weatherCondition: WeatherCondition = WeatherCondition.UNKNOWN,
    val crowdTolerance: CrowdTolerance = CrowdTolerance.MEDIUM,
    val commuteProfile: CommuteProfile? = null,
)

enum class TimeOfDay {
    EARLY_MORNING,    // 5:00-7:00
    MORNING_RUSH,     // 7:00-9:00
    MORNING,          // 9:00-12:00
    AFTERNOON,        // 12:00-17:00
    EVENING_RUSH,     // 17:00-19:00
    EVENING,          // 19:00-22:00
    NIGHT,            // 22:00-5:00
    UNKNOWN,
}

enum class DayType {
    WEEKDAY,
    WEEKEND,
    HOLIDAY,
    UNKNOWN,
}

enum class WeatherCondition {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    FOGGY,
    UNKNOWN,
}

enum class CrowdTolerance {
    LOW,      // Avoid crowds
    MEDIUM,   // Don't mind moderate crowds
    HIGH,     // Comfortable with crowds
}

data class CommuteProfile(
    val homeStation: String,
    val workStation: String,
    val preferredDepartureHour: Int,
    val averageTripsPerWeek: Float,
    val topRoutes: List<FrequentRoute>,
)

data class FrequentRoute(
    val fromStationId: String,
    val fromStationName: String,
    val toStationId: String,
    val toStationName: String,
    val frequency: Int,
    val preferredTime: TimeOfDay,
)

// ── Recent / Personalization Models ──────────────────────────

data class RecentStation(
    val stationId: String,
    val stationName: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1,
) {
    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - visitedAt
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> "${days / 7}w ago"
        }
    }
}

data class StationReview(
    val id: String,
    val stationId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val helpful: Int = 0,
    val tags: List<String> = emptyList(),
)

data class CommunityTip(
    val id: String,
    val stationId: String,
    val tip: String,
    val author: String,
    val upvotes: Int = 0,
    val category: TipCategory = TipCategory.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class TipCategory(val label: String) {
    CROWD_AVOIDANCE("Crowd Tip"),
    PLATFORM_TIP("Platform Tip"),
    EXIT_TIP("Exit Tip"),
    SAFETY("Safety"),
    ACCESSIBILITY("Accessibility"),
    GENERAL("General"),
}

data class JourneySuggestion(
    val destinationStation: Station,
    val estimatedMinutes: Int,
    val interchanges: Int,
    val reason: String,
    val category: SuggestionCategory = SuggestionCategory.POPULAR,
)

enum class SuggestionCategory(val label: String) {
    POPULAR("Popular"),
    QUICK("Quick Route"),
    COMMUTE("Commute"),
    NEARBY("Nearby"),
}

data class CrowdHeatmapEntry(
    val hour: Int,
    val crowdLevel: CrowdLevel,
    val percentageFull: Int,
)

data class StationInsight(
    val title: String,
    val description: String,
    val icon: String,
    val type: StationInsightType = StationInsightType.INFO,
)

enum class StationInsightType { INFO, TIP, WARNING, ALERT }

data class PlatformInfo(
    val lineId: String,
    val lineName: String,
    val lineColor: Color,
    val platformName: String,
    val direction: String,
    val nextTrainMinutes: Int? = null,
)

data class NetworkStats(
    val totalStations: Int,
    val totalLines: Int,
    val totalConnectionsKm: Double,
    val stationsWithStepFree: Int,
    val busiestStation: String,
    val longestLine: String,
)

data class UserPreferences(
    val preferLessWalking: Boolean = false,
    val preferLessCrowds: Boolean = true,
    val preferFastestRoute: Boolean = true,
    val preferStepFree: Boolean = false,
    val homeStationId: String? = null,
    val workStationId: String? = null,
)

data class LiveArrival(
    val lineId: String,
    val lineName: String,
    val lineColor: Color,
    val platform: String,
    val direction: String,
    val destination: String,
    val timeToStationSeconds: Int,
    val currentLocation: String,
    val expectedArrival: String,
) {
    val timeToStationMinutes: Int get() = timeToStationSeconds / 60
    val displayTime: String get() = when {
        timeToStationSeconds < 30 -> "Due"
        timeToStationSeconds < 60 -> "<1 min"
        else -> "${timeToStationMinutes} min"
    }
}

data class StationArrivals(
    val stationId: String,
    val stationName: String,
    val arrivals: List<LiveArrival>,
    val lastUpdated: Long = System.currentTimeMillis(),
) {
    val byLine: Map<String, List<LiveArrival>> get() = arrivals.groupBy { it.lineId }
    val nextTrain: LiveArrival? get() = arrivals.minByOrNull { it.timeToStationSeconds }
}

data class NearbyStopPoint(
    val id: String,
    val name: String,
    val indicator: String?,
    val towards: String?,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int,
)

data class Disruption(
    val category: String,
    val description: String,
    val closureText: String?,
    val type: String,
    val affectedLineIds: List<String> = emptyList(),
    val severity: DisruptionSeverity = DisruptionSeverity.MINOR,
)

enum class DisruptionSeverity { MINOR, MODERATE, SEVERE, CLOSURE }

data class NetworkLiveStatus(
    val lineStatuses: List<LineStatus>,
    val disruptions: List<Disruption>,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isLive: Boolean = true,
) {
    val goodServiceCount: Int get() = lineStatuses.count { it.isGoodService }
    val disruptedCount: Int get() = lineStatuses.count { !it.isGoodService }
    val overallHealth: Float get() = if (lineStatuses.isEmpty()) 1f else goodServiceCount.toFloat() / lineStatuses.size
}
