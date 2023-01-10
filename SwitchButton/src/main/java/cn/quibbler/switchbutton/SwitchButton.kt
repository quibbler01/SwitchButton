package cn.quibbler.switchbutton

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * SwitchButton.
 */
class SwitchButton : View {

    companion object {

        private val DEFAULT_WIDTH = 58.dp2px()
        private val DEFAULT_HEIGHT = 36.dp2px()

        /**
         * Animation state:
         * 1. Still
         * 2. Entering dragging
         * 3. Being dragging
         * 4. Drag-reset
         * 5. Drag-switch
         * 6. Click to switch
         */
        private const val ANIMATE_STATE_NONE = 0
        private const val ANIMATE_STATE_PENDING_DRAG = 1
        private const val ANIMATE_STATE_DRAGING = 2
        private const val ANIMATE_STATE_PENDING_RESET = 3
        private const val ANIMATE_STATE_PENDING_SETTLE = 4
        private const val ANIMATE_STATE_SWITCH = 5

        private fun Float.dp2px(): Float {
            val r = Resources.getSystem()
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, r.displayMetrics)
        }

        private fun Int.dp2px(): Float {
            val r = Resources.getSystem()
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), r.displayMetrics)
        }

    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {

    }

}