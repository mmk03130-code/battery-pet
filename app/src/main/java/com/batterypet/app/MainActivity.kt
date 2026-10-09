package com.batterypet.app

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PetKit.prefs(this)

        buildPetRow()
        buildThemeRow()

        val overlaySwitch = findViewById<Switch>(R.id.overlay_switch)
        overlaySwitch.isChecked = prefs.getBoolean("overlay_enabled", false)
        overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    overlaySwitch.isChecked = false
                    Toast.makeText(this, getString(R.string.overlay_permission_hint), Toast.LENGTH_LONG).show()
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                    return@setOnCheckedChangeListener
                }
                prefs.edit().putBoolean("overlay_enabled", true).apply()
                startForegroundService(Intent(this, PetOverlayService::class.java))
            } else {
                prefs.edit().putBoolean("overlay_enabled", false).apply()
                stopService(Intent(this, PetOverlayService::class.java))
            }
        }

        findViewById<Button>(R.id.btn_calibrate).setOnClickListener {
            startActivity(Intent(this, CalibrationActivity::class.java))
        }

        findViewById<Button>(R.id.btn_widget).setOnClickListener {
            val manager = getSystemService(AppWidgetManager::class.java)
            val provider = ComponentName(this, PetWidgetProvider::class.java)
            if (manager.isRequestPinAppWidgetSupported) {
                manager.requestPinAppWidget(provider, null, null)
            } else {
                Toast.makeText(this, getString(R.string.widget_hint), Toast.LENGTH_LONG).show()
            }
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-sync in case the user granted overlay permission in Settings.
        findViewById<Switch>(R.id.overlay_switch).isChecked =
            prefs.getBoolean("overlay_enabled", false)
        buildPetRow()
        buildThemeRow()
    }

    private fun buildPetRow() {
        val row = findViewById<LinearLayout>(R.id.pet_row)
        row.removeAllViews()
        val selected = prefs.getString("pet", "bunny")
        val pad = PetKit.dp(this, 10)
        for (pet in PetKit.PETS) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    PetKit.dp(this@MainActivity, 84),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val m = PetKit.dp(this@MainActivity, 6)
                    setMargins(m, m, m, m)
                }
                setPadding(pad, pad, pad, pad)
                background = getDrawable(
                    if (pet == selected) R.drawable.bg_card_selected else R.drawable.bg_card
                )
                isClickable = true
                isFocusable = true
            }
            val image = ImageView(this).apply {
                val s = PetKit.dp(this@MainActivity, 56)
                layoutParams = LinearLayout.LayoutParams(s, s)
                setImageResource(PetKit.petDrawable(pet, "normal"))
            }
            val label = TextView(this).apply {
                text = pet.replaceFirstChar { it.uppercaseChar() }
                textSize = 12f
                setTextColor(getColor(R.color.ink))
                gravity = Gravity.CENTER
            }
            card.addView(image)
            card.addView(label)
            card.setOnClickListener {
                prefs.edit().putString("pet", pet).apply()
                buildPetRow()
                sendRefresh()
            }
            row.addView(card)
        }
    }

    private fun buildThemeRow() {
        val row = findViewById<LinearLayout>(R.id.theme_row)
        row.removeAllViews()
        val selected = prefs.getString("theme", "pink")
        val pad = PetKit.dp(this, 10)
        for (theme in PetKit.THEMES) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    PetKit.dp(this@MainActivity, 104),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val m = PetKit.dp(this@MainActivity, 6)
                    setMargins(m, m, m, m)
                }
                setPadding(pad, pad, pad, pad)
                background = getDrawable(
                    if (theme.key == selected) R.drawable.bg_card_selected else R.drawable.bg_card
                )
                isClickable = true
                isFocusable = true
            }
            val preview = BatteryLevelView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    PetKit.dp(this@MainActivity, 56),
                    PetKit.dp(this@MainActivity, 30)
                )
                setLevel(72)
                setColors(theme.outline, theme.fill)
            }
            val label = TextView(this).apply {
                text = theme.label
                textSize = 12f
                setTextColor(getColor(R.color.ink))
                gravity = Gravity.CENTER
            }
            card.addView(preview)
            card.addView(label)
            card.setOnClickListener {
                prefs.edit().putString("theme", theme.key).apply()
                buildThemeRow()
                sendRefresh()
                Toast.makeText(this, theme.label + " selected", Toast.LENGTH_SHORT).show()
            }
            row.addView(card)
        }
    }

    private fun sendRefresh() {
        sendBroadcast(Intent(PetOverlayService.ACTION_REFRESH).setPackage(packageName))
        PetWidgetProvider.updateAll(this)
    }
}
