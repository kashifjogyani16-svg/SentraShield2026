package com.sentra.shield.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom water-wave view used inside the Dynamic Island overlay (both collapsed
 * "pill" and expanded states). Draws 3 layered sine waves for a parallax-like
 * depth effect plus small rising bubbles, clipped to the view's circular/rounded
 * bounds by the XML background outline.
 */
class WaterWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var waterColor: Int = Color.parseColor("#00B4D8")
    private var waterLevel: Float = 0.5f // 0f = empty, 1f = full

    private var phase = 0f
    private val phaseAnimator = ValueAnimator.ofFloat(0f, (2 * PI).toFloat()).apply {
        duration = 2500L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    private data class Bubble(
        var x: Float,
        var y: Float,
        var radius: Float,
        var speed: Float,
        var swayPhase: Float
    )

    private val bubbles = mutableListOf<Bubble>()
    private val random = Random(System.currentTimeMillis())

    private val wavePaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val wavePaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val wavePaint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        applyColor()
    }

    fun setWaterColor(color: Int) {
        waterColor = color
        applyColor()
        invalidate()
    }

    fun setWaterLevel(level: Float) {
        waterLevel = level.coerceIn(0f, 1f)
        invalidate()
    }

    fun start() {
        if (!phaseAnimator.isRunning) phaseAnimator.start()
        if (bubbles.isEmpty()) spawnInitialBubbles()
    }

    fun stop() {
        phaseAnimator.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun applyColor() {
        val a = Color.argb(230, Color.red(waterColor), Color.green(waterColor), Color.blue(waterColor))
        val b = Color.argb(170, Color.red(waterColor), Color.green(waterColor), Color.blue(waterColor))
        val c = Color.argb(120, Color.red(waterColor), Color.green(waterColor), Color.blue(waterColor))
        wavePaint1.color = a
        wavePaint2.color = b
        wavePaint3.color = c
        bubblePaint.color = Color.argb(200, 255, 255, 255)
        glowPaint.color = waterColor
    }

    private fun spawnInitialBubbles() {
        bubbles.clear()
        val count = 6
        repeat(count) {
            bubbles.add(
                Bubble(
                    x = random.nextFloat() * width.coerceAtLeast(1),
                    y = height + random.nextFloat() * height,
                    radius = 2f + random.nextFloat() * 4f,
                    speed = 0.6f + random.nextFloat() * 1.2f,
                    swayPhase = random.nextFloat() * (2 * PI).toFloat()
                )
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            spawnInitialBubbles()
            // radial glow behind the water for a soft luminous edge
            glowPaint.shader = RadialGradient(
                w / 2f, h / 2f, maxOf(w, h) / 1.4f,
                intArrayOf(Color.argb(60, Color.red(waterColor), Color.green(waterColor), Color.blue(waterColor)), Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)

        val baseY = height * (1f - waterLevel)
        drawWaveLayer(canvas, wavePaint3, baseY + 6f, amplitude = 5f, waveLength = width * 0.9f, phaseShift = phase * 0.7f)
        drawWaveLayer(canvas, wavePaint2, baseY + 3f, amplitude = 6f, waveLength = width * 0.65f, phaseShift = phase * 1.1f + 1.5f)
        drawWaveLayer(canvas, wavePaint1, baseY, amplitude = 7f, waveLength = width * 0.5f, phaseShift = phase * 1.4f + 3f)

        drawBubbles(canvas, baseY)
    }

    private fun drawWaveLayer(
        canvas: Canvas,
        paint: Paint,
        baseY: Float,
        amplitude: Float,
        waveLength: Float,
        phaseShift: Float
    ) {
        val path = Path()
        val w = width.toFloat()
        val h = height.toFloat()
        path.moveTo(0f, h)
        path.lineTo(0f, baseY)

        val step = 4
        var x = 0f
        while (x <= w) {
            val y = baseY + amplitude * sin((x / waveLength) * 2 * PI + phaseShift).toFloat()
            path.lineTo(x, y)
            x += step
        }
        path.lineTo(w, baseY)
        path.lineTo(w, h)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawBubbles(canvas: Canvas, waterTopY: Float) {
        val h = height.toFloat()
        for (bubble in bubbles) {
            bubble.y -= bubble.speed
            bubble.x += sin((bubble.y * 0.05f) + bubble.swayPhase) * 0.6f

            if (bubble.y < waterTopY - 2f) {
                bubble.y = h + bubble.radius
                bubble.x = random.nextFloat() * width
            }
            if (bubble.y > waterTopY) {
                canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubblePaint)
            }
        }
    }
}
