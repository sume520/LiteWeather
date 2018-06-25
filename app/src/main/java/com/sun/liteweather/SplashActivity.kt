package com.sun.liteweather

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN



class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //启动页实现沉浸
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = option

        Handler().postDelayed({
            var intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        },3000)
    }
}
