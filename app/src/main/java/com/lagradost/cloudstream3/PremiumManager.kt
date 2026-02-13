package com.lagradost.cloudstream3

import android.content.Context
import android.provider.Settings
import androidx.preference.PreferenceManager
import java.security.MessageDigest
import java.util.Locale

object PremiumManager {
    private const val PREF_IS_PREMIUM = "is_premium_user"
    private const val PREF_EXPIRY_DATE = "premium_expiry_date"
    
    // SALT pengunci tetap
    private const val SALT = "ADIXTREAM_SECRET_KEY_2026_SECURE" 

    const val PREMIUM_REPO_URL = "https://raw.githubusercontent.com/aldry84/Repo_Premium/refs/heads/builds/repo.json"
    const val FREE_REPO_URL = "https://raw.githubusercontent.com/michat88/Repo_Gratis/refs/heads/builds/repo.json"

    // Mengambil ID perangkat tanpa proses hashCode yang rumit
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "00000000"
        return androidId.takeLast(8).uppercase()
    }

    /**
     * GENERATE CODE (Hanya untuk Admin)
     */
    fun generateUnlockCode(deviceId: String): String {
        return try {
            val input = deviceId + SALT
            val md = MessageDigest.getInstance("MD5")
            // Menggunakan Charsets.UTF_8 agar hasil MD5 selalu konsisten
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            
            bytes.joinToString("") { "%02x".format(it) }
                .substring(0, 6)
                .uppercase(Locale.getDefault())
        } catch (e: Exception) {
            "ERROR"
        }
    }

    /**
     * AKTIVASI PERMANEN
     */
    fun activatePremiumWithCode(context: Context, code: String, deviceId: String): Boolean {
        val inputCode = code.trim().uppercase(Locale.getDefault())
        val expectedCode = generateUnlockCode(deviceId)

        if (inputCode == expectedCode) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().apply {
                putBoolean(PREF_IS_PREMIUM, true)
                // Menggunakan nilai maksimal agar status premium tidak pernah mati
                putLong(PREF_EXPIRY_DATE, Long.MAX_VALUE) 
                apply()
            }
            return true
        }
        return false
    }

    // Fungsi isPremium sekarang hanya mengecek boolean saja, tidak mengecek waktu
    fun isPremium(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_IS_PREMIUM, false)
    }

    fun deactivatePremium(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().apply {
            putBoolean(PREF_IS_PREMIUM, false)
            putLong(PREF_EXPIRY_DATE, 0)
            apply()
        }
    }
    
    fun getExpiryDateString(context: Context): String {
        return if (isPremium(context)) "Lifetime Premium" else "Non-Premium"
    }
}
