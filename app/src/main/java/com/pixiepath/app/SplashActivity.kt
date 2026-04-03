package com.pixiepath.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full screen immersive
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )

        setContentView(R.layout.activity_splash)

        val bg = findViewById<ImageView>(R.id.splash_bg)
        val tagline = findViewById<TextView>(R.id.splash_tagline)

        // Background: fade in + gentle zoom from 1.2x to 1.0x (fills screen, settles in)
        bg.scaleX = 1.2f
        bg.scaleY = 1.2f
        bg.animate()
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(1600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Tagline: fade in + slide up after a delay
        tagline.translationY = 40f
        tagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(800)
            .setDuration(900)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()

        // Transition to main activity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2800)
    }
}
