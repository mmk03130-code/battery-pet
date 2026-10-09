package com.batterypet.app

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Lets the user drag the pet + battery icon so it sits exactly over the
 * phone's real status-bar battery icon. Position is saved on exit.
 */
class CalibrationActivity : Activity() {

    private lateinit var dragView: LinearLayout
    private var moved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val prefs = PetKit.prefs(this)

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        dragView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val petView = ImageView(this).apply {
            val s = PetKit.dp(this@CalibrationActivity, 48)
            layoutParams = LinearLayout.LayoutParams(s, s)
            setImageResource(PetKit.moodDrawable(this@CalibrationActivity))
        }
        val batteryView = BatteryLevelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                PetKit.dp(this@CalibrationActivity, 34),
                PetKit.dp(this@CalibrationActivity, 18)
            )
            val theme = PetKit.themeOf(prefs.getString("theme", "pink"))
            setLevel(prefs.getInt("last_level", 72))
            setColors(theme.outline, theme.fill)
        }
        dragView.addView(petView)
        dragView.addView(batteryView)

        val dragLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = PetKit.dp(this@CalibrationActivity, 4)
            rightMargin = PetKit.dp(this@CalibrationActivity, 16)
        }
        root.addView(dragView, dragLp)

        val hint = TextView(this).apply {
            text = getString(R.string.calibrate_hint)
            textSize = 14f
            setTextColor(getColor(R.color.white))
            setBackgroundColor(0xAA000000.toInt())
            gravity = Gravity.CENTER
            val p = PetKit.dp(this@CalibrationActivity, 16)
            setPadding(p, p, p, p)
        }
        val hintLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM }
        root.addView(hint, hintLp)

        var downDX = 0f
        var downDY = 0f
        dragView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downDX = v.x - event.rawX
                    downDY = v.y - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    moved = true
                    val lp = v.layoutParams as FrameLayout.LayoutParams
                    lp.gravity = Gravity.TOP or Gravity.START
                    lp.leftMargin = (event.rawX + downDX).toInt()
                    lp.topMargin = (event.rawY + downDY).toInt()
                    lp.rightMargin = 0
                    v.layoutParams = lp
                    true
                }
                else -> false
            }
        }

        setContentView(root)
    }

    override fun onPause() {
        super.onPause()
        if (moved) {
            val lp = dragView.layoutParams as FrameLayout.LayoutParams
            PetKit.prefs(this).edit()
                .putBoolean("overlay_absolute", true)
                .putInt("overlay_x", lp.leftMargin)
                .putInt("overlay_y", lp.topMargin)
                .apply()
        }
    }
}
