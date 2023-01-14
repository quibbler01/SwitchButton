package cn.quibbler.switchbutton

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
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

        private fun optInt(typedArray: TypedArray?, index: Int, def: Int): Int {
            if (typedArray == null) {
                return def
            } else {
                return typedArray.getInt(index, def)
            }
        }

        private fun optPixelSize(typedArray: TypedArray?, index: Int, def: Float): Float {
            if (typedArray == null) {
                return def
            } else {
                return typedArray.getDimension(index, def)
            }
        }

        private fun optPixelSize(typedArray: TypedArray?, index: Int, def: Int): Int {
            if (typedArray == null) {
                return def
            } else {
                return typedArray.getDimensionPixelSize(index, def)
            }
        }

        private fun optColor(typedArray: TypedArray?, index: Int, def: Int): Int {
            if (typedArray == null) {
                return def
            } else {
                return typedArray.getColor(index, def)
            }
        }

        private fun optBoolean(typedArray: TypedArray?, index: Int, def: Boolean): Boolean {
            if (typedArray == null) {
                return def
            } else {
                return typedArray.getBoolean(index, def)
            }
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
    private var buttonPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 背景画笔
     */
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

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
    private var valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

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

        shadowEffect = optBoolean(typedArray, R.styleable.SwitchButton_sb_enable_effect, true)

        uncheckCircleColor = optColor(typedArray, R.styleable.SwitchButton_sb_uncheckcircle_color, -0x555556) //0XffAAAAAA;

        uncheckCircleWidth = optPixelSize(typedArray, R.styleable.SwitchButton_sb_uncheckcircle_width, 1.5f.dp2px().toInt()) //dp2pxInt(1.5f);

        uncheckCircleOffsetX = 10f.dp2px()

        uncheckCircleRadius = optPixelSize(typedArray, R.styleable.SwitchButton_sb_uncheckcircle_radius, 4f.dp2px()) //dp2px(4);

        checkedLineOffsetX = 4f.dp2px()
        checkedLineOffsetY = 4f.dp2px()

        shadowRadius = optPixelSize(typedArray, R.styleable.SwitchButton_sb_shadow_radius, 2.5f.dp2px().toInt()) //dp2pxInt(2.5f);

        shadowOffset = optPixelSize(typedArray, R.styleable.SwitchButton_sb_shadow_offset, 1.5f.dp2px().toInt()) //dp2pxInt(1.5f);

        shadowColor = optColor(typedArray, R.styleable.SwitchButton_sb_shadow_color, 0X33000000) //0X33000000;

        uncheckColor = optColor(typedArray, R.styleable.SwitchButton_sb_uncheck_color, -0x222223) //0XffDDDDDD;

        checkedColor = optColor(typedArray, R.styleable.SwitchButton_sb_checked_color, -0xae2c99) //0Xff51d367;

        borderWidth = optPixelSize(typedArray, R.styleable.SwitchButton_sb_border_width, 1f.dp2px().toInt()) //dp2pxInt(1);

        checkLineColor = optColor(typedArray, R.styleable.SwitchButton_sb_checkline_color, Color.WHITE) //Color.WHITE;

        checkLineWidth = optPixelSize(typedArray, R.styleable.SwitchButton_sb_checkline_width, 1f.dp2px().toInt()) //dp2pxInt(1.0f);

        checkLineLength = 6f.dp2px()

        val buttonColor = optColor(typedArray, R.styleable.SwitchButton_sb_button_color, Color.WHITE) //Color.WHITE;

        uncheckButtonColor = optColor(typedArray, R.styleable.SwitchButton_sb_uncheckbutton_color, buttonColor)

        checkedButtonColor = optColor(typedArray, R.styleable.SwitchButton_sb_checkedbutton_color, buttonColor)

        val effectDuration = optInt(typedArray, R.styleable.SwitchButton_sb_effect_duration, 300) //300;

        isChecked = optBoolean(typedArray, R.styleable.SwitchButton_sb_checked, false)

        showIndicator = optBoolean(typedArray, R.styleable.SwitchButton_sb_show_indicator, true)

        background = optColor(typedArray, R.styleable.SwitchButton_sb_background, Color.WHITE) //Color.WHITE;

        enableEffect = optBoolean(typedArray, R.styleable.SwitchButton_sb_enable_effect, true)

        typedArray?.recycle()

        buttonPaint.color = buttonColor

        if (shadowEffect) {
            buttonPaint.setShadowLayer(shadowRadius.toFloat(), 0f, shadowOffset.toFloat(), shadowColor)
        }


        valueAnimator.duration = effectDuration.toLong()
        valueAnimator.repeatCount = 0

        valueAnimator.addUpdateListener(animatorUpdateListener)
        valueAnimator.addListener(animatorListener)

        super.setClickable(true)
        this.setPadding(0, 0, 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
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