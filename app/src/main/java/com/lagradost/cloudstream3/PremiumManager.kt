package com.lagradost.cloudstream3

import android.content.Context
import android.provider.Settings
import androidx.preference.PreferenceManager

object PremiumManager {
    private const val PREF_IS_PREMIUM = "is_premium_user"
    
    const val PREMIUM_REPO_URL = "https://raw.githubusercontent.com/aldry84/Repo_Premium/refs/heads/builds/repo.json"
    const val FREE_REPO_URL = "https://raw.githubusercontent.com/michat88/Repo_Gratis/refs/heads/builds/repo.json"

    fun isPremium(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_IS_PREMIUM, false)
    }

    // Menentukan URL mana yang harus didownload aplikasi
    fun getCurrentRepoUrl(context: Context): String {
        return if (isPremium(context)) PREMIUM_REPO_URL else FREE_REPO_URL
    }

    fun activatePremiumWithCode(context: Context, code: String, deviceId: String): Boolean {
        if (code.isBlank()) return false
        
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().apply {
            putBoolean(PREF_IS_PREMIUM, true)
            // HAPUS SEMUA CACHE REPO LAMA
            remove("app_repository_cache")
            remove("plugins_cache") 
            apply()
        }
        return true
    }

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "00000000"
        return androidId.takeLast(8).uppercase()
    }

    fun getExpiryDateString(context: Context): String {
        return if (isPremium(context)) "Lifetime Premium" else "Non-Premium"
    }
}
