package com.savvyapps.togglebuttonlayout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.TooltipCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * ToggleButtonLayout is a layout used to group related options. Layout and spacing is arranged to
 * convey that certain toggle buttons are part of a group.
 */
class ToggleButtonLayout : CardView {

    /**
     * If [allow][allowDeselection] selected items to be de-selected. Defaults to true.
     */
    var allowDeselection = true

    /**
     * Set if we are going to [allow multiple selection][multipleSelection] or not.
     * This will also call [reset] in order to prevent strange behaviour switching between multiple and single selection
     */
    var multipleSelection: Boolean = false
        set(value) {
            field = value
            reset()
        }

    private lateinit var rootContainer: LinearLayout

    private val toggles = mutableListOf<Toggle>()

    @ColorInt
    private var dividerColor: Int? = null
    private var dividerSize: Int = 0
    private var hasDivider = false

    @ColorInt
    private var toggleBackground: Drawable? = null
    private var toggleBackgrounds: Array<Drawable>? = null
    private var isBgSeparated = false
    @ColorInt
    private lateinit var contentColor: ColorStateList
    private var contentColors: Array<ColorStateList>? = null
    private var isContentSeparated = false

    @LayoutRes
    private var layoutRes: Int = 0
    private var toggleLayoutMode: Int = MODE_WRAP

    private val onToggleClickListener = OnClickListener { v ->
        val toggle = v.getTag(R.id.tb_toggle_id) as Toggle
        val thisSelected = toggle.isSelected

        if (!allowDeselection && thisSelected) {
            //do nothing
        } else {
            toggle(toggle.id, !thisSelected)
            onToggledListener?.invoke(toggle, !thisSelected)
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

        preventCornerOverlap = false

        rootContainer = LinearLayout(context)
        addView(rootContainer)

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleButtonLayout, 0, 0)

        multipleSelection = a.getBoolean(R.styleable.ToggleButtonLayout_multipleSelection, false)
        allowDeselection = a.getBoolean(R.styleable.ToggleButtonLayout_allowDeselection, true)
        toggleLayoutMode = a.getInt(R.styleable.ToggleButtonLayout_toggleLayoutMode, MODE_WRAP)
        layoutRes = a.getResourceId(R.styleable.ToggleButtonLayout_customLayout, 0)

        if (a.hasValue(R.styleable.ToggleButtonLayout_dividerColor) ||
            a.hasValue(R.styleable.ToggleButtonLayout_dividerSize)
        ) {
            hasDivider = true
            dividerColor = a.getColor(
                R.styleable.ToggleButtonLayout_dividerColor,
                context.resources.getColor(R.color.default_divider_color)
            )
            dividerSize = a.getDimensionPixelSize(
                R.styleable.ToggleButtonLayout_dividerSize,
                context.dpToPx(1)
            )
        }

        toggleBackground = a.getDrawable(R.styleable.ToggleButtonLayout_toggleBackground)
        if (toggleBackground == null) {
            val drawable = StateListDrawable()
            drawable.addState(
                intArrayOf(android.R.attr.state_selected),
                ColorDrawable(context.resources.getColor(R.color.default_selected_background))
            )
            toggleBackground = drawable
        }
        if (a.hasValue(R.styleable.ToggleButtonLayout_toggleBackgrounds)) {
            isBgSeparated = true
            val resId = a.getResourceId(R.styleable.ToggleButtonLayout_toggleBackgrounds, -1)
            val ta = resources.obtainTypedArray(resId)
            toggleBackgrounds = Array(ta.length()) { ta.getDrawable(it) }
            ta.recycle()
        }

        contentColor = a.getColorStateList(R.styleable.ToggleButtonLayout_contentColor) ?:
                ColorStateList.valueOf(resources.getColor(R.color.default_content_color))
        if (a.hasValue(R.styleable.ToggleButtonLayout_contentColors)) {
            isContentSeparated = true
            val resId = a.getResourceId(R.styleable.ToggleButtonLayout_contentColors, -1)
            val ta = resources.obtainTypedArray(resId)
            contentColors = Array(ta.length()) { ta.getColorStateList(it) }
            ta.recycle()
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
        rootContainer.removeAllViews()
        toggles.clear()

        val menu = MenuBuilder(context)
        MenuInflater(context).inflate(menuId, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val toggle = Toggle(item.itemId, item.icon, item.title)
            addToggle(toggle)
        }
    }

    /**
     * Add a [toggle] to the layout
     */
    fun addToggle(toggle: Toggle) {
        val index = toggles.size
        toggles.add(toggle)

        val toggleView = ToggleView(context, toggle, layoutRes)
        toggleView.setOnClickListener(onToggleClickListener)

        if (hasDivider && index > 0) {
            val divider = View(context)
            divider.setBackgroundColor(dividerColor!!)
            divider.layoutParams = LayoutParams(dividerSize, MATCH_PARENT)
            rootContainer.addView(divider)
        }

        if (toggleLayoutMode == MODE_EVEN) {
            toggleView.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
        } else {
            toggleView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        if (!isCustomLayout()) {
            val color = if (isContentSeparated && index < contentColors!!.size) {
                contentColors!![index]
            } else {
                contentColor
            }
            toggleView.contentColor = color

            val bg = if (isBgSeparated && index < toggleBackgrounds!!.size) {
                toggleBackgrounds!![index]
            } else {
                toggleBackground
            }
            toggleView.background = bg!!.mutate().constantState.newDrawable()
        }

        rootContainer.addView(toggleView)
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
     * Manually set state of the [specified id][toggleId] toggle to [toggled].
     */
    fun toggle(toggleId: Int, toggled: Boolean) {
        for ((i, toggle) in toggles.withIndex()) {
            if (toggle.id != toggleId) {
                if (!multipleSelection && toggle.isSelected) {
                    toggle.isSelected = false
                    syncToggleState(i)
                }
            } else if (toggle.isSelected != toggled) {
                toggle.isSelected = toggled
                syncToggleState(i)
            }
        }
    }

    /**
     * Set all toggles to [selected].
     *
     * If [multipleSelection] is set to false, [toggleAll] to [true][selected] will do nothing
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

    private fun syncToggleState(index: Int) {
        val toggle = toggles[index]
        val view = rootContainer.findViewById<ToggleView>(toggle.id)
        view.isSelected = toggle.isSelected
    }

    private fun isCustomLayout() = layoutRes != 0

    /**
     * Default view for Toggle
     */
    @SuppressLint("ViewConstructor")
    private class ToggleView(
        context: Context, private val toggle: Toggle, @LayoutRes layoutRes: Int
    ) : FrameLayout(context) {

        private val isCustomView = layoutRes != 0

        var textView: TextView? = null
        var imageView: ImageView? = null

        internal var contentColor: ColorStateList? = null
            set(value) {
                field = value
                if (!isCustomView) {
                    textView?.setTextColor(value)
                }
            }

        init {
            id = toggle.id

            if (isCustomView) {
                View.inflate(context, layoutRes, this)
                textView = findViewById(android.R.id.text1)
                imageView = findViewById(android.R.id.icon)
            } else {
                buildDefaultView()
            }

            setTag(R.id.tb_toggle_id, toggle)
            textView?.text = toggle.title
            if (hasIcon()) {
                imageView?.setImageDrawable(toggle.icon)
            }
            foreground = context.getThemeAttrDrawable(R.attr.selectableItemBackground)
        }

        private fun buildDefaultView() {
            val lp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            if (!hasIcon()) {
                textView = TextView(context)
                textView!!.layoutParams = lp
                addView(textView)
            } else {
                imageView = ImageView(context)
                imageView!!.layoutParams = lp

                if (toggle.title != null) {
                    TooltipCompat.setTooltipText(this, toggle.title)
                }
                addView(imageView)
            }

            val padding = context.resources.getDimensionPixelSize(R.dimen.default_padding)
            setPadding(padding, padding, padding, padding)
        }

        override fun setSelected(selected: Boolean) {
            if (!isCustomView) {
                var color = contentColor!!.defaultColor
                if (selected) {
                    color = contentColor!!.getColorForState(
                        intArrayOf(android.R.attr.state_selected), color
                    )
                }
                imageView?.setColorFilter(color)
            }
            super.setSelected(selected)
        }

        private fun hasIcon() = toggle.icon != null
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
