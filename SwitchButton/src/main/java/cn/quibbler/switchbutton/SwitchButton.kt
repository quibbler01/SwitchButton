package cn.quibbler.switchbutton

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.RectF
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

    /**
     * 阴影半径
     */
    private var shadowRadius = 0

    /**
     * 阴影Y偏移px
     */
    private var shadowOffset = 0

    /**
     * 阴影颜色
     */
    private var shadowColor = 0

    /**
     * 背景半径
     */
    private var viewRadius = 0f

    /**
     * 按钮半径
     */
    private var buttonRadius = 0f

    /**
     * 背景高
     */
    private var height = 0f

    /**
     * 背景宽
     */
    private var width = 0f

    /**
     * 背景位置
     */
    private var left = 0f
    private var top = 0f
    private var right = 0f
    private var bottom = 0f
    private var centerX = 0f
    private var centerY = 0f

    /**
     * 背景底色
     */
    private var background = 0

    /**
     * 背景关闭颜色
     */
    private var uncheckColor = 0

    /**
     * 背景打开颜色
     */
    private var checkedColor = 0

    /**
     * 边框宽度px
     */
    private var borderWidth = 0

    /**
     * 打开指示线颜色
     */
    private var checkLineColor = 0

    /**
     * 打开指示线宽
     */
    private var checkLineWidth = 0

    /**
     * 打开指示线长
     */
    private var checkLineLength = 0f

    /**
     * 关闭圆圈颜色
     */
    private var uncheckCircleColor = 0

    /**
     * 关闭圆圈线宽
     */
    private var uncheckCircleWidth = 0

    /**
     * 关闭圆圈位移X
     */
    private var uncheckCircleOffsetX = 0f

    /**
     * 关闭圆圈半径
     */
    private var uncheckCircleRadius = 0f

    /**
     * 打开指示线位移X
     */
    private var checkedLineOffsetX = 0f

    /**
     * 打开指示线位移Y
     */
    private var checkedLineOffsetY = 0f

    /**
     * Color for button when it's uncheck
     */
    private var uncheckButtonColor = 0

    /**
     * Color for button when it's check
     */
    private var checkedButtonColor = 0


    /**
     * 按钮最左边
     */
    private var buttonMinX = 0f

    /**
     * 按钮最右边
     */
    private var buttonMaxX = 0f

    /**
     * 按钮画笔
     */
    private var buttonPaint: Paint? = null

    /**
     * 背景画笔
     */
    private var paint: Paint? = null


    /**
     * 当前状态
     */
    private var viewState: ViewState = ViewState()
    private var beforeState: ViewState = ViewState()
    private var afterState: ViewState = ViewState()

    private val rect = RectF()

    /**
     * 动画状态
     */
    private var animateState = ANIMATE_STATE_NONE


    /**
     *
     */
    private var valueAnimator: ValueAnimator? = null

    private var argbEvaluator = ArgbEvaluator()


    /**
     * 是否选中
     */
    private var isChecked = false

    /**
     * 是否启用动画
     */
    private var enableEffect = false

    /**
     * 是否启用阴影效果
     */
    private var shadowEffect = false

    /**
     * 是否显示指示器
     */
    private var showIndicator = false

    /**
     * 收拾是否按下
     */
    private var isTouchingDown = false

    /**
     *
     */
    private var isUiInited = false

    /**
     *
     */
    private var isEventBroadcast = false

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    /**
     * 手势按下的时刻
     */
    private var touchDownTime: Long = 0L

    private val postPendingDrag = Runnable {
        if (!isInAnimating()) {
            pendingDragState()
        }
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.SwitchButton)



        typedArray?.recycle()
    }

    /**
     * Is it in animation state
     * @return
     */
    private fun isInAnimating(): Boolean {
        return animateState != ANIMATE_STATE_NONE
    }

    /**
     * Start dragging
     */
    private fun pendingDragState() {
        if (isInAnimating()) return
        if (!isTouchingDown) return

        valueAnimator?.let {
            if (it.isRunning) {
                it.cancel()
            }
        }

        animateState = ANIMATE_STATE_PENDING_DRAG


        beforeState.copy(viewState)
        afterState.copy(viewState)

        if (isChecked) {
            afterState.checkStateColor = checkedColor
            afterState.buttonX = buttonMaxX
            afterState.checkedLineColor = checkedColor
        } else {
            afterState.checkStateColor = uncheckColor
            afterState.buttonX = buttonMinX
            afterState.radius = viewRadius
        }

        valueAnimator?.start()
    }

    private interface OnCheckedChangeListener {
        fun onCheckedChanged(view: SwitchButton, isChecked: Boolean)
    }

    /**
     * 保存动画状态
     * */
    private class ViewState {

        /**
         * 按钮x位置[buttonMinX-buttonMaxX]
         */
        var buttonX = 0f

        /**
         * 状态背景颜色
         */
        var checkStateColor = 0

        /**
         * 选中线的颜色
         */
        var checkedLineColor = 0

        /**
         * 状态背景的半径
         */
        var radius = 0f

        fun copy(source: ViewState) {
            buttonX = source.buttonX
            checkStateColor = source.checkStateColor
            checkedLineColor = source.checkedLineColor
            radius = source.radius
        }

    }

}