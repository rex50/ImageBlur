package com.rex50.sample


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.rex50.imageblur.ImageBlur
import com.rex50.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private val image: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.mipmap.img_sample)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.sbBlur?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyBlurToImageView(progress.toFloat()/4)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

    }

    private fun applyBlurToImageView(intensity: Float) {
        ImageBlur.with(this)
            .load(image) //Bitmap from which blurred image will be created
            .intensity(intensity) //Blur intensity
            .scale(0.5f) //Scale intensity
            .into(binding?.ivPreview) //Directly apply to image view
    }
}