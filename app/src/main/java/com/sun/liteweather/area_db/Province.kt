package com.sun.liteweather.area_db

import org.litepal.crud.DataSupport

data class Province(
        var id:Int=0,
        var provinceName:String?=null,
        var provinceCode:Int=0
):DataSupport()