package vcmsa.projects.flappybird

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect

class Bird(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    var x: Int
    var y: Int
    var velocityY: Int = 0
    private val gravity: Int = 2 // How fast the bird accelerates downwards
    private val flapStrength: Int = -25 // How much upward velocity on tap

    private var birdBitmap: Bitmap
    private var birdWidth: Int
    private var birdHeight: Int

    init {
        // Load the bird bitmap
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.flappybird_bird)
        // Scale the bird bitmap
        birdWidth = (screenWidth * 0.08f).toInt() // 8% of screen width
        birdHeight = (birdWidth * (originalBitmap.height.toFloat() / originalBitmap.width.toFloat())).toInt() // Maintain aspect ratio
        birdBitmap = Bitmap.createScaledBitmap(originalBitmap, birdWidth, birdHeight, false)


        // Initial position
        x = screenWidth / 4
        y = screenHeight / 2 - birdHeight / 2
    }

    fun update() {
        velocityY += gravity
        y += velocityY

        // Prevent bird from going off the top of the screen
        if (y < 0) {
            y = 0
            velocityY = 0 // Stop upward movement if it hits the top
        }

        // Game over if bird hits bottom (handled in GameView for now)
        // if (y + birdHeight > screenHeight) {
        //     y = screenHeight - birdHeight
        //     velocityY = 0
        // }
    }

    fun flap() {
        velocityY = flapStrength
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(birdBitmap, x.toFloat(), y.toFloat(), null)
    }

    fun getRect(): Rect {
        return Rect(x, y, x + birdWidth, y + birdHeight)
    }

    fun reset() {
        x = screenWidth / 4
        y = screenHeight / 2 - birdHeight / 2
        velocityY = 0
    }
}