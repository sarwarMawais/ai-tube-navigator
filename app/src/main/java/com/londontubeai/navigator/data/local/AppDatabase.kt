package com.londontubeai.navigator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.londontubeai.navigator.data.local.dao.TubeDao
import com.londontubeai.navigator.data.local.entity.CachedArrivalEntity
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.local.entity.CachedRouteEntity
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.local.entity.UserPreferencesEntity

@Database(
    entities = [
        SavedJourneyEntity::class,
        CachedLineStatusEntity::class,
        CachedArrivalEntity::class,
        CachedRouteEntity::class,
        UserPreferencesEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tubeDao(): TubeDao
}
