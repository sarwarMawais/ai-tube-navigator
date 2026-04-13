package com.londontubeai.navigator.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.londontubeai.navigator.data.local.dao.TubeDao
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.notifications.DisruptionCheckWorker
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.ui.appicon.AppIconManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsPrefsState(
    // Route preferences
    val preferFastest: Boolean = true,
    val preferLessCrowds: Boolean = true,
    val preferLessWalking: Boolean = false,
    val preferStepFree: Boolean = false,
    val maxWalkingMetres: Int = 500,
    val maxInterchanges: Int = 3,
    // Commute
    val homeStationId: String? = null,
    val homeStationName: String? = null,
    val workStationId: String? = null,
    val workStationName: String? = null,
    // Notifications
    val pushDisruptions: Boolean = true,
    val pushCommute: Boolean = true,
    val pushAiTips: Boolean = false,
    val severeOnly: Boolean = false,
    val quietHours: Boolean = false,
    // Appearance
    val darkMode: String = "system",
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    // Location
    val liveLocation: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val dao: TubeDao,
    private val app: Application,
    private val iconManager: AppIconManager,
) : ViewModel() {

    private val _currentIconId = MutableStateFlow(iconManager.getCurrentIconId())
    val currentIconId = _currentIconId.asStateFlow()

    fun setIcon(id: String) {
        iconManager.setIcon(id)
        _currentIconId.value = id
    }

    // Combine flows in groups of ≤5 (Kotlin combine limit), then merge
    val prefsState = combine(
        prefs.preferFastest,
        prefs.preferLessCrowds,
        prefs.preferLessWalking,
        prefs.preferStepFree,
        prefs.pushDisruptions,
    ) { fastest, crowds, walking, stepFree, disruptions ->
        PartialA(fastest, crowds, walking, stepFree, disruptions)
    }.combine(
        combine(
            prefs.pushCommute,
            prefs.pushAiTips,
            prefs.homeStationId,
            prefs.workStationId,
            prefs.darkMode,
        ) { commute, aiTips, homeId, workId, dark ->
            PartialB(commute, aiTips, homeId, workId, dark)
        }
    ) { a, b ->
        PartialAB(a, b)
    }.combine(
        combine(
            prefs.highContrast,
            prefs.largeText,
            prefs.liveLocation,
            prefs.quietHours,
            prefs.severeOnly,
        ) { contrast, text, loc, quiet, severe ->
            PartialC(contrast, text, loc, quiet, severe)
        }
    ) { ab, c ->
        PartialABC(ab, c)
    }.combine(
        combine(
            prefs.maxWalkingMetres,
            prefs.maxInterchanges,
        ) { walk, inter ->
            PartialD(walk, inter)
        }
    ) { abc, d ->
        SettingsPrefsState(
            preferFastest = abc.ab.a.fastest,
            preferLessCrowds = abc.ab.a.crowds,
            preferLessWalking = abc.ab.a.walking,
            preferStepFree = abc.ab.a.stepFree,
            maxWalkingMetres = d.walk,
            maxInterchanges = d.inter,
            pushDisruptions = abc.ab.a.disruptions,
            pushCommute = abc.ab.b.commute,
            pushAiTips = abc.ab.b.aiTips,
            severeOnly = abc.c.severe,
            quietHours = abc.c.quiet,
            homeStationId = abc.ab.b.homeId,
            homeStationName = abc.ab.b.homeId?.let { TubeData.getStationById(it)?.name },
            workStationId = abc.ab.b.workId,
            workStationName = abc.ab.b.workId?.let { TubeData.getStationById(it)?.name },
            darkMode = abc.ab.b.dark,
            highContrast = abc.c.contrast,
            largeText = abc.c.text,
            liveLocation = abc.c.loc,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsPrefsState())

    // Route
    suspend fun setPreferFastest(v: Boolean) = prefs.setPreferFastest(v)
    suspend fun setPreferLessCrowds(v: Boolean) = prefs.setPreferLessCrowds(v)
    suspend fun setPreferLessWalking(v: Boolean) = prefs.setPreferLessWalking(v)
    suspend fun setPreferStepFree(v: Boolean) = prefs.setPreferStepFree(v)
    suspend fun setMaxWalkingMetres(v: Int) = prefs.setMaxWalkingMetres(v)
    suspend fun setMaxInterchanges(v: Int) = prefs.setMaxInterchanges(v)
    // Commute
    suspend fun setHomeStation(id: String?) = prefs.setHomeStationId(id)
    suspend fun setWorkStation(id: String?) = prefs.setWorkStationId(id)
    // Notifications
    suspend fun setPushDisruptions(v: Boolean) {
        prefs.setPushDisruptions(v)
        val wm = WorkManager.getInstance(app)
        if (v) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work = PeriodicWorkRequestBuilder<DisruptionCheckWorker>(
                15, TimeUnit.MINUTES,
            ).setConstraints(constraints).build()
            wm.enqueueUniquePeriodicWork(
                DisruptionCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                work,
            )
        } else {
            wm.cancelUniqueWork(DisruptionCheckWorker.WORK_NAME)
        }
    }
    suspend fun setPushCommute(v: Boolean) = prefs.setPushCommute(v)
    suspend fun setPushAiTips(v: Boolean) = prefs.setPushAiTips(v)
    suspend fun setSevereOnly(v: Boolean) = prefs.setSevereOnly(v)
    suspend fun setQuietHours(v: Boolean) = prefs.setQuietHours(v)
    // Appearance
    suspend fun setDarkMode(v: String) = prefs.setDarkMode(v)

    suspend fun setHighContrast(v: Boolean) = prefs.setHighContrast(v)
    suspend fun setLargeText(v: Boolean) = prefs.setLargeText(v)
    // Location
    suspend fun setLiveLocation(v: Boolean) = prefs.setLiveLocation(v)

    // Data & Storage
    suspend fun clearCache() {
        dao.clearCachedLineStatuses()
        dao.clearStaleArrivals(Long.MAX_VALUE)
        dao.clearStaleRoutes(Long.MAX_VALUE)
        dao.clearNonFavouriteJourneys()
    }

    suspend fun resetPreferences() {
        prefs.clearAll()
    }

    private data class PartialA(val fastest: Boolean, val crowds: Boolean, val walking: Boolean, val stepFree: Boolean, val disruptions: Boolean)
    private data class PartialB(val commute: Boolean, val aiTips: Boolean, val homeId: String?, val workId: String?, val dark: String)
    private data class PartialC(val contrast: Boolean, val text: Boolean, val loc: Boolean, val quiet: Boolean, val severe: Boolean)
    private data class PartialD(val walk: Int, val inter: Int)
    private data class PartialAB(val a: PartialA, val b: PartialB)
    private data class PartialABC(val ab: PartialAB, val c: PartialC)
}
