package com.savvyapps.togglebuttonlayout

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes

internal fun Context.dpToPx(dp: Int): Int {
    return Math.round(dp * this.resources.displayMetrics.density)
}

internal fun Context.getThemeAttrDrawable(@AttrRes attributeDrawable: Int): Drawable? {
    val attrs = intArrayOf(attributeDrawable)
    val ta = this.obtainStyledAttributes(attrs)
    val drawableFromTheme = ta.getDrawable(0)
    ta.recycle()
    return drawableFromTheme
}

internal fun Context.getThemeAttrColor(@AttrRes attributeColor: Int): Int {
    val attrs = intArrayOf(attributeColor)
    val ta = this.obtainStyledAttributes(attrs)
    val color = ta.getColor(0, Color.TRANSPARENT)
    ta.recycle()
    return color
}
