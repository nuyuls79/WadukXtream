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

    // URL Repo tetap menggunakan milik Anda
    const val PREMIUM_REPO_URL = "https://raw.githubusercontent.com/aldry84/Repo_Premium/refs/heads/builds/repo.json"
    const val FREE_REPO_URL = "https://raw.githubusercontent.com/michat88/Repo_Gratis/refs/heads/builds/repo.json"

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "00000000"
        return androidId.takeLast(8).uppercase()
    }

    // Fungsi ini tetap ada tapi tidak akan terpakai karena sistem sudah "bypass"
    fun generateUnlockCode(deviceId: String): String {
        return try {
            val input = deviceId + SALT
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.substring(0, 6).uppercase(Locale.getDefault())
        } catch (e: Exception) { "ERROR" }
    }

    fun activatePremiumWithCode(context: Context, code: String, deviceId: String): Boolean {
        // Kita buat selalu benar agar tidak ada lagi pesan "Salah/Expired"
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().apply {
            putBoolean(PREF_IS_PREMIUM, true)
            putLong(PREF_EXPIRY_DATE, Long.MAX_VALUE) 
            apply()
        }
        return true
    }

    /**
     * PERUBAHAN UTAMA:
     * Fungsi ini sekarang selalu mengembalikan 'true'.
     * Aplikasi akan langsung menganggap Anda Premium selamanya.
     */
    fun isPremium(context: Context): Boolean {
        return true 
    }

    fun deactivatePremium(context: Context) {
        // Tidak diperlukan lagi tapi tetap dibiarkan agar tidak error saat compile
    }
    
    fun getExpiryDateString(context: Context): String {
        return "Lifetime Premium"
    }
}
