package com.londontubeai.navigator.ui.appicon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class AppIconOption(
    val id: String,
    val name: String,
    val description: String,
    val gradientStart: Int,
    val gradientMid: Int,
    val gradientEnd: Int,
    val aliasName: String?,
)

@Singleton
class AppIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val PKG = "com.londontubeai.navigator"

        val ALL_ICONS = listOf(
            AppIconOption("classic",    "Classic Blue",  "Original TfL underground blue",    0xFF001D5B.toInt(), 0xFF003688.toInt(), 0xFF0052CC.toInt(), null),
            AppIconOption("midnight",   "Night Sky",     "Stars and crescent moon",          0xFF020812.toInt(), 0xFF080F22.toInt(), 0xFF0C1830.toInt(), ".MainActivityMidnight"),
            AppIconOption("scarlet",    "Pride",         "Celebrate with rainbow colours",   0xFFE40303.toInt(), 0xFF008026.toInt(), 0xFF750787.toInt(), ".MainActivityScarlet"),
            AppIconOption("elizabeth",  "Royal",         "Deep purple with diamond sparkle", 0xFF1A0038.toInt(), 0xFF4A2880.toInt(), 0xFF6848A0.toInt(), ".MainActivityElizabeth"),
            AppIconOption("overground", "Sunrise",       "Golden hour sun rising at dawn",   0xFF3A1A00.toInt(), 0xFFA84000.toInt(), 0xFFEF8010.toInt(), ".MainActivityOverground"),
            AppIconOption("emerald",    "Winter",        "Snowflakes and icy blue skies",    0xFF001830.toInt(), 0xFF003860.toInt(), 0xFF005580.toInt(), ".MainActivityEmerald"),
            AppIconOption("sunset",     "Sunset",        "Fiery warm waves at dusk",         0xFF2C0000.toInt(), 0xFF881800.toInt(), 0xFFEC7020.toInt(), ".MainActivitySunset"),
            AppIconOption("ocean",      "Ocean",         "Deep ocean ripples and waves",     0xFF000E20.toInt(), 0xFF003858.toInt(), 0xFF006898.toInt(), ".MainActivityOcean"),
            AppIconOption("rose",       "Blossom",       "Cherry blossom pink petals",       0xFF500028.toInt(), 0xFF901848.toInt(), 0xFFC03865.toInt(), ".MainActivityRose"),
            AppIconOption("carbon",     "Urban",         "City skyline lit up at night",     0xFF050505.toInt(), 0xFF0E0E14.toInt(), 0xFF18181E.toInt(), ".MainActivityCarbon"),
        )
    }

    fun getCurrentIconId(): String {
        val pm = context.packageManager
        for (icon in ALL_ICONS) {
            val alias = icon.aliasName ?: continue
            val cn = ComponentName(PKG, PKG + alias)
            val state = pm.getComponentEnabledSetting(cn)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) return icon.id
        }
        return "classic"
    }

    fun setIcon(iconId: String) {
        val pm = context.packageManager
        val currentId = getCurrentIconId()
        if (currentId == iconId) return

        val newIcon = ALL_ICONS.firstOrNull { it.id == iconId } ?: return

        // Disable whatever is currently active
        if (currentId == "classic") {
            pm.setComponentEnabledSetting(
                ComponentName(PKG, "$PKG.MainActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        } else {
            val currentAlias = ALL_ICONS.first { it.id == currentId }.aliasName!!
            pm.setComponentEnabledSetting(
                ComponentName(PKG, PKG + currentAlias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }

        // Enable the new icon
        if (newIcon.aliasName == null) {
            pm.setComponentEnabledSetting(
                ComponentName(PKG, "$PKG.MainActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        } else {
            pm.setComponentEnabledSetting(
                ComponentName(PKG, PKG + newIcon.aliasName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}
