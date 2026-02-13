package com.lagradost.cloudstream3

// ... (semua import tetap sama, saya pastikan tidak ada yang hilang)
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.ui.result.setSafeOnLongClickListener
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- PERBAIKAN OTOMATIS UNTUK LINK 404 (Line 115) ---
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val currentRepo = prefs.getString("app_repository_url", "")
        
        // Jika link masih yang lama (404) atau kosong, paksa ganti ke RepoPremium
        if (currentRepo.isNullOrEmpty() || currentRepo.contains("Repo_Premium")) {
            prefs.edit().apply {
                putString("app_repository_url", "https://raw.githubusercontent.com/aldry84/RepoPremium/main/repo.json")
                putBoolean("is_premium_user", true)
                remove("app_repository_cache") // Paksa download ulang
                apply()
            }
        }
        // ---------------------------------------------------

        setContentView(R.layout.activity_main)
        // ... (Sisa kode onCreate Anda tetap sama sampai bawah)
    }

    // ... (Fungsi-fungsi lain tetap sama)

    fun showPremiumDialog(context: Context) {
        val deviceId = PremiumManager.getDeviceId(context)
        val scroll = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#1A1A1A"))
                cornerRadius = 40f
            }
        }
        scroll.addView(layout)

        // Title & UI UI lainnya ... (Tetap sama seperti file asli Anda)
        
        val inputCode = EditText(context).apply {
            hint = "Masukkan Kode Unlock"
            setHintTextColor(android.graphics.Color.GRAY)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            background = null
        }

        val btnUnlock = Button(context).apply {
            text = "UNLOCK NOW"
            setTextColor(android.graphics.Color.WHITE)
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#E50914"))
                cornerRadius = 20f
            }
        }

        // --- PERBAIKAN TOMBOL UNLOCK (RESTART SISTEM) ---
        btnUnlock.setOnClickListener {
            val code = inputCode.text.toString()
            if (PremiumManager.activatePremiumWithCode(context, code, deviceId)) {
                (btnUnlock.tag as? AlertDialog)?.dismissSafe()
                
                AlertDialog.Builder(context)
                    .setTitle("Premium Berhasil!")
                    .setMessage("Repo Premium sedang disinkronkan. Aplikasi akan memuat ulang.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        // Hapus cache agar saat restart dia mendownload dari link baru
                        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                            putString("app_repository_url", "https://raw.githubusercontent.com/aldry84/RepoPremium/main/repo.json")
                            remove("app_repository_cache")
                            apply()
                        }
                        
                        // RESTART PAKSA
                        val componentName = intent?.component
                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }
                    .show()
            } else {
                Toast.makeText(context, "⛔ Kode Salah atau Expired!", Toast.LENGTH_SHORT).show()
            }
        }

        // Layouting (Sisa kode menyusun layout tetap sama)
        // ...
    }
}
