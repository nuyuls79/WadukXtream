package com.lagradost.cloudstream3.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import android.text.TextUtils
import com.lagradost.cloudstream3.MainActivity.Companion.deleteFileOnExit
import com.lagradost.cloudstream3.services.PackageInstallerService
import com.lagradost.cloudstream3.utils.AppContextUtils.setDefaultFocus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class InAppUpdater {
    companion object {
        // KONFIGURASI REPO ADIXTREAM
        private const val GITHUB_USER_NAME = "michat88"
        private const val GITHUB_REPO = "AdiXtream"

        private const val LOG_TAG = "InAppUpdater"

        // === DATA MODELS ===
        data class GithubAsset(
            @JsonProperty("name") val name: String,
            @JsonProperty("size") val size: Int,
            @JsonProperty("browser_download_url") val browserDownloadUrl: String,
            @JsonProperty("content_type") val contentType: String,
        )

        data class GithubRelease(
            @JsonProperty("tag_name") val tagName: String,
            @JsonProperty("body") val body: String,
            @JsonProperty("assets") val assets: List<GithubAsset>,
            @JsonProperty("target_commitish") val targetCommitish: String,
            @JsonProperty("prerelease") val prerelease: Boolean,
            @JsonProperty("node_id") val nodeId: String
        )

        data class GithubObject(
            @JsonProperty("sha") val sha: String,
            @JsonProperty("type") val type: String,
            @JsonProperty("url") val url: String,
        )

        data class GithubTag(
            @JsonProperty("object") val githubObject: GithubObject,
        )

        data class Update(
            @JsonProperty("shouldUpdate") val shouldUpdate: Boolean,
            @JsonProperty("updateURL") val updateURL: String?,
            @JsonProperty("updateVersion") val updateVersion: String?,
            @JsonProperty("changelog") val changelog: String?,
            @JsonProperty("updateNodeId") val updateNodeId: String?
        )

        private suspend fun Activity.getAppUpdate(): Update {
            return try {
                val settingsManager = PreferenceManager.getDefaultSharedPreferences(this)
                if (settingsManager.getBoolean(
                        getString(R.string.prerelease_update_key),
                        resources.getBoolean(R.bool.is_prerelease)
                    )
                ) {
                    getPreReleaseUpdate()
                } else {
                    getReleaseUpdate()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, Log.getStackTraceString(e))
                Update(false, null, null, null, null)
            }
        }

        // PERBAIKAN LOGIKA PENCARIAN RELEASE
        private suspend fun Activity.getReleaseUpdate(): Update {
            val url = "https://api.github.com/repos/$GITHUB_USER_NAME/$GITHUB_REPO/releases"
            val headers = mapOf("Accept" to "application/vnd.github.v3+json")
            val response = parseJson<List<GithubRelease>>(app.get(url, headers = headers).text)

            val versionRegex = Regex("""(.*?((\d+)\.(\d+)\.(\d+))\.apk)""")
            val versionRegexLocal = Regex("""(.*?((\d+)\.(\d+)\.(\d+)).*)""")

            // Filter hanya rilis resmi (bukan prerelease) dan urutkan berdasarkan versi terbaru
            val latestRelease = response.filter { !it.prerelease }
                .sortedByDescending { rel ->
                    rel.assets.firstOrNull { it.name.endsWith(".apk") }?.name?.let { name ->
                        versionRegex.find(name)?.groupValues?.let {
                            it[3].toInt() * 100_000_000 + it[4].toInt() * 10_000 + it[5].toInt()
                        }
                    } ?: 0
                }.firstOrNull()

            // Cari asset yang benar-benar file APK di dalam rilis tersebut
            val foundAsset = latestRelease?.assets?.firstOrNull { it.name.endsWith(".apk") }
            
            val currentVersion = packageName?.let {
                packageManager.getPackageInfo(it, 0)
            }

            foundAsset?.name?.let { assetName ->
                val foundVersion = versionRegex.find(assetName)
                val shouldUpdate = if (foundAsset.browserDownloadUrl != "" && foundVersion != null) {
                    val localV = currentVersion?.versionName?.let { versionName ->
                        versionRegexLocal.find(versionName)?.groupValues?.let {
                            it[3].toInt() * 100_000_000 + it[4].toInt() * 10_000 + it[5].toInt()
                        }
                    } ?: 0
                    val remoteV = foundVersion.groupValues.let {
                        it[3].toInt() * 100_000_000 + it[4].toInt() * 10_000 + it[5].toInt()
                    }
                    localV < remoteV
                } else false

                return Update(
                    shouldUpdate,
                    foundAsset.browserDownloadUrl,
                    foundVersion?.groupValues?.get(2),
                    latestRelease.body,
                    latestRelease.nodeId
                )
            }
            return Update(false, null, null, null, null)
        }

        private suspend fun Activity.getPreReleaseUpdate(): Update {
            val tagUrl = "https://api.github.com/repos/$GITHUB_USER_NAME/$GITHUB_REPO/git/ref/tags/pre-release"
            val releaseUrl = "https://api.github.com/repos/$GITHUB_USER_NAME/$GITHUB_REPO/releases"
            val headers = mapOf("Accept" to "application/vnd.github.v3+json")
            val response = parseJson<List<GithubRelease>>(app.get(releaseUrl, headers = headers).text)

            val found = response.lastOrNull { rel -> rel.prerelease || rel.tagName == "pre-release" }
            val foundAsset = found?.assets?.firstOrNull { it.name.endsWith(".apk") }

            val tagResponse = parseJson<GithubTag>(app.get(tagUrl, headers = headers).text)

            val shouldUpdate = (getString(R.string.commit_hash).trim().take(7) 
                                != tagResponse.githubObject.sha.trim().take(7))

            return if (foundAsset != null) {
                Update(
                    shouldUpdate,
                    foundAsset.browserDownloadUrl,
                    tagResponse.githubObject.sha.take(10),
                    found.body,
                    found.nodeId
                )
            } else {
                Update(false, null, null, null, null)
            }
        }


        private val updateLock = Mutex()

        private suspend fun Activity.downloadUpdate(url: String): Boolean {
            try {
                val appUpdateName = "AdiXtream_Update"
                val appUpdateSuffix = "apk"

                this.cacheDir.listFiles()?.filter {
                    it.name.startsWith(appUpdateName) && it.extension == appUpdateSuffix
                }?.forEach { deleteFileOnExit(it) }

                val downloadedFile = File.createTempFile(appUpdateName, ".$appUpdateSuffix")
                val sink: BufferedSink = downloadedFile.sink().buffer()

                updateLock.withLock {
                    sink.writeAll(app.get(url).body.source())
                    sink.close()
                    openApk(this, Uri.fromFile(downloadedFile))
                }
                return true
            } catch (e: Exception) {
                return false
            }
        }

        private fun openApk(context: Context, uri: Uri) {
            try {
                uri.path?.let {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        File(it)
                    )
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                        data = contentUri
                    }
                    context.startActivity(installIntent)
                }
            } catch (e: Exception) {
                logError(e)
            }
        }

        suspend fun Activity.runAutoUpdate(checkAutoUpdate: Boolean = true): Boolean {
            val settingsManager = PreferenceManager.getDefaultSharedPreferences(this)

            if (!checkAutoUpdate || settingsManager.getBoolean(getString(R.string.auto_update_key), true)) {
                val update = getAppUpdate()
                if (update.shouldUpdate && update.updateURL != null) {

                    val updateNodeId = settingsManager.getString(getString(R.string.skip_update_key), "")

                    if (update.updateNodeId.equals(updateNodeId) && checkAutoUpdate) {
                        return false
                    }

                    runOnUiThread {
                        try {
                            val currentVersion = packageManager.getPackageInfo(packageName, 0)

                            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
                            builder.setTitle(getString(R.string.new_update_format).format(
                                currentVersion.versionName, update.updateVersion))

                            val logRegex = Regex("\\[(.*?)\\]\\((.*?)\\)")
                            val sanitizedChangelog = update.changelog?.replace(logRegex) { it.groupValues[1] }

                            builder.setMessage(sanitizedChangelog)

                            builder.apply {
                                setPositiveButton(R.string.update) { _, _ ->
                                    if (ApkInstaller.delayedInstaller?.startInstallation() == true) return@setPositiveButton
                                    showToast(R.string.download_started, Toast.LENGTH_LONG)

                                    if (settingsManager.getInt(getString(R.string.apk_installer_key), -1) == -1) {
                                        if (isMiUi()) settingsManager.edit().putInt(getString(R.string.apk_installer_key), 1).apply()
                                    }

                                    when (settingsManager.getInt(getString(R.string.apk_installer_key), 0)) {
                                        0 -> {
                                            val intent = PackageInstallerService.getIntent(this@runAutoUpdate, update.updateURL)
                                            ContextCompat.startForegroundService(this@runAutoUpdate, intent)
                                        }
                                        1 -> {
                                            ioSafe {
                                                if (!downloadUpdate(update.updateURL!!))
                                                    runOnUiThread { showToast(R.string.download_failed, Toast.LENGTH_LONG) }
                                            }
                                        }
                                    }
                                }
                                setNegativeButton(R.string.cancel) { _, _ -> }
                                if (checkAutoUpdate) {
                                    setNeutralButton(R.string.skip_update) { _, _ ->
                                        settingsManager.edit().putString(getString(R.string.skip_update_key), update.updateNodeId ?: "").apply()
                                    }
                                }
                            }
                            builder.show().setDefaultFocus()
                        } catch (e: Exception) { logError(e) }
                    }
                    return true
                }
                return false
            }
            return false
        }

        private fun isMiUi(): Boolean = !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))

        private fun getSystemProperty(propName: String): String? {
            return try {
                val p = Runtime.getRuntime().exec("getprop $propName")
                BufferedReader(InputStreamReader(p.inputStream), 1024).use { it.readLine() }
            } catch (ex: IOException) { null }
        }
    }
}
