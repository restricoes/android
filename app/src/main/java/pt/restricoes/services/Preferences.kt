package pt.restricoes.services

import android.content.Context
import android.content.SharedPreferences

object Preferences {

    private const val PREFIX = "pt.restricoes"
    private const val DEFAULTS_ID = "$PREFIX.defaults"

    fun Context.defaults(): SharedPreferences {
        return this.getSharedPreferences(DEFAULTS_ID, Context.MODE_PRIVATE)
    }

    fun Context.widgetPreferences(widgetId: Int?): SharedPreferences {
        val id = widgetId ?: "null"
        return this.getSharedPreferences("$PREFIX.widget.$id", Context.MODE_PRIVATE)
    }
}