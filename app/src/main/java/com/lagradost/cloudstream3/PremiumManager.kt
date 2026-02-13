package com.lagradost.cloudstream3

import android.content.Context
import android.provider.Settings
import androidx.preference.PreferenceManager
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object PremiumManager {
    private const val PREF_IS_PREMIUM = "is_premium_user"
    private const val PREF_EXPIRY_DATE = "premium_expiry_date"
    private const val SALT = "ADIXTREAM_SECRET_KEY_2026_SECURE" 
    private const val EPOCH_YEAR = 2025 

    // URL Repository Anda
    const val PREMIUM_REPO_URL = "https://raw.githubusercontent.com/aldry84/Repo_Premium/refs/heads/builds/repo.json"
    const val FREE_REPO_URL = "https://raw.githubusercontent.com/michat88/Repo_Gratis/refs/heads/builds/repo.json"

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "00000000"
        return abs(androidId.hashCode()).toString().take(8)
    }

    /**
     * Mengambil URL Repo secara dinamis.
     * Inilah kunci agar aplikasi otomatis pindah ke repo Premium.
     */
    fun getCurrentRepoUrl(context: Context): String {
        return if (isPremium(context)) PREMIUM_REPO_URL else FREE_REPO_URL
    }

    fun generateUnlockCode(deviceId: String, daysValid: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysValid)
        val targetDate = calendar.time

        val epochCal = Calendar.getInstance()
        epochCal.set(EPOCH_YEAR, Calendar.JANUARY, 1, 0, 0, 0)
        
        val diffMillis = targetDate.time - epochCal.timeInMillis
        val daysFromEpoch = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
        val dateHex = "%03X".format(daysFromEpoch)

        val signatureInput = "$deviceId$dateHex$SALT"
        val signatureHash = MessageDigest.getInstance("MD5").digest(signatureInput.toByteArray(Charsets.UTF_8))
        val signatureHex = signatureHash.joinToString("") { "%02x".format(it) }.substring(0, 3).uppercase()

        return "$dateHex$signatureHex"
    }

    fun activatePremiumWithCode(context: Context, code: String, deviceId: String): Boolean {
        if (code.length != 6) return false
        val inputCode = code.uppercase()
        val datePartHex = inputCode.substring(0, 3)
        val sigPartHex = inputCode.substring(3, 6)

        val checkInput = "$deviceId$datePartHex$SALT"
        val checkHashBytes = MessageDigest.getInstance("MD5").digest(checkInput.toByteArray(Charsets.UTF_8))
        val expectedSig = checkHashBytes.joinToString("") { "%02x".format(it) }.substring(0, 3).uppercase()

        if (sigPartHex != expectedSig) return false

        try {
            val daysFromEpoch = datePartHex.toInt(16)
            val expiryCal = Calendar.getInstance()
            expiryCal.set(EPOCH_YEAR, Calendar.JANUARY, 1, 0, 0, 0)
            expiryCal.add(Calendar.DAY_OF_YEAR, daysFromEpoch)
            
            val expiryTime = expiryCal.timeInMillis
            if (System.currentTimeMillis() > expiryTime) return false

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().apply {
                putBoolean(PREF_IS_PREMIUM, true)
                putLong(PREF_EXPIRY_DATE, expiryTime)
                // Hapus cache repo lama agar aplikasi memuat repo premium
                remove("app_repository_cache") 
                apply()
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun isPremium(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isPremium = prefs.getBoolean(PREF_IS_PREMIUM, false)
        val expiryDate = prefs.getLong(PREF_EXPIRY_DATE, 0)
        
        if (isPremium) {
            if (System.currentTimeMillis() > expiryDate) {
                deactivatePremium(context)
                return false
            }
            return true
        }
        return false
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
