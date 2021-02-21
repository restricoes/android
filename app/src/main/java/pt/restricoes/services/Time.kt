package pt.restricoes.services

import java.text.SimpleDateFormat
import java.util.*

object Time {

    const val SECOND = 1000L
    const val MINUTE = 60 * SECOND
    const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR

    fun parseDate(str: String?): Date? {
        str ?: return null

        val parser =  SimpleDateFormat("yyyy-MM-dd")
        return parser.parse(str)
    }

    fun parseHour(str: String?): Date? {
        str ?: return null

        val parser =  SimpleDateFormat("HH:mm")
        return parser.parse(str)
    }

    fun formatHour(date: Date): String {
        val formatter =  SimpleDateFormat("HH:mm")
        return formatter.format(date)
    }

    fun startOfDay(date: Date): Date {
        val time = date.time
        return Date(time - (time % DAY))
    }

    fun endOfDay(date: Date): Date {
        val startOfDay = this.startOfDay(date)
        return Date(startOfDay.time + DAY - 1)
    }

    fun startOfDay(calendar: Calendar): Date {
        return Time.startOfDay(calendar.time)
    }

    fun isWeekend(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return (dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY)
    }

    fun dayOfWeek(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "sunday"
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            else -> ""
        }
    }
}