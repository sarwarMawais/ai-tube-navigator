package com.londontubeai.navigator.data.transit

/**
 * City-agnostic transit provider interface.
 * Enables multi-city scalability by abstracting transit data sources.
 *
 * To add a new city:
 * 1. Implement this interface (e.g., NycSubwayProvider, ParisMetroProvider)
 * 2. Register in TransitProviderRegistry
 * 3. Provide city-specific station/line/connection data
 *
 * Current implementations:
 * - LondonTubeProvider (default, backed by TubeData + TfL API)
 */
interface TransitProvider {
    val cityId: String
    val cityName: String
    val countryCode: String
    val timezone: String
    val apiBaseUrl: String?

    fun getStationCount(): Int
    fun getLineCount(): Int
    fun searchStations(query: String): List<TransitStation>
    fun getLineColors(): Map<String, Long> // lineId -> ARGB color
}

data class TransitStation(
    val id: String,
    val name: String,
    val lineIds: List<String>,
    val zone: String,
    val latitude: Double,
    val longitude: Double,
)

data class TransitCity(
    val id: String,
    val name: String,
    val country: String,
    val stationCount: Int,
    val lineCount: Int,
    val isAvailable: Boolean,
    val comingSoon: Boolean = false,
)

object TransitProviderRegistry {
    val availableCities: List<TransitCity> = listOf(
        TransitCity("london", "London", "UK", 272, 12, isAvailable = true),
        TransitCity("nyc", "New York", "US", 472, 26, isAvailable = false, comingSoon = true),
        TransitCity("paris", "Paris", "FR", 303, 16, isAvailable = false, comingSoon = true),
        TransitCity("tokyo", "Tokyo", "JP", 285, 13, isAvailable = false, comingSoon = true),
        TransitCity("berlin", "Berlin", "DE", 175, 10, isAvailable = false, comingSoon = true),
    )

    fun getCity(cityId: String): TransitCity? = availableCities.find { it.id == cityId }
}
