package com.batterypet.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Cute custom battery icon: rounded outline, level fill, cap nub on the right.
 */
class BatteryLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var level: Int = 72
    private var outlineColor: Int = 0xFF5A4A5A.toInt()
    private var fillColor: Int = 0xFFFF9EBB.toInt()

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val rect = RectF()

    fun setLevel(value: Int) {
        level = value.coerceIn(0, 100)
        invalidate()
    }

    fun setColors(outline: Int, fill: Int) {
        outlineColor = outline
        fillColor = fill
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = resources.displayMetrics.density
        val stroke = 3f * d
        val capW = 3f * d
        val corner = 4f * d
        val bodyRight = width - capW - stroke / 2f

        // Outline body
        outlinePaint.color = outlineColor
        outlinePaint.strokeWidth = stroke
        rect.set(stroke / 2f, stroke / 2f, bodyRight, height - stroke / 2f)
        canvas.drawRoundRect(rect, corner, corner, outlinePaint)

        // Cap nub on the right
        val capTop = height / 2f - 4f * d
        val capBottom = height / 2f + 4f * d
        rect.set(bodyRight, capTop, width - stroke / 2f, capBottom)
        canvas.drawRoundRect(rect, 2f * d, 2f * d, outlinePaint)

        // Level fill
        val innerLeft = stroke + 2f * d
        val innerRight = bodyRight - 2f * d
        val fillWidth = (innerRight - innerLeft) * (level / 100f)
        if (fillWidth > 0f) {
            fillPaint.color = fillColor
            rect.set(innerLeft, stroke + 2f * d, innerLeft + fillWidth, height - stroke - 2f * d)
            canvas.drawRoundRect(rect, 2f * d, 2f * d, fillPaint)
        }
    }
}
