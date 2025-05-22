package vcmsa.projects.flappybird

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.Exception

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var gameThread: Thread? = null
    private var surfaceHolder: SurfaceHolder = holder
    @Volatile
    private var isPlaying: Boolean = false
    internal var isGameOver: Boolean = false

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private lateinit var bird: Bird
    private val towers = mutableListOf<Tower>()
    private val towerSpawnInterval = 2000
    private var lastTowerSpawnTime: Long = 0

    private var score: Int = 0
    private val paintScore: Paint = Paint()
    private val paintGameOver: Paint = Paint()

    private val towerPaint: Paint = Paint() // For drawing towers if not using bitmaps

    init {
        surfaceHolder.addCallback(this)
        isFocusable = true // To receive touch events

        paintScore.color = Color.BLACK
        paintScore.textSize = 80f
        paintScore.textAlign = Paint.Align.LEFT

        paintGameOver.color = Color.RED
        paintGameOver.textSize = 120f
        paintGameOver.textAlign = Paint.Align.CENTER

        towerPaint.color = Color.GREEN // Default tower color
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height

        if (!::bird.isInitialized) { // Initialize bird only once with correct screen dimensions
            bird = Bird(context, screenWidth, screenHeight)
        }
        resetGame() // Start or restart the game state
        startGameThread()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameThread()
    }

    private fun startGameThread() {
        isPlaying = true
        isGameOver = false // Ensure game over is reset
        gameThread = Thread(this)
        gameThread?.start()
    }

    private fun stopGameThread() {
        isPlaying = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    override fun run() {
        var lastFrameTime = System.currentTimeMillis()
        while (isPlaying) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastFrameTime
            lastFrameTime = currentTime

            if (!isGameOver) {
                update(deltaTime)
            }
            drawGame()
            controlFrameRate(currentTime)
        }
    }

    private fun update(deltaTime: Long) {
        bird.update()

        // Spawn towers
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTowerSpawnTime > towerSpawnInterval) {
            towers.add(Tower(screenWidth, screenHeight))
            lastTowerSpawnTime = currentTime
        }

        // Update and remove off-screen towers
        val towersToRemove = mutableListOf<Tower>()
        for (tower in towers) {
            tower.update()
            if (tower.isOffScreen()) {
                towersToRemove.add(tower)
            }

            // Check for scoring
            if (!tower.passed && tower.x + tower.pipeWidth < bird.x) {
                score++
                tower.passed = true
                // Potentially increase difficulty or speed here
            }

            // Collision detection
            if (Rect.intersects(bird.getRect(), tower.getTopRect()) ||
                Rect.intersects(bird.getRect(), tower.getBottomRect())) {
                isGameOver = true
            }
        }
        towers.removeAll(towersToRemove)

        // Check for collision with screen top/bottom
        if (bird.y <= 0) { // Hitting top
            bird.y = 0
            bird.velocityY = 0 // Stop movement
        }
        if (bird.y + bird.getRect().height() >= screenHeight) { // Hitting bottom
            isGameOver = true
        }
    }

    private fun drawGame() {
        if (surfaceHolder.surface.isValid) {
            val canvas: Canvas? = try {
                surfaceHolder.lockCanvas()
            } catch (e: Exception) {
                null
            }

            canvas?.let {
                // Draw background
                it.drawColor(Color.CYAN) // Light blue background

                // Draw towers
                for (tower in towers) {
                    tower.draw(it, towerPaint)
                }

                // Draw bird
                bird.draw(it)

                // Draw score
                it.drawText("Score: $score", 50f, 100f, paintScore)

                // Draw Game Over message
                if (isGameOver) {
                    it.drawText("GAME OVER", (screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), paintGameOver)
                    it.drawText("Tap to Retry", (screenWidth / 2).toFloat(), (screenHeight / 2 + 100).toFloat(), paintScore)
                }
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }
    }

    private fun controlFrameRate(currentTime: Long) {
        // Aim for roughly 60 FPS
        val targetFrameTime = 1000 / 60
        val timeTaken = System.currentTimeMillis() - currentTime
        val sleepTime = targetFrameTime - timeTaken
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isGameOver) {
                    resetGame()
                    isGameOver = false // Start playing again

                } else if (isPlaying) {
                    bird.flap()
                }
                return true // Event handled
            }
        }
        return super.onTouchEvent(event)
    }

    private fun resetGame() {
        if (::bird.isInitialized) bird.reset() // Check if bird is initialized
        towers.clear()
        score = 0
        lastTowerSpawnTime = System.currentTimeMillis() // Reset spawn timer

    }
}