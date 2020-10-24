package com.nessy.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager

//Step 1: design splash screen
//hide actionBar
//make this activity fullscreen
//create bew activity
//run & done

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hiding title bar of this activity
        window.requestFeature(Window.FEATURE_NO_TITLE)
        //making this activity fullscreen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash_screen)

//        4second splash time
        Handler().postDelayed({
//            start main activity
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            finish()
        }, 4000)

    }
}