package com.qdesigns.publiceye.utils

import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtils {
    companion object {
        fun getDateString(time: Long?): String {
            if (time == null) return ""
            //Get instance of calendar
            val calendar = Calendar.getInstance(Locale.getDefault())
            //get current date from time
            calendar.timeInMillis = time
            //return formatted date
            return "at " + android.text.format.DateFormat.format("E, dd MMMM yyyy, HH:mm", calendar)
                .toString()
        }
    }
}