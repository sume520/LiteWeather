package com.sun.liteweather.gson

import com.google.gson.annotations.SerializedName

class Basic {

    @SerializedName("city")
    var cityName: String? = null

    @SerializedName("id")
    var weatherId: String? = null

    var update: Update? = null

    inner class Update {

        @SerializedName("loc")
        var updateTime: String? = null

    }

}
