package com.hromnik.contactgroups.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hromnik.contactgroups.R

class FloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {

    init {
        layoutParams = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            marginEnd = resources.getDimensionPixelSize(R.dimen.fab_margin)
            bottomMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
        }

        /*val layout = CoordinatorLayout(context)
        size = SIZE_AUTO
        customSize = resources.getDimensionPixelSize(R.dimen.fab_margin)

        val imageView = ImageView(context).apply {
            scaleType = ScaleType.CENTER
        }

        layout.addView(imageView)*/

        setImageResource(android.R.drawable.ic_input_add)
        imageTintList = ContextCompat.getColorStateList(context, R.color.white)
    }
}