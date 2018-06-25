package com.sun.liteweather.gson

class AQI {

    var city: CityAQI? = null

    inner class CityAQI {

        var aqi: String? = null

        var pm25: String? = null

    }

}
