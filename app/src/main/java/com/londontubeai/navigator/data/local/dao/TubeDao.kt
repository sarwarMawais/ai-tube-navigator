package com.londontubeai.navigator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.londontubeai.navigator.data.local.entity.CachedArrivalEntity
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.local.entity.CachedRouteEntity
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TubeDao {

    // Saved Journeys
    @Query("SELECT * FROM saved_journeys ORDER BY timestamp DESC")
    fun getAllSavedJourneys(): Flow<List<SavedJourneyEntity>>

    @Query("SELECT * FROM saved_journeys WHERE isFavourite = 1 ORDER BY timestamp DESC")
    fun getFavouriteJourneys(): Flow<List<SavedJourneyEntity>>

    @Query("SELECT * FROM saved_journeys ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentJourneys(limit: Int = 10): Flow<List<SavedJourneyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: SavedJourneyEntity): Long

    @Update
    suspend fun updateJourney(journey: SavedJourneyEntity)

    @Query("DELETE FROM saved_journeys WHERE id = :id")
    suspend fun deleteJourney(id: Long)

    @Query("DELETE FROM saved_journeys WHERE isFavourite = 0")
    suspend fun clearNonFavouriteJourneys()

    // Cached Line Status
    @Query("SELECT * FROM cached_line_status ORDER BY lineName ASC")
    fun getCachedLineStatuses(): Flow<List<CachedLineStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineStatuses(statuses: List<CachedLineStatusEntity>)

    @Query("DELETE FROM cached_line_status")
    suspend fun clearCachedLineStatuses()

    @Query("SELECT MAX(lastUpdated) FROM cached_line_status")
    suspend fun getLastStatusUpdate(): Long?

    // Cached Arrivals (for offline)
    @Query("SELECT * FROM cached_arrivals WHERE stationId = :stationId ORDER BY timeToStationSeconds ASC")
    suspend fun getCachedArrivals(stationId: String): List<CachedArrivalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedArrivals(arrivals: List<CachedArrivalEntity>)

    @Query("DELETE FROM cached_arrivals WHERE stationId = :stationId")
    suspend fun clearCachedArrivals(stationId: String)

    @Query("DELETE FROM cached_arrivals WHERE cachedAt < :olderThan")
    suspend fun clearStaleArrivals(olderThan: Long)

    // Cached Routes (for offline)
    @Query("SELECT * FROM cached_routes WHERE routeKey = :routeKey")
    suspend fun getCachedRoute(routeKey: String): CachedRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedRoute(route: CachedRouteEntity)

    @Query("DELETE FROM cached_routes WHERE cachedAt < :olderThan")
    suspend fun clearStaleRoutes(olderThan: Long)

    // User Preferences
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(preferences: UserPreferencesEntity)
}
