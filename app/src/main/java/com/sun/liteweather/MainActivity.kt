package com.sun.liteweather

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs=PreferenceManager
                .getDefaultSharedPreferences(this@MainActivity)
        //判断有无读取到位置信息
        if(prefs.getString("weather",null)!=null){
            val intent= Intent(this,WeatherActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }
}
