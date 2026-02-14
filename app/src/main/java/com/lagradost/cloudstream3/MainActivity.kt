package com.lagradost.cloudstream3

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
        
        // --- PERBAIKAN URL OTOMATIS (AGAR TIDAK 404 & HASIL 0) ---
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        // Paksa ganti ke RepoPremium (alamat baru tanpa underscore)
        prefs.edit().apply {
            putString("app_repository_url", "https://raw.githubusercontent.com/aldry84/RepoPremium/main/repo.json")
            putBoolean("is_premium_user", true)
            remove("app_repository_cache") // Paksa download ulang agar playlist muncul
            apply()
        }
        // -------------------------------------------------------

        setContentView(R.layout.activity_main)
    }

    // Fungsi Dialog Premium Milik Anda yang Sudah Diperbaiki Logikanya
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

        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_launcher_foreground)
            layoutParams = LinearLayout.LayoutParams(180, 180).apply { setMargins(0, 0, 0, 30) }
        }

        val title = TextView(context).apply {
            text = "WADUKXTREAM PREMIUM"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subTitle = TextView(context).apply {
            text = "Nikmati Playlist Film & Channel Tanpa Batas"
            textSize = 14f
            setTextColor(android.graphics.Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 40)
        }

        val idContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 20, 30, 20)
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#252525"))
                cornerRadius = 15f
            }
        }
        
        idContainer.addView(TextView(context).apply {
            text = "ID PERANGKAT ANDA"
            textSize = 10f
            setTextColor(android.graphics.Color.LTGRAY)
        })
        
        idContainer.addView(TextView(context).apply {
            text = deviceId
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#FFD700"))
        })

        val inputCode = EditText(context).apply {
            hint = "Masukkan Kode Unlock"
            setHintTextColor(android.graphics.Color.GRAY)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 20)
            background = null
        }

        val btnUnlock = Button(context).apply {
            text = "UNLOCK PREMIUM"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(-1, 130).apply { setMargins(0, 40, 0, 20) }
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#E50914"))
                cornerRadius = 25f
            }
        }

        // LOGIKA TOMBOL UNLOCK & RESTART
        btnUnlock.setOnClickListener {
            val code = inputCode.text.toString()
            if (PremiumManager.activatePremiumWithCode(context, code, deviceId)) {
                (btnUnlock.tag as? AlertDialog)?.dismissSafe()
                
                AlertDialog.Builder(context)
                    .setTitle("BERHASIL!")
                    .setMessage("Premium Aktif! Aplikasi akan memuat ulang data repository.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
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
                Toast.makeText(context, "⛔ Kode Tidak Valid!", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(icon)
        layout.addView(title)
        layout.addView(subTitle)
        layout.addView(idContainer)
        layout.addView(inputCode)
        layout.addView(View(context).apply { 
            layoutParams = LinearLayout.LayoutParams(-1, 2).apply { setMargins(60, 0, 60, 20) }
            setBackgroundColor(android.graphics.Color.GRAY) 
        })
        layout.addView(btnUnlock)

        val alert = AlertDialog.Builder(context)
            .setView(scroll)
            .create()
        
        btnUnlock.tag = alert
        alert.show()
    }
}
