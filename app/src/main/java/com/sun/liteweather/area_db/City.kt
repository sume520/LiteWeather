package com.sun.liteweather.area_db

import org.litepal.crud.DataSupport

data class City(
        var id:Int=0,
        var cityName:String?=null,
        var cityCode:Int=0,
        var provinceId:Int=0
): DataSupport()