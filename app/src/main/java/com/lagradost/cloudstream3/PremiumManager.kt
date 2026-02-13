package com.lagradost.cloudstream3

import android.content.Context
import android.provider.Settings
import androidx.preference.PreferenceManager
import java.security.MessageDigest
import java.util.Locale

object PremiumManager {
    private const val PREF_IS_PREMIUM = "is_premium_user"
    private const val PREF_EXPIRY_DATE = "premium_expiry_date"
    private const val SALT = "ADIXTREAM_SECRET_KEY_2026_SECURE" 

    // URL Repo Anda
    const val PREMIUM_REPO_URL = "https://raw.githubusercontent.com/aldry84/Repo_Premium/refs/heads/builds/repo.json"
    const val FREE_REPO_URL = "https://raw.githubusercontent.com/michat88/Repo_Gratis/refs/heads/builds/repo.json"

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "00000000"
        return androidId.takeLast(8).uppercase()
    }

    /**
     * FUNGSI KRUSIAL: Menentukan URL mana yang harus dimuat oleh aplikasi.
     */
    fun getCurrentRepoUrl(context: Context): String {
        return if (isPremium(context)) PREMIUM_REPO_URL else FREE_REPO_URL
    }

    fun generateUnlockCode(deviceId: String): String {
        return try {
            val input = deviceId + SALT
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.substring(0, 6).uppercase(Locale.getDefault())
        } catch (e: Exception) { "ERROR" }
    }

    fun activatePremiumWithCode(context: Context, code: String, deviceId: String): Boolean {
        val inputCode = code.trim().uppercase(Locale.getDefault())
        val expectedCode = generateUnlockCode(deviceId)

        if (inputCode == expectedCode) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().apply {
                putBoolean(PREF_IS_PREMIUM, true)
                putLong(PREF_EXPIRY_DATE, Long.MAX_VALUE) 
                // Membersihkan cache agar aplikasi memuat ulang repo yang baru
                remove("app_repository_cache") 
                apply()
            }
            return true
        }
        return false
    }

    fun isPremium(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_IS_PREMIUM, false)
    }

    // Fungsi ini ditambahkan kembali agar Build Signed APK tidak error
    fun getExpiryDateString(context: Context): String {
        return if (isPremium(context)) "Lifetime Premium" else "Non-Premium"
    }

    fun deactivatePremium(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().apply {
            putBoolean(PREF_IS_PREMIUM, false)
            putLong(PREF_EXPIRY_DATE, 0)
            apply()
        }
    }
}
