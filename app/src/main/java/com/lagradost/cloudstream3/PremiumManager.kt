package com.lagradost.cloudstream3

import android.content.Context
import androidx.preference.PreferenceManager

object PremiumManager {
    // URL TERBARU: Sudah disesuaikan dengan perubahan nama repo pemiliknya
    const val PREMIUM_REPO_URL = "https://raw.githubusercontent.com/nuyuls79/Nontonmovies/refs/heads/builds/repo.json"
    const val FREE_REPO_URL = PREMIUM_REPO_URL 

    fun isPremium(context: Context): Boolean = true

    fun getDeviceId(context: Context): String {
        val androidId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "00000000"
        return androidId.takeLast(8).uppercase()
    }

    fun getExpiryDateString(context: Context): String = "Lifetime Access"
    
    fun activatePremiumWithCode(context: Context, code: String, deviceId: String): Boolean {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("is_premium_user", true).apply()
        return true
    }
}
