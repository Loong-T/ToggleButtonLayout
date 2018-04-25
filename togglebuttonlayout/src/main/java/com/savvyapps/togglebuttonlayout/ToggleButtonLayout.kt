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

    private lateinit var linearLayout: LinearLayout

    private val toggles = mutableListOf<Toggle>()

    // customization
    private var multipleSelection: Boolean = false
    private var allowDeselection = true
    @ColorInt
    private var dividerColor: Int? = null
    @ColorInt
    private var selectedColor: Int? = null
    private var selectedColors: Array<Drawable>? = null
    @ColorInt
    private var textColor: Int? = null
    private var textColors: Array<ColorStateList>? = null
    @LayoutRes
    private var layoutRes: Int? = null
    private var toggleLayoutMode: Int = MODE_WRAP

    private val onToggleClickListener = OnClickListener { v ->
        val toggle = v.getTag(R.id.tb_toggle_id) as Toggle
        val thisSelected = toggle.isSelected
        // if allowing deselection and currently selected, do nothing. Could invert it, but
        // it makes more sense to me this way
        if (!allowDeselection && thisSelected) {
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

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        linearLayout = LinearLayout(context)
        addView(linearLayout)

        val a = getContext().obtainStyledAttributes(
            attrs, R.styleable.ToggleButtonLayout, 0, 0
        )

        multipleSelection = a.getBoolean(R.styleable.ToggleButtonLayout_multipleSelection, false)
        allowDeselection = a.getBoolean(R.styleable.ToggleButtonLayout_allowDeselection, true)
        toggleLayoutMode = a.getInt(R.styleable.ToggleButtonLayout_toggleLayoutMode, MODE_WRAP)
        if (a.hasValue(R.styleable.ToggleButtonLayout_dividerColor)) {
            dividerColor = a.getColor(R.styleable.ToggleButtonLayout_dividerColor, Color.GRAY)
        }
        if (a.hasValue(R.styleable.ToggleButtonLayout_customLayout)) {
            layoutRes = a.getResourceId(R.styleable.ToggleButtonLayout_customLayout, 0)
        }

        selectedColor = a.getColor(
            R.styleable.ToggleButtonLayout_selectedColor,
            Utils.getThemeAttrColor(getContext(), R.attr.colorControlHighlight)
        )
        if (a.hasValue(R.styleable.ToggleButtonLayout_selectedColors)) {
            val resId = a.getResourceId(R.styleable.ToggleButtonLayout_selectedColors, -1)
            if (resId != -1) {
                val ta = resources.obtainTypedArray(resId)
                selectedColors = Array(ta.length()) { ta.getDrawable(it) }
                ta.recycle()
            }
        }

        textColor = a.getColor(
            R.styleable.ToggleButtonLayout_textColor,
            resources.getColor(R.color.default_text_color)
        )
        if (a.hasValue(R.styleable.ToggleButtonLayout_textColors)) {
            val resId = a.getResourceId(R.styleable.ToggleButtonLayout_textColors, -1)
            if (resId != -1) {
                val ta = resources.obtainTypedArray(resId)
                textColors = Array(ta.length()) { ta.getColorStateList(it) }
                ta.recycle()
            }
        }

        // make sure this one is last
        if (a.hasValue(R.styleable.ToggleButtonLayout_menu)) {
            inflateMenu(a.getResourceId(R.styleable.ToggleButtonLayout_menu, 0))
        }
        a.recycle()
    }


    /**
     * Add actions to the layout from the given menu resource [menuId].
     */
    @SuppressLint("RestrictedApi")
    fun inflateMenu(@MenuRes menuId: Int) {
        val menu = MenuBuilder(context)
        MenuInflater(context).inflate(menuId, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val toggle = Toggle(item.itemId, item.icon, item.title)
            addToggle(toggle, i)
        }
    }

    /**
     * Return a list of selected toggles
     */
    fun getSelectedToggles() = toggles.filter { it.isSelected }

    /**
     * Return toggle at index [i]
     */
    fun getToggle(i: Int) = toggles[i]

    /**
     * Add a [toggle] to the layout
     */
    fun addToggle(toggle: Toggle) {
        addToggle(toggle, toggles.size)
    }

    /**
     * Manually set state of the [specified id][toggleId] toggle to [toggled].
     */
    fun setToggled(toggleId: Int, toggled: Boolean) {
        for ((i, toggle) in toggles.withIndex()) {
            if (multipleSelection) {
                if (toggleId == toggle.id) {
                    toggle.isSelected = toggled
                    syncToggleState(i)
                    break
                }
            } else {
                if (toggleId == toggle.id) {
                    toggle.isSelected = toggled
                    syncToggleState(i)
                } else if (toggle.isSelected) {
                    toggle.isSelected = false
                    syncToggleState(i)
                }
            }
        }
    }

    /**
     * Set state of toggle with [toggleId] to opposite
     */
    fun toggle(toggleId: Int) {
        for ((i, toggle) in toggles.withIndex()) {
            if (multipleSelection) {
                if (toggleId == toggle.id) {
                    toggle.isSelected = !toggle.isSelected
                    syncToggleState(i)
                    break
                }
            } else {
                if (toggleId == toggle.id) {
                    toggle.isSelected = !toggle.isSelected
                    syncToggleState(i)
                } else if (toggle.isSelected) {
                    toggle.isSelected = false
                    syncToggleState(i)
                }
            }
        }
    }

    /**
     * Set if we are going to [allow] multiple selection or not. This will also call [reset]
     * in order to prevent strange behaviour switching between multiple and single selection
     */
    fun setMultipleSelection(allow: Boolean) {
        multipleSelection = allow
        reset()
    }

    /**
     * If [allow] selected items to be de-selected. Defaults to true.
     */
    fun setAllowDeselection(allow: Boolean) {
        allowDeselection = allow
    }

    /**
     * Set all toggles to [selected].
     *
     * If [setMultipleSelection] is set to false, [toggleAll] to [true][selected] will do nothing
     */
    fun toggleAll(selected: Boolean) {
        if (selected && !multipleSelection)
            return

        for ((i, toggle) in toggles.withIndex()) {
            toggle.isSelected = selected
            syncToggleState(i)
        }
    }

    /**
     * Reset all toggles to unselected
     */
    fun reset() {
        toggleAll(false)
    }

    private fun addToggle(toggle: Toggle, toggleIndex: Int) {
        toggles.add(toggle)
        val toggleView = ToggleView(context, toggle, layoutRes)
        toggleView.setOnClickListener(onToggleClickListener)

        if (dividerColor != null && toggles.size > 1) {
            val divider = View(context)
            divider.setBackgroundColor(dividerColor!!)
            val params = LinearLayout.LayoutParams(
                Utils.dpToPx(context, 1),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            divider.layoutParams = params
            linearLayout.addView(divider)
        }

        if (toggleLayoutMode == MODE_EVEN) {
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            toggleView.layoutParams = params
        }

        if (layoutRes == null && layoutRes != 0) {
            if (textColors != null && toggleIndex < textColors!!.size) {
                toggleView.textView?.setTextColor(textColors!![toggleIndex])
            } else {
                toggleView.textView?.setTextColor(textColor!!)
            }
        }

        linearLayout.addView(toggleView)
    }

    private fun syncToggleState(index: Int) {
        val toggle = toggles[index]
        val view = linearLayout.findViewById<View>(toggle.id)
        view.isSelected = toggle.isSelected

        if (toggle.isSelected) {
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
    internal class ToggleView(context: Context, toggle: Toggle, @LayoutRes layoutRes: Int?) :
        FrameLayout(context) {
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

    companion object {

        /**
         * Toggles will wrap content. Default value
         */
        const val MODE_WRAP = 0

        /**
         * Toggles will be evenly distributed within view
         */
        const val MODE_EVEN = 1
    }
}
