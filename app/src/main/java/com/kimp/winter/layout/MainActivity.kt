package com.kimp.winter.layout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.kimp.winter.layout.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var glidImage : DrawableImageViewTarget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        glidImage = DrawableImageViewTarget(binding.gifImage)
    }

    fun start(view: View) {
        binding.winter.startWinter()
        Glide.with(this).load(R.drawable.cat).into(glidImage)
    }

    fun stop(view: View) {
        binding.winter.stopWinter()
    }
}