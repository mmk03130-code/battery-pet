package com.batterypet.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Foreground service drawing the custom battery icon + pet as a non-touchable
 * system overlay (TYPE_APPLICATION_OVERLAY), positioned over the phone's real
 * status-bar battery icon. Taps pass through to the status bar underneath.
 */
class PetOverlayService : Service() {

    companion object {
        const val ACTION_REFRESH = "com.batterypet.app.ACTION_REFRESH"
    }

    private lateinit var windowManager: WindowManager
    private var overlayRoot: LinearLayout? = null
    private lateinit var petView: ImageView
    private lateinit var batteryView: BatteryLevelView
    private var bobAnimator: ObjectAnimator? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                PetKit.prefs(context).edit()
                    .putInt("last_level", pct)
                    .putBoolean("last_charging", charging)
                    .apply()
            }
            refreshPet()
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        petView = ImageView(this).apply {
            val s = PetKit.dp(this@PetOverlayService, 40)
            layoutParams = LinearLayout.LayoutParams(s, s)
        }
        batteryView = BatteryLevelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                PetKit.dp(this@PetOverlayService, 30),
                PetKit.dp(this@PetOverlayService, 16)
            )
        }
        root.addView(petView)
        root.addView(batteryView)

        val prefs = PetKit.prefs(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        if (prefs.getBoolean("overlay_absolute", false)) {
            params.gravity = Gravity.TOP or Gravity.START
            params.x = prefs.getInt("overlay_x", 0)
            params.y = prefs.getInt("overlay_y", 0)
        } else {
            params.gravity = Gravity.TOP or Gravity.END
            params.x = 0
            params.y = 0
        }
        windowManager.addView(root, params)
        overlayRoot = root

        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(
                NotificationChannel("battery_pet", "Battery Pet", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification: Notification = Notification.Builder(this, "battery_pet")
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.overlay_running))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(ACTION_REFRESH)
        }
        val sticky: Intent? = if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(batteryReceiver, filter)
        }
        if (sticky != null) {
            batteryReceiver.onReceive(this, sticky)
        } else {
            refreshPet()
        }

        bobAnimator = ObjectAnimator.ofFloat(petView, "translationY", 0f, -8f).apply {
            duration = 1100
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    private fun refreshPet() {
        val prefs = PetKit.prefs(this)
        val theme = PetKit.themeOf(prefs.getString("theme", "pink"))
        batteryView.setLevel(prefs.getInt("last_level", 100))
        batteryView.setColors(theme.outline, theme.fill)
        petView.setImageResource(PetKit.moodDrawable(this))
        PetWidgetProvider.updateAll(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        bobAnimator?.cancel()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver was never registered; nothing to do.
        }
        overlayRoot?.let { windowManager.removeView(it) }
        overlayRoot = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
