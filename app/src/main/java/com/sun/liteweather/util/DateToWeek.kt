package com.sun.liteweather.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

object DateToWeek {
    fun getWeek(sdate: String?): String? {
        // 再转换为时间
        val date = strToDate(sdate)
        val c = Calendar.getInstance()
        c.setTime(date)
        // int hour=c.get(Calendar.DAY_OF_WEEK);
        // hour中存的就是星期几了，其范围 1~7
        // 1=星期日 7=星期六，其他类推
        val weeken = SimpleDateFormat("EEEE").format(c.getTime())
        return toChineseWeek(weeken)
    }

    fun strToDate(strDate: String?): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val pos = ParsePosition(0)
        return formatter.parse(strDate, pos)
    }

    fun toChineseWeek(eng: String?): String? {
        when (eng) {
            "Monday" -> return "星期一"
            "Tuesday" -> return "星期二"
            "Wednesday" -> return "星期三"
            "Thursday" -> return "星期四"
            "Friday" -> return "星期五"
            "Saturday" -> return "星期六"
            "Sunday" -> return "星期日"
            else -> return eng
        }
    }
}