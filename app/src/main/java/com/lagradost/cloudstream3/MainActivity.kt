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
        
        // --- LOGIKA PERBAIKAN REPO (Agar Tidak 404 & Tidak 0) ---
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        // Memastikan URL mengarah ke RepoPremium (tanpa underscore)
        prefs.edit().apply {
            putString("app_repository_url", "https://raw.githubusercontent.com/aldry84/RepoPremium/main/repo.json")
            putBoolean("is_premium_user", true)
            // Menghapus cache agar aplikasi terpaksa mendownload ulang saat start
            remove("app_repository_cache")
            apply()
        }
        // -------------------------------------------------------

        setContentView(R.layout.activity_main)
    }

    // Fungsi Dialog yang sudah diperbaiki agar tidak berantakan
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
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 20, 0, 20)
        }

        val idLabel = TextView(context).apply {
            text = "ID PERANGKAT ANDA:"
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
        }

        val idText = TextView(context).apply {
            text = deviceId
            textSize = 20f
            setTextColor(android.graphics.Color.parseColor("#FFD700"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 40)
        }

        val inputCode = EditText(context).apply {
            hint = "Masukkan Kode Unlock"
            setHintTextColor(android.graphics.Color.GRAY)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setStroke(2, android.graphics.Color.GRAY)
                cornerRadius = 10f
            }
            setPadding(20, 20, 20, 20)
        }

        val btnUnlock = Button(context).apply {
            text = "ACTIVATE PREMIUM"
            setTextColor(android.graphics.Color.WHITE)
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#E50914"))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 40, 0, 0) }
        }

        btnUnlock.setOnClickListener {
            val code = inputCode.text.toString()
            if (code.isNotEmpty()) {
                (btnUnlock.tag as? AlertDialog)?.dismissSafe()
                
                AlertDialog.Builder(context)
                    .setTitle("Berhasil!")
                    .setMessage("Premium Aktif. Aplikasi akan memulai ulang untuk sinkronisasi.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        // Hapus cache sebelum restart
                        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                            remove("app_repository_cache")
                            apply()
                        }
                        
                        // Restart Paksa
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
        layout.addView(idLabel)
        layout.addView(idText)
        layout.addView(inputCode)
        layout.addView(btnUnlock)

        val alert = AlertDialog.Builder(context)
            .setView(scroll)
            .create()
        
        btnUnlock.tag = alert
        alert.show()
    }
}
