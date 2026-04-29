package com.londontubeai.navigator.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

data class AppLanguageOption(
    val tag: String?,
    val nativeName: String,
    val englishName: String,
)

object AppLanguageManager {
    val supportedLanguages: List<AppLanguageOption> = listOf(
        AppLanguageOption(tag = null, nativeName = "System default", englishName = "Follow System"),
        AppLanguageOption(tag = "en", nativeName = "English", englishName = "English"),
        AppLanguageOption(tag = "es", nativeName = "Español", englishName = "Spanish"),
        AppLanguageOption(tag = "fr", nativeName = "Français", englishName = "French"),
        AppLanguageOption(tag = "de", nativeName = "Deutsch", englishName = "German"),
        AppLanguageOption(tag = "it", nativeName = "Italiano", englishName = "Italian"),
        AppLanguageOption(tag = "pt", nativeName = "Português", englishName = "Portuguese"),
        AppLanguageOption(tag = "pl", nativeName = "Polski", englishName = "Polish"),
        AppLanguageOption(tag = "ro", nativeName = "Română", englishName = "Romanian"),
        AppLanguageOption(tag = "hi", nativeName = "हिन्दी", englishName = "Hindi"),
        AppLanguageOption(tag = "zh-CN", nativeName = "简体中文", englishName = "Chinese (Simplified)"),
        AppLanguageOption(tag = "ja", nativeName = "日本語", englishName = "Japanese"),
        AppLanguageOption(tag = "ko", nativeName = "한국어", englishName = "Korean"),
        AppLanguageOption(tag = "ar", nativeName = "العربية", englishName = "Arabic"),
    )

    fun applyLanguage(languageTag: String?) {
        val locales = if (languageTag.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun currentLanguageTag(): String? {
        val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return tags.ifBlank { null }
    }

    fun displayName(languageTag: String?): String {
        return supportedLanguages.firstOrNull { it.tag == languageTag }?.nativeName ?: supportedLanguages.first().nativeName
    }
}
