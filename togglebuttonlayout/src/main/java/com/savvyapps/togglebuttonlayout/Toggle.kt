package com.savvyapps.togglebuttonlayout

import android.graphics.drawable.Drawable

/**
 * An item within a [ToggleButtonLayout]
 */
data class Toggle(
    /**
     * The ID provided within the Toggle
     */
    val id: Int
) {

    init {
        if (id == 0) {
            throw IllegalArgumentException("Toggle must have a non-zero id")
        }
    }

    constructor(id: Int, icon: Drawable?, title: CharSequence?) : this(id) {
        this.icon = icon
        this.title = title
    }

    /**
     * The icon drawable inflated by the menu resource or passed during creation
     */
    var icon: Drawable? = null

    /**
     * Optional title
     */
    var title: CharSequence? = null

    /**
     * The selection state of the toggle
     */
    var isSelected: Boolean = false
}
