package com.londontubeai.navigator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_journeys")
data class SavedJourneyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromStationId: String,
    val fromStationName: String,
    val toStationId: String,
    val toStationName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavourite: Boolean = false,
)

@Entity(tableName = "cached_line_status")
data class CachedLineStatusEntity(
    @PrimaryKey
    val lineId: String,
    val lineName: String,
    val statusSeverity: Int,
    val statusDescription: String,
    val reason: String?,
    val lastUpdated: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_arrivals")
data class CachedArrivalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stationId: String,
    val lineId: String,
    val lineName: String,
    val platform: String,
    val destination: String,
    val direction: String,
    val timeToStationSeconds: Int,
    val expectedArrival: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_routes")
data class CachedRouteEntity(
    @PrimaryKey
    val routeKey: String, // "fromId_toId"
    val fromStationId: String,
    val fromStationName: String,
    val toStationId: String,
    val toStationName: String,
    val totalDurationMinutes: Int,
    val totalInterchanges: Int,
    val legsSummary: String, // JSON-serialized summary
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val preferLessWalking: Boolean = false,
    val preferLessCrowds: Boolean = true,
    val preferFastestRoute: Boolean = true,
    val preferStepFree: Boolean = false,
    val homeStationId: String? = null,
    val workStationId: String? = null,
    val darkMode: Boolean? = null,
    val favoriteLines: String = "",
    val frequentRoutes: String = "",
    val preferredStations: String = "",
)
