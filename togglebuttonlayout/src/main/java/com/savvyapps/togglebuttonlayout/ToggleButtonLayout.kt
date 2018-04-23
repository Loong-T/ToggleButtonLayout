package com.savvyapps.togglebuttonlayout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.MenuInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * ToggleButtonLayout is a layout used to group related options. Layout and spacing is arranged to
 * convey that certain toggle buttons are part of a group.
 */
class ToggleButtonLayout : CardView {

    companion object {

        /**
         * Toggles will wrap content. Default
         */
        const val MODE_WRAP = 0

        /**
         * Toggles will be evenly distributed within view
         */
        const val MODE_EVEN = 1
    }

    private lateinit var linearLayout: LinearLayout

    private val toggles = mutableListOf<Toggle>()

    //customization
    private var multipleSelection: Boolean = false
    private var allowDeselection = true
    @ColorInt
    private var dividerColor: Int? = null
    @ColorInt
    private var selectedColor: Int? = null
    private var selectedColors: Array<Drawable>? = null
    private var textColors: Array<ColorStateList>? = null
    @LayoutRes
    private var layoutRes: Int? = null
    private var mode: Int = 0

    private val onClickListener = OnClickListener { v ->
        val toggle = v.getTag(R.id.tb_toggle_id) as Toggle
        val currentSelection = toggle.isSelected
        // if allowing deselection and currently selected, do nothing. Could invert it, but
        // it makes more sense to me this way
        if (!allowDeselection && currentSelection) {
            //do nothing
        } else {
            setToggled(toggle.id, !toggle.isSelected)
            onToggledListener?.invoke(toggle, toggle.isSelected)
        }
    }

    /**
     * Listen for when toggles get selected and deselected
     */
    var onToggledListener: ((toggle: Toggle, selected: Boolean) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        linearLayout = LinearLayout(context)
        addView(linearLayout)

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleButtonLayout,
                0, 0)

        if (a.hasValue(R.styleable.ToggleButtonLayout_multipleSelection)) {
            multipleSelection = a.getBoolean(R.styleable.ToggleButtonLayout_multipleSelection, false)
        }
        if (a.hasValue(R.styleable.ToggleButtonLayout_allowDeselection)) {
            allowDeselection = a.getBoolean(R.styleable.ToggleButtonLayout_allowDeselection, true)
        }
        if (a.hasValue(R.styleable.ToggleButtonLayout_dividerColor)) {
            dividerColor = a.getColor(R.styleable.ToggleButtonLayout_dividerColor, Color.GRAY)
        }
        if (a.hasValue(R.styleable.ToggleButtonLayout_customLayout)) {
            layoutRes = a.getResourceId(R.styleable.ToggleButtonLayout_customLayout, 0)
        }
        if (a.hasValue(R.styleable.ToggleButtonLayout_toggleMode)) {
            val mode = a.getInt(R.styleable.ToggleButtonLayout_toggleMode, MODE_WRAP)
            this.mode = mode
        }

        if (a.hasValue(R.styleable.ToggleButtonLayout_selectedColors)) {
            val resId = a.getResourceId(R.styleable.ToggleButtonLayout_selectedColors, -1)
            if (resId != -1) {
                val ta = resources.obtainTypedArray(resId)
                selectedColors = Array(ta.length()) { ta.getDrawable(it) }
                ta.recycle()
            }
        }
        selectedColor = a.getColor(R.styleable.ToggleButtonLayout_selectedColor,
            Utils.getThemeAttrColor(getContext(), R.attr.colorControlHighlight))

        if (a.hasValue(R.styleable.ToggleButtonLayout_textColors)) {
            val resId = a.getResourceId(R.styleable.ToggleButtonLayout_textColors, 0)
            if (resId != -1) {
                val ta = resources.obtainTypedArray(resId)
                textColors = Array(ta.length()) { ta.getColorStateList(it) }
                ta.recycle()
            }
        }

        //make sure this one is last
        if (a.hasValue(R.styleable.ToggleButtonLayout_menu)) {
            inflateMenu(a.getResourceId(R.styleable.ToggleButtonLayout_menu, 0))
        }
        a.recycle()
    }

    /**
     * Add actions to the layout from the given menu resource id.
     *
     * @param menuId menu resource id
     */
    @SuppressLint("RestrictedApi")
    fun inflateMenu(@MenuRes menuId: Int) {
        val menu = MenuBuilder(context)
        MenuInflater(context).inflate(menuId, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val toggle = Toggle(item.itemId, item.icon, item.title)
            addToggle(toggle)
        }
    }

    /**
     * Get a list of selected toggles
     *
     * @return the selected toggles
     */
    fun getSelectedToggles(): List<Toggle> {
        return toggles.filter { it.isSelected }
    }

    /**
     * Add a toggle to the layout
     *
     * @param toggle the toggle to add
     */
    fun addToggle(toggle: Toggle) {
        toggles.add(toggle)
        val toggleView = ToggleView(context, toggle, layoutRes)
        toggleView.setOnClickListener(onClickListener)
        if (dividerColor != null && toggles.size > 1) {
            val divider = View(context)
            divider.setBackgroundColor(dividerColor!!)
            val params = LinearLayout.LayoutParams(Utils.dpToPx(context, 1), ViewGroup.LayoutParams.MATCH_PARENT)
            divider.layoutParams = params
            linearLayout.addView(divider)
        }
        if (mode == MODE_EVEN) {
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            toggleView.layoutParams = params
        }
        val idx = toggles.indexOf(toggle)
        if (textColors != null && idx < textColors!!.size) {
            toggleView.textView?.setTextColor(textColors!![idx])
        }
        linearLayout.addView(toggleView)
    }

    /**
     * Set if we are going to allow multiple selection or not. This will also call [.reset]
     * in order to prevent strange behaviour switching between multiple and single selection
     *
     * @param multipleSelection true if allowing multiple selection, false otherwise
     */
    fun setMultipleSelection(multipleSelection: Boolean) {
        this.multipleSelection = multipleSelection
        reset()
    }

    /**
     * Allow selected items to be de-selected. Defaults to true.
     *
     * @param allowDeselection allow de-selection
     */
    fun setAllowDeselection(allowDeselection: Boolean) {
        this.allowDeselection = allowDeselection
    }

    /**
     * Reset all toggles to unselected
     */
    fun reset() {
        for (toggle in toggles) {
            toggle.isSelected = false
            toggleState(toggle)
        }
    }

    /**
     * Manually set the toggled state of the specified toggle.
     *
     * @param toggleId the id of the toggle
     * @param toggled  true if should be toggled on, false otherwise
     */
    fun setToggled(toggleId: Int, toggled: Boolean) {
        for (toggle in toggles) {
            if (toggle.id == toggleId) {
                toggle.isSelected = toggled
                toggleState(toggle)
                if (!multipleSelection) {
                    for (otherToggle in toggles) {
                        if (otherToggle != toggle && otherToggle.isSelected) {
                            otherToggle.isSelected = false
                            toggleState(otherToggle)
                            break
                        }
                    }
                }
                break
            }
        }
    }

    private fun toggleState(toggle: Toggle) {
        val view = linearLayout.findViewById<View>(toggle.id)
        view.isSelected = toggle.isSelected

        if (toggle.isSelected) {
            val index = toggles.indexOf(toggle)
            val colorDrawable = if (selectedColors != null && selectedColors!!.size > index) {
                selectedColors!![index]
            } else {
                ColorDrawable(selectedColor!!)
            }
            view.background = colorDrawable
        } else {
            view.background = null
        }
    }

    /**
     * Default view for Toggle
     */
    @SuppressLint("ViewConstructor")
    internal class ToggleView(context: Context, toggle: Toggle, @LayoutRes layoutRes: Int?) : FrameLayout(context) {
        var textView: TextView? = null
        var imageView: ImageView? = null

        init {
            id = toggle.id
            if (layoutRes != null) {
                View.inflate(context, layoutRes, this)
                textView = findViewById(android.R.id.text1)
                imageView = findViewById(android.R.id.icon)
            } else {
                textView = TextView(context)
                imageView = ImageView(context)
                addView(imageView)
                addView(textView)
                val eightDp = Utils.dpToPx(getContext(), 8)
                setPadding(eightDp, eightDp, eightDp, eightDp)
            }
            setTag(R.id.tb_toggle_id, toggle)
            textView?.text = toggle.title
            if (toggle.icon != null) {
                imageView?.setImageDrawable(toggle.icon)
            }
            foreground = Utils.getThemeAttrDrawable(getContext(), R.attr.selectableItemBackground)
        }
    }
}
