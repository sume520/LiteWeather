package com.sun.liteweather.util

import android.text.TextUtils
import com.google.gson.Gson
import com.sun.liteweather.area_db.City
import com.sun.liteweather.area_db.County
import com.sun.liteweather.area_db.Province
import com.sun.liteweather.gson.Weather
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonUtil {
    //解析处理省级数据
    fun handleProvinceResponse(response: String): Boolean {
        if (!TextUtils.isEmpty(response)) {
            try {
                val allProvinces = JSONArray(response)
                for (i in 0 until allProvinces.length()) {
                    val provinceObject = allProvinces.getJSONObject(i)
                    val province = Province()
                    province.provinceName = provinceObject.getString("name")
                    province.provinceCode = provinceObject.getInt("id")
                    province.save()
                }
                return true
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return false
    }

    //解析处理市级数据
    fun handleCityResponse(response: String, provinceId: Int): Boolean {
        if (!TextUtils.isEmpty(response)) {
            try {
                val allCities = JSONArray(response)
                for (i in 0 until allCities.length()) {
                    val cityObject = allCities.getJSONObject(i)
                    val city = City()
                    city.cityName = cityObject.getString("name")
                    city.cityCode = cityObject.getInt("id")
                    city.provinceId = provinceId
                    city.save()
                }
                return true
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return false
    }

    //解析处理县级数据
    fun handleCountyResponse(response: String, cityId: Int): Boolean {
        if (!TextUtils.isEmpty(response)) {
            try {
                val allCounties = JSONArray(response)
                for (i in 0 until allCounties.length()) {
                    val countyObject = allCounties.getJSONObject(i)
                    val county = County()
                    county.countyName = countyObject.getString("name")
                    county.weatherId = countyObject.getString("weather_id")
                    county.cityId = cityId
                    county.save()
                }
                return true
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return false
    }

    //解析weather类数据
    fun handleWeatherResponse(response: String): Weather? {
        try {
            val jsonObject = JSONObject(response)
            val jsonArray = jsonObject.getJSONArray("HeWeather")
            val weatherContent = jsonArray.getJSONObject(0).toString()
            return Gson().fromJson<Weather>(weatherContent, Weather::class.java!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}