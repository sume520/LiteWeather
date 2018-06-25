package com.sun.liteweather

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Toast
import com.sun.liteweather.gson.Weather
import com.sun.liteweather.service.AutoUpdateDataService
import com.sun.liteweather.util.DateToWeek
import com.sun.liteweather.util.HttpUtil.sendOkHttpRequest
import com.sun.liteweather.util.JsonUtil
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.app_bar_weather.*
import kotlinx.android.synthetic.main.aqi.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.forecast_item.view.*
import kotlinx.android.synthetic.main.now.*
import kotlinx.android.synthetic.main.suggestion.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class WeatherActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var weather_id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherData = prefs.getString("weather", null)
        if (weatherData != null) { //从缓存读取天气信息
            val weather = JsonUtil.handleWeatherResponse(weatherData)
            weather_id = weather!!.basic!!.weatherId
            showWeatherInfo(weather)
        } else {//从服务器读取天气数据
            weather_id = intent.getStringExtra("weather_id")
            weather_layout!!.visibility = View.INVISIBLE
            requestWeather(weather_id)
        }
        //下拉刷新
        swip_refresh.setOnRefreshListener { requestWeather(weather_id) }
        swip_refresh.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark)
    }

    //获取天气信息
    private fun requestWeather(weatherId: String?) {
        val weatherUrl = "http://guolin.tech/api/weather?cityid=$weatherId&key=086b13da7bb7426fb738fee13a306c27"
        sendOkHttpRequest(weatherUrl, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val response = response.body().string()
                val weather = JsonUtil.handleWeatherResponse(response)
                runOnUiThread {
                    if (weather != null && "ok" == weather.status) {
                        val editor = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                        editor.putString("weather", response)
                        editor.apply()
                        weather_id = weather.basic!!.weatherId
                        showWeatherInfo(weather)
                    } else {
                        Toast.makeText(this@WeatherActivity, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    }
                    swip_refresh.isRefreshing = false
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@WeatherActivity, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    swip_refresh.isRefreshing = false
                }
            }
        })
    }

    //显示天气界面数据
    private fun showWeatherInfo(weather: Weather) {
        val cityName = weather.basic!!.cityName
        val lastupdateTime = weather.basic!!.update!!.updateTime!!.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
        val degree = weather.now!!.temperature!! + "℃"
        val weatherInfo = weather.now!!.more!!.info
        toolbar_title.text = cityName
        last_update_time.text = lastupdateTime
        tv_degress!!.text = degree
        tv_info!!.text = weatherInfo
        weather_image.setImageResource(selectWeatherImage(weatherInfo))
        forecast_layout!!.removeAllViews()
        for (forecast in weather.forecastList!!) {
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecast_layout, false)
            view.tv_date!!.text = DateToWeek.getWeek(forecast.date)
            view.info_image.setImageResource(selectWeatherImage(forecast.more!!.info))
            view.tv_max!!.text = forecast.temperature!!.max
            view.tv_min!!.text = forecast.temperature!!.min
            forecast_layout!!.addView(view)
        }
        if (weather.aqi != null) {
            tv_aqi!!.text = weather.aqi!!.city!!.aqi
            tv_pm25!!.text = weather.aqi!!.city!!.pm25
            Log.d("tv_aqi ",tv_aqi.text.toString())
            Log.d("aqi ",weather.aqi!!.city!!.aqi)
            Log.d("tv_pm25",tv_pm25.text.toString())
            Log.d("pm25",weather.aqi!!.city!!.pm25)
        }
        val comfort = "舒适度：" + weather.suggestion!!.comfort!!.info!!
        val carWash = "洗车指数：" + weather.suggestion!!.carWash!!.info!!
        val sport = "运行建议：" + weather.suggestion!!.sport!!.info!!
        tv_comfort!!.text = comfort
        tv_car_wash!!.text = carWash
        tv_sport!!.text = sport
        weather_layout!!.visibility = View.VISIBLE
        val intent = Intent(this, AutoUpdateDataService::class.java)
        startService(intent)
    }

    //根据天气情况选择天气图标
    private fun selectWeatherImage(info: String?): Int {
        when (info) {
            "晴" -> return R.drawable.sunny
            "阴" -> return R.drawable.overcast
            "少云" -> return R.drawable.few_clouds
            "多云" -> return R.drawable.cloudy
            "小雨" -> return R.drawable.light_rain
            "中雨" -> return R.drawable.moderate_rain
            "大雨" -> return R.drawable.heavy_rain
            "阵雨" -> return R.drawable.shower_rain
            "雷阵雨" -> return R.drawable.thunder_shower
            else -> return R.drawable.w_unknow
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.weather, menu)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_location->{
                var intent=Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
