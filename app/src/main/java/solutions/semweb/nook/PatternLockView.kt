package solutions.semweb.nook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

/** Lightweight 3x3 gesture-pattern input; callers must verify and protect the saved pattern. */
class PatternLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var onPatternCompleted: ((String) -> Unit)? = null
    private val selected = linkedSetOf<Int>()
    private var pointerX = -1f
    private var pointerY = -1f
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF348A63.toInt()
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        isClickable = true
        contentDescription = context.getString(R.string.pattern_lock_description)
    }

    fun clearPattern() {
        selected.clear()
        pointerX = -1f
        pointerY = -1f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val spacing = minOf(width, height) / 3f
        val left = (width - spacing * 3f) / 2f
        val top = (height - spacing * 3f) / 2f
        path.reset()
        selected.forEachIndexed { index, node ->
            val x = left + (node % 3 + 0.5f) * spacing
            val y = top + (node / 3 + 0.5f) * spacing
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        if (pointerX >= 0f && selected.isNotEmpty()) path.lineTo(pointerX, pointerY)
        canvas.drawPath(path, linePaint)
        for (node in 0..8) {
            val x = left + (node % 3 + 0.5f) * spacing
            val y = top + (node / 3 + 0.5f) * spacing
            dotPaint.color = if (node in selected) 0xFF348A63.toInt() else 0xFF8A8A8A.toInt()
            dotPaint.style = Paint.Style.STROKE
            dotPaint.strokeWidth = 4f
            canvas.drawCircle(x, y, spacing * 0.13f, dotPaint)
            if (node in selected) {
                dotPaint.style = Paint.Style.FILL
                canvas.drawCircle(x, y, spacing * 0.07f, dotPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                clearPattern()
                addNodeAt(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                pointerX = event.x
                pointerY = event.y
                addNodeAt(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                addNodeAt(event.x, event.y)
                pointerX = -1f
                pointerY = -1f
                invalidate()
                if (selected.size >= 4) onPatternCompleted?.invoke(selected.joinToString(","))
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                clearPattern()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun addNodeAt(x: Float, y: Float) {
        if (width == 0 || height == 0) return
        val spacing = minOf(width, height) / 3f
        val left = (width - spacing * 3f) / 2f
        val top = (height - spacing * 3f) / 2f
        val radius = spacing * 0.32f
        for (node in 0..8) {
            val cx = left + (node % 3 + 0.5f) * spacing
            val cy = top + (node / 3 + 0.5f) * spacing
            if (hypot(x - cx, y - cy) <= radius) {
                if (node !in selected) selected.add(node)
                return
            }
        }
    }
}
