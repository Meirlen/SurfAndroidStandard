package ru.surfstudio.android.animations.behaviors

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import ru.surfstudio.android.animations.R


@Suppress("unused")
/**
 * Behavior для сокрытия кнопки при скролле NestedScrollView
 *
 * Xml параметры: behavior_showScrollLevel, behavior_hideScrollLevel
 */
class BottomButtonBehavior @JvmOverloads constructor(
        context: Context? = null,
        attrs: AttributeSet? = null
) : ViewSnackbarBehavior<Button>(context, attrs) {

    var showScrollLevel: Int = -1
    var hideScrollLevel: Int = 1

    /**
     * @param showScrollLevel  должен быть меньше 0
     * @param hideScrollLevel  больше 0
     */
    constructor(showScrollLevel: Int, hideScrollLevel: Int) : this() {
        this.showScrollLevel = showScrollLevel
        this.hideScrollLevel = hideScrollLevel
    }

    init {
        applyAttrs(context, attrs)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: Button, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)

        val animator = ViewCompat.animate(child).setInterpolator(FastOutSlowInInterpolator())
        if (dyConsumed > hideScrollLevel) {
            val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
            val bottomMargin = layoutParams.bottomMargin
            animator.translationY((child.height + bottomMargin).toFloat())
                    .setStartDelay(100L)
                    .start()
        } else if (dyConsumed < showScrollLevel) {
            animator.translationY(0f)
                    .start()
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: Button, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    private fun applyAttrs(context: Context?, attrs: AttributeSet?) {
        val ta = context?.theme?.obtainStyledAttributes(attrs, R.styleable.BottomButtonBehavior_Params, 0, 0)
        if (ta != null) {
            showScrollLevel = ta.getInt(R.styleable.BottomButtonBehavior_Params_behavior_showScrollLevel, showScrollLevel)
            hideScrollLevel = ta.getInt(R.styleable.BottomButtonBehavior_Params_behavior_hideScrollLevel, hideScrollLevel)
        }
        ta?.recycle()
    }
}