package vcmsa.projects.flappybird

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.random.Random

class Tower(private val screenWidth: Int, private val screenHeight: Int) {
    var x: Int = screenWidth // Start off-screen to the right
    var topPipeEndY: Int
    var bottomPipeStartY: Int

    companion object {
        const val PIPE_WIDTH_PERCENT = 0.15f // 15% of screen width
        const val GAP_HEIGHT_PERCENT = 0.25f // 25% of screen height, adjust for difficulty
        const val SPEED = 8 // Pixels per frame
    }

    val pipeWidth: Int = (screenWidth * PIPE_WIDTH_PERCENT).toInt()
    private val gapHeight: Int = (screenHeight * GAP_HEIGHT_PERCENT).toInt()

    var passed: Boolean = false // To track if the bird has passed this tower for scoring

    init {
        // Randomize the position of the gap
        val minGapTop = (screenHeight * 0.15f).toInt() // Min 15% from top
        val maxGapBottom = (screenHeight * 0.85f).toInt() // Max 85% from top
        val maxGapTop = maxGapBottom - gapHeight

        topPipeEndY = Random.nextInt(minGapTop, maxGapTop + 1)
        bottomPipeStartY = topPipeEndY + gapHeight
    }

    fun update() {
        x -= SPEED
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.GREEN // Or use a bitmap for towers

        // Draw top pipe
        canvas.drawRect(x.toFloat(), 0f, (x + pipeWidth).toFloat(), topPipeEndY.toFloat(), paint)
        // Draw bottom pipe
        canvas.drawRect(x.toFloat(), bottomPipeStartY.toFloat(), (x + pipeWidth).toFloat(), screenHeight.toFloat(), paint)
    }

    fun isOffScreen(): Boolean {
        return x + pipeWidth < 0
    }

    fun getTopRect(): Rect {
        return Rect(x, 0, x + pipeWidth, topPipeEndY)
    }

    fun getBottomRect(): Rect {
        return Rect(x, bottomPipeStartY, x + pipeWidth, screenHeight)
    }
}