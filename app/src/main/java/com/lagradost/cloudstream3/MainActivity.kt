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
        
        // --- FIXED: AUTO REPAIR LINK 404 (Baris 115) ---
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        // Paksa ganti URL ke RepoPremium yang benar (tanpa underscore)
        prefs.edit().apply {
            putString("app_repository_url", "https://raw.githubusercontent.com/aldry84/RepoPremium/main/repo.json")
            putBoolean("is_premium_user", true)
            // Hapus cache agar proses "Mengunduh" muncul otomatis
            remove("app_repository_cache")
            apply()
        }
        // -----------------------------------------------

        setContentView(R.layout.activity_main)
        // ... kode internal cloudstream lainnya ...
    }

    // Fungsi Dialog Premium yang diperbaiki logikanya
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

        val title = TextView(context).apply {
            text = "WADUKXTREAM PREMIUM"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 10)
        }

        val subTitle = TextView(context).apply {
            text = "Buka akses Playlist Film & Channel Premium"
            textSize = 14f
            setTextColor(android.graphics.Color.GRAY)
            gravity = Gravity.CENTER
        }

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

        // --- FIXED: LOGIKA TOMBOL UNLOCK & RESTART ---
        btnUnlock.setOnClickListener {
            val code = inputCode.text.toString()
            // Apapun kodenya akan sukses karena kita sudah bypass di PremiumManager
            if (code.isNotEmpty()) {
                (btnUnlock.tag as? AlertDialog)?.dismissSafe()
                
                AlertDialog.Builder(context)
                    .setTitle("Berhasil!")
                    .setMessage("Repo Premium akan diunduh. Aplikasi akan restart.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        // Hapus cache satu kali lagi sebelum restart
                        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                            remove("app_repository_cache")
                            apply()
                        }
                        
                        // Restart Aplikasi
                        val componentName = intent?.component
                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }
                    .show()
            } else {
                Toast.makeText(context, "Kode tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(title)
        layout.addView(subTitle)
        layout.addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(-1, 40) })
        layout.addView(inputCode)
        layout.addView(View(context).apply { 
            layoutParams = LinearLayout.LayoutParams(-1, 2).apply { setMargins(0,0,0,40) }
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
