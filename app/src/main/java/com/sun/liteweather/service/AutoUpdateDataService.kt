package com.sun.liteweather.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import com.sun.liteweather.util.HttpUtil
import com.sun.liteweather.util.JsonUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class AutoUpdateDataService:Service(){
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWeather()
        //定时
        val manager=getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val hours=1*60*60*1000//1小时
        val triggerAtTime=SystemClock.elapsedRealtime()+hours
        val i=Intent(this,AutoUpdateDataService::class.java)
        val pi=PendingIntent.getService(this,0,i,0)
        manager.cancel(pi)
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi)
        return super.onStartCommand(intent, flags, startId)
    }

    //更新天气
    private fun updateWeather() {
        val prefs=PreferenceManager.getDefaultSharedPreferences(this)
        val weatherData=prefs.getString("weather_data",null)
        if(weatherData!=null){
            // 有缓存时直接解析天气数据
            val weather = JsonUtil.handleWeatherResponse(weatherData)
            val city_id = weather!!.basic!!.weatherId
            val weatherUrl = "http://guolin.tech/api/weather?cityid=$city_id&key=bc0418b57b2d4918819d3974ac1285d9"
            HttpUtil.sendOkHttpRequest(weatherUrl, object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body().string()
                    val weather = JsonUtil.handleWeatherResponse(responseText)
                    if (weather != null && "ok" == weather.status) {
                        val editor = PreferenceManager.getDefaultSharedPreferences(this@AutoUpdateDataService).edit()
                        editor.putString("weather", responseText)
                        editor.apply()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }
            })
        }
    }

}