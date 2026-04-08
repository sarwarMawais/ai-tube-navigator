package com.londontubeai.navigator.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") current: String = "true",
        @Query("temperature_unit") unit: String = "celsius",
    ): WeatherResponse
}

data class WeatherResponse(
    @SerializedName("current_weather") val current: CurrentWeather,
)

data class CurrentWeather(
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("windspeed") val windSpeed: Double,
    @SerializedName("weathercode") val weatherCode: Int,
)

// Map OpenMeteo weather codes to our impact levels
fun mapWeatherCodeToImpact(code: Int): com.londontubeai.navigator.ui.screens.home.ImpactLevel = when (code) {
    0, 1 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.LOW        // Clear, mainly clear
    2, 3 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.LOW        // Partly cloudy, overcast
    45, 48 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.MEDIUM    // Fog, depositing rime fog
    51, 53, 55, 56, 57 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.MEDIUM  // Drizzle
    61, 63, 65, 66, 67 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.MEDIUM  // Rain
    71, 73, 75, 77, 85, 86 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.HIGH // Snow
    80, 81, 82 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.HIGH    // Showers
    95, 96, 99 -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.EXTREME // Thunderstorm
    else -> com.londontubeai.navigator.ui.screens.home.ImpactLevel.LOW
}
