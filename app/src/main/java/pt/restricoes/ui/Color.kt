package pt.restricoes.ui

import android.content.Context
import androidx.annotation.ColorRes

object Color {

    fun fromAttr(context: Context,
                 attr: Int,
                 @ColorRes default: Int = android.R.color.black): Int {
        val attrs = intArrayOf(attr)
        val style = context.obtainStyledAttributes(attrs)
        val defaultColor = context.resources.getColor(default)

        return style.getColor(0, defaultColor)
    }
}