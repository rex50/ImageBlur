package com.rex50.imageblur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import kotlinx.coroutines.*
import kotlin.math.roundToInt

/**
 * An utility class which helps to blur a bitmap in a efficient way
 */
class ImageBlur private constructor(private val context: Context) {

    /**
     * Current scale which will be applied on bitmap
     */
    var scale = 0.3f
        private set

    /**
     * Current bitmap from which a new blurred bitmap will be created
     */
    private var image: Bitmap? = null

    /**
     * Current intensity which will be applied on bitmap
     */
    private var intensity = 8.0f

    /**
     * To allow multiple process of blur or not
     */
    private var allowMultipleTask = false

    /**
     * Callback for OOM exception
     */
    private var onOutOfMemory: (() -> Unit)? = null

    /**
     * Current job(process of blurring a bitmap) running.
     * Will be null if no job is running
     */
    private var lastBlurJob: Job? = null


    private val renderScript: RenderScript by lazy {
        RenderScript.create(context)
    }

    private val asyncScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Main + Job())
    }


    /**
     * Scales and blurs a bitmap based on a given radius using {@link Renderscript}
     *
     * While performing this operation it can throw {@link OutOfMemoryError} which is
     * handled by the function and you can use {@link #onOutOfMemory} for a callback.
     *
     * @return blurred image if operation was successful else returns the original bitmap.
     *
     * Renderscript is going to be deprecated from Android 12 so need to update below code soon
     * to support Android 12 later using below library
     * @see: https://github.com/android/renderscript-intrinsics-replacement-toolkit
     */
    private fun blur(): Bitmap? {
        return try {
            image?.takeIf { intensity != 0.0f }?.let { bitmap ->
                val width = (bitmap.width * scale).roundToInt()
                val height = (bitmap.height * scale).roundToInt()

                val input = Bitmap.createScaledBitmap(bitmap, width, height, false)
                val output = Bitmap.createBitmap(input)

                val intrinsicBlur =
                    ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
                val inputAllocation = Allocation.createFromBitmap(renderScript, input)
                val outputAllocation = Allocation.createFromBitmap(renderScript, output)

                intrinsicBlur.setRadius(intensity)
                intrinsicBlur.setInput(inputAllocation)
                intrinsicBlur.forEach(outputAllocation)
                outputAllocation.copyTo(output)

                output
            } ?: image
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "scale: ", e)
            onOutOfMemory?.invoke()
            image
        }
    }

    /**
     * For loading a bitmap from which a new blurred bitmap will be created
     */
    fun load(bitmap: Bitmap?): ImageBlur {
        image = bitmap
        return this
    }

    /**
     * For loading a resource drawable from which a new blurred bitmap will be created
     *
     * Note: It is recommended to use this method 
     * ONLY FOR SINGLE USE (i.e. only wants to blur a single time).
     */
    fun load(@DrawableRes res: Int): ImageBlur {
        image = getBitmapFromRes(context, res)
        return this
    }

    /**
     * For applying blur intensity
     *
     * @default 8.0
     * @param intensity can be from 0.0 to 25.0
     */
    fun intensity(@FloatRange(from = 0.0, to = 25.0) intensity: Float): ImageBlur {
        this.intensity = if (intensity < MAX_RADIUS && intensity >= 0) intensity else MAX_RADIUS
        return this
    }

    /**
     * For scaling the bitmap
     *
     * @default 0.3
     * @param scale can be from 0.0 to 1.0
     */
    fun scale(@FloatRange(from = 0.2, to = 1.0) scale: Float): ImageBlur {
        this.scale = when {
            scale > MAX_SCALE -> MAX_SCALE
            scale < MIN_SCALE -> MIN_SCALE
            else -> scale
        }
        return this
    }

    /**
     * For allowing multiple process of blurring at a time.
     *
     * It is recommended while using seekbar for blurring a
     * image set allowMultipleTask to false
     *
     * @default false
     * @param allow true for allowing and false for not allowing
     */
    fun allowMultipleTask(allow: Boolean = true): ImageBlur {
        allowMultipleTask = allow
        return this
    }

    /**
     * To get a callback when there was a OOM exception
     * while running blurring process
     *
     * @param callback to get callback
     */
    fun onOutOfMemory(callback: () -> Unit): ImageBlur{
        onOutOfMemory = callback
        return this
    }

    /**
     * To show the blurred image into the ImageView
     * with applied configs.
     * 
     * Also if allowMultipleTask is set to true then this
     * function will cancel last process and start a new process
     * 
     * @see intensity()
     * @see scale()
     *
     * @param imageView to set bitmap
     */
    fun into(imageView: ImageView?) {
        if(!allowMultipleTask && lastBlurJob?.isActive == true)
            lastBlurJob?.cancel(CancellationException("Trying a new image to blur"))

        lastBlurJob = asyncScope.launch {
            try {
                imageView?.let {
                    val bitmap = getBlurredImageAsync()
                    it.setImageBitmap(bitmap)
                }
            } catch (e: CancellationException) {
                Log.w(TAG, "Cancelled last blur job and currently allowMultipleTask is $allowMultipleTask")
            } finally {
                lastBlurJob = null
            }
        }
    }

    /**
     * To get blurred image asynchronously
     * with applied configs
     * @see intensity()
     * @see scale()
     */
    suspend fun getBlurredImageAsync(): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext blur()
    }

    /**
     * To get blurred image
     * with applied configs
     * @see intensity()
     * @see scale()
     */
    fun getBlurredImage(): Bitmap? = blur()


    companion object {

        private const val TAG = "BlurImage"

        private const val MAX_RADIUS = 25.0f
        private const val MIN_SCALE = 0.2f
        private const val MAX_SCALE = 0.9f

        /**
         * To a new instance of BlurImage
         *
         * @param context of the activity or fragment
         * @return new instance of BlurImage
         */
        fun with(context: Context): ImageBlur {
            return ImageBlur(context)
        }
        
        private fun getBitmapFromRes(context: Context, @DrawableRes res: Int): Bitmap? {
            return BitmapFactory.decodeResource(context.resources, res)
        }
    }
}