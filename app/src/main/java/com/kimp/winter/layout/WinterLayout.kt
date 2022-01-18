package com.kimp.winter.layout

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toBitmap
import java.lang.Math.PI
import java.lang.Math.sin
import kotlin.random.Random

class WinterLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var snows = arrayListOf<Snow>()
    private var animator: ValueAnimator? = null
    private var minAmplitude = 40
    private var maxAmplitude = 50
    private var minSpeed = 3
    private var maxSpeed = 7
    private var minSize = 20
    private var maxSize = 30
    private var bitmap: Bitmap? = null
    private var paused = true
    private var snowCount = 100
    private var stopInProcess = false

    init {
        setWillNotDraw(false)
        paint.color = Color.BLUE
        paint.style = Paint.Style.FILL

        val a = context.obtainStyledAttributes(attrs, R.styleable.WinterLayout)
        snowCount = a.getInt(R.styleable.WinterLayout_snowCount, 100)
        minAmplitude = a.getInt(R.styleable.WinterLayout_minAmplitude, 40)
        maxAmplitude = a.getInt(R.styleable.WinterLayout_maxAmplitude, 50)
        minSpeed = a.getInt(R.styleable.WinterLayout_minSpeed, 3)
        maxSpeed = a.getInt(R.styleable.WinterLayout_maxSpeed, 7)
        minSize = a.getInt(R.styleable.WinterLayout_minSize, 20)
        maxSize = a.getInt(R.styleable.WinterLayout_maxSize, 30)
        bitmap = a.getDrawable(R.styleable.WinterLayout_snowImage)?.toBitmap()

        a.recycle()
    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)

        if(!paused) {
            snows.forEach {
                it.update()
                it.draw(canvas!!)
            }
        }
    }

    fun isPaused(): Boolean = paused

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        for (i in 0 until snowCount) {
            snows.add(Snow(Snow.Params(width, height, bitmap, minAmplitude, maxAmplitude,
                minSpeed, maxSpeed, minSize, maxSize)))
        }
    }

    fun startWinter(){
        snows.forEach {
            it.start()
        }

        stopInProcess = false

        if(animator?.isRunning == true)
            return


        animator = ValueAnimator.ofFloat(0f, 360f)
        animator?.addUpdateListener {
            invalidate()
        }
        animator?.repeatCount = -1
        animator?.start()
        paused = false
    }

    fun stopWinter(){
        if(stopInProcess)
            return
        stopInProcess = true
        var size = 0
        snows.forEach {
            it.stop {
                size++
                if(size == snowCount) {
                    animator?.cancel()
                    paused = true
                    stopInProcess = false
                    snows.forEach{
                        it.restart()
                    }
                }
            }
        }
    }

    fun stopImmediately(){
        animator?.cancel()
        paused = true
        invalidate()
        animator = null
        snows.forEach {
            it.restart()
        }
    }

    fun setSnowSize(size: Int, bitmap: Bitmap?=null, minAmplitude: Int=40, maxAmplitude: Int=50,
                    minSpeed: Int=3, maxSpeed: Int=7, minSize: Int=20, maxSize: Int = 30){
        snows.clear()
        snowCount = size

        this.bitmap = bitmap
        this.minAmplitude = minAmplitude
        this.maxAmplitude = maxAmplitude
        this.minSpeed = minSpeed
        this.maxSpeed = maxSpeed
        this.minSize = minSize
        this.maxSize = maxSize

    }

}

class Snow(val params: Params) {

    private var positionX = 0f                                      //position of snowflake on X coordinate
    private var positionY = 0f                                      //position of snowflake on Y coordinate
    private var range = Random.nextInt(params.minAmplitude, params.maxAmplitude)          //range for amplitude
    private var speedY = Random.nextInt(params.minSpeed,params.maxSpeed)            //speed on Y coordinate
    private var speedX = Random.nextInt(45, 65)         //speed on X coordinate
    private var startingOffset = Random.nextFloat()                 //for initializing starting point of snowflake
    private var amplitude = params.parentWidth * range/100         // amplitude for sin function, depends on range field
    private var horizontalOffset = Random.nextInt(4,7)  //horizontal offset, for keeping snowflakes on screen
    private var degree = 0                                          //degree for sin function
    private var size = Random.nextInt(params.minSize, params.maxSize)           //size of snow
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var bitmap: Bitmap? = null

    private var stopped = false
    private var destroyed = false
    private var callback: (()-> Unit)?= null

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        positionY = (-Random.nextInt(1, params.parentHeight)).toFloat()
        positionX = params.parentWidth/2f
        if(params.bitmap!=null) {
            bitmap = Bitmap.createScaledBitmap(params.bitmap, size, size, false)
        }
    }

    fun draw(canvas: Canvas){
        if(!destroyed && isOnScreen()) {
            if (bitmap != null)
                canvas.drawBitmap(bitmap!!, positionX, positionY, paint)
            else
                canvas.drawCircle(positionX, positionY, size.toFloat(), paint)
        }
    }

    private fun isOnScreen(): Boolean{
        return positionY > 0 && positionY < params.parentHeight
    }

    fun update(){
        if(destroyed)
            return

        val radians = (PI / amplitude) * degree
        val sin = sin(radians)
        degree++

        positionX = (sin * params.parentWidth/ horizontalOffset * speedX/100 + params.parentWidth*startingOffset).toFloat()
        positionY += speedY

        if(positionY-size > params.parentHeight)
            reset()
    }

    fun stop(callback: (() -> Unit)){
        stopped = true
        this.callback = callback
    }

    private fun reset(){
        if(stopped) {
            destroyed = true
            callback?.invoke()
        }

        positionY = (-size).toFloat()
        speedY = Random.nextInt(params.minSpeed, params.maxSpeed)
        speedX = Random.nextInt(45, 65)
        startingOffset = Random.nextFloat()
        amplitude = params.parentWidth * range / 100
        horizontalOffset = Random.nextInt(4, 7)
        size = Random.nextInt(params.minSize, params.maxSize)
        if (params.bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(params.bitmap, size, size, false)
        }

    }

    fun restart(){
        reset()
        positionY = (-Random.nextInt(1, params.parentHeight)).toFloat()
        positionX = params.parentWidth/2f
    }

    fun start() {
        stopped = false
        callback = null
        destroyed = false
    }

    data class Params(val parentWidth: Int, val parentHeight: Int, val bitmap: Bitmap? = null,
                      val minAmplitude: Int, val maxAmplitude: Int, val minSpeed: Int, val maxSpeed: Int,
                      val minSize: Int, val maxSize: Int)
}
