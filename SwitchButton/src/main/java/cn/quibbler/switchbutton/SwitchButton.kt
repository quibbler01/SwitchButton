package cn.quibbler.switchbutton

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import kotlin.math.max
import kotlin.math.min

/**
 * SwitchButton.
 */
class SwitchButton : View, Checkable {

    companion object {

        private val DEFAULT_WIDTH: Int = 58.dp2px().toInt()
        private val DEFAULT_HEIGHT: Int = 36.dp2px().toInt()

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
     * Shadow radius
     */
    private var shadowRadius = 0

    /**
     * Shadow Y offset px
     */
    private var shadowOffset = 0

    /**
     * Shadow Color
     */
    private var shadowColor = 0

    /**
     * Background radius
     */
    private var viewRadius = 0f

    /**
     * Button radius
     */
    private var buttonRadius = 0f

    /**
     * Background high
     */
    private var height = 0f

    /**
     * Background width
     */
    private var width = 0f

    /**
     * Background position
     */
    private var left = 0f
    private var top = 0f
    private var right = 0f
    private var bottom = 0f
    private var centerX = 0f
    private var centerY = 0f

    /**
     * Background background color
     */
    private var background = 0

    /**
     * Background off color
     */
    private var uncheckColor = 0

    /**
     * Background Open Color
     */
    private var checkedColor = 0

    /**
     * Border width px
     */
    private var borderWidth = 0

    /**
     * Turn on indicator line color
     */
    private var checkLineColor = 0

    /**
     * Turn on indicator line weight
     */
    private var checkLineWidth = 0

    /**
     * Open indicator line length
     */
    private var checkLineLength = 0f

    /**
     * Turn off circle color
     */
    private var uncheckCircleColor = 0

    /**
     * Turn off circle lineweight
     */
    private var uncheckCircleWidth = 0

    /**
     * Close circle displacement X
     */
    private var uncheckCircleOffsetX = 0f

    /**
     * Turn off circle radius
     */
    private var uncheckCircleRadius = 0f

    /**
     * Open indicator line displacement X
     */
    private var checkedLineOffsetX = 0f

    /**
     * Open indicator line displacement Y
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
     * The leftmost button
     */
    private var buttonMinX = 0f

    /**
     * Rightmost button
     */
    private var buttonMaxX = 0f

    /**
     * Button brush
     */
    private var buttonPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Background brush
     */
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Current state
     */
    private var viewState: ViewState = ViewState()
    private var beforeState: ViewState = ViewState()
    private var afterState: ViewState = ViewState()

    private val rect = RectF()

    /**
     * Animation state
     */
    private var animateState = ANIMATE_STATE_NONE

    /**
     * value animator for this SwitchButton
     */
    private var valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    /**
     * Evaluator of ARGB colors
     */
    private var argbEvaluator = ArgbEvaluator()

    /**
     * Check
     */
    private var isChecked = false

    /**
     * Whether to enable animation
     */
    private var enableEffect = false

    /**
     * Whether to enable shadow effect
     */
    private var shadowEffect = false

    /**
     * Whether the indicator is displayed
     */
    private var showIndicator = false

    /**
     * Check whether it is pressed
     */
    private var isTouchingDown = false

    /**
     * is UI init
     */
    private var isUiInited = false

    /**
     * should broadcast state check change
     */
    private var isEventBroadcast = false

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    /**
     * The moment when the gesture is pressed
     */
    private var touchDownTime: Long = 0L

    /**
     * post pending drag this SwitchButton
     */
    private val postPendingDrag = Runnable {
        if (!isInAnimating()) {
            pendingDragState()
        }
    }

    /**
     * this SwitchButton animator update listener
     */
    private val animatorUpdateListener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        val value = animation.animatedValue as Float
        when (animateState) {
            ANIMATE_STATE_PENDING_SETTLE -> {}
            ANIMATE_STATE_PENDING_RESET -> {}
            ANIMATE_STATE_PENDING_DRAG -> {
                viewState.checkedLineColor = argbEvaluator.evaluate(value, beforeState.checkedLineColor, afterState.checkedLineColor) as Int
                viewState.radius = beforeState.radius + (afterState.radius - beforeState.radius) * value
                if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                    viewState.buttonX = beforeState.buttonX + (afterState.buttonX - beforeState.buttonX) * value
                    viewState.checkStateColor = argbEvaluator.evaluate(value, beforeState.checkStateColor, afterState.checkStateColor) as Int
                }
            }

            ANIMATE_STATE_SWITCH -> {
                viewState.buttonX = beforeState.buttonX + (afterState.buttonX - beforeState.buttonX) * value
                val fraction = (viewState.buttonX - buttonMinX) / (buttonMaxX - buttonMinX)
                viewState.checkStateColor = argbEvaluator.evaluate(fraction, uncheckColor, checkedColor) as Int
                viewState.radius = fraction * viewRadius
                viewState.checkedLineColor = argbEvaluator.evaluate(fraction, Color.TRANSPARENT, checkLineColor) as Int
            }

            ANIMATE_STATE_DRAGING -> {}
            ANIMATE_STATE_NONE -> {}
        }
        postInvalidate()
    }

    /**
     * this SwitchButton animator listener
     */
    private val animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            when (animateState) {
                ANIMATE_STATE_DRAGING -> {}
                ANIMATE_STATE_PENDING_DRAG -> {
                    animateState = ANIMATE_STATE_DRAGING
                    viewState.checkedLineColor = Color.TRANSPARENT
                    viewState.radius = viewRadius

                    postInvalidate()
                }

                ANIMATE_STATE_PENDING_RESET -> {
                    animateState = ANIMATE_STATE_NONE
                    postInvalidate()
                }

                ANIMATE_STATE_PENDING_SETTLE -> {
                    animateState = ANIMATE_STATE_NONE
                    postInvalidate()
                    broadcastEvent()
                }

                ANIMATE_STATE_SWITCH -> {
                    isChecked = !isChecked
                    animateState = ANIMATE_STATE_NONE
                    postInvalidate()
                    broadcastEvent()
                }

                ANIMATE_STATE_NONE -> {}
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    }

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    /**
     * init view property
     *
     * @param context Context?
     * @param attrs AttributeSet?
     */
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
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private fun broadcastEvent() {
        if (onCheckedChangeListener != null) {
            isEventBroadcast = true
            onCheckedChangeListener?.onCheckedChanged(this, isChecked)
        }
        isEventBroadcast = false
    }

    /**
     * Is it in animation state
     * @return is in animating
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

        valueAnimator.let {
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

        valueAnimator.start()
    }

    override fun setChecked(checked: Boolean) {
        if (checked == isChecked()) {
            postInvalidate()
            return
        }
        toggle(enableEffect, false)
    }

    override fun isChecked(): Boolean = isChecked

    override fun toggle() {
        toggle(true)
    }

    /**
     * Switch state
     * @param animate
     */
    fun toggle(animate: Boolean) {
        toggle(animate, true)
    }

    /**
     * toggle SwitchButton check state
     *
     * @param animate Boolean
     * @param broadcast Boolean
     */
    private fun toggle(animate: Boolean, broadcast: Boolean) {
        if (!isEnabled) return

        if (isEventBroadcast) {
            throw RuntimeException("should NOT switch the state in method: [onCheckedChanged]!")
        }

        if (!isUiInited) {
            isChecked = !isChecked
            if (broadcast) {
                broadcastEvent()
            }
            return
        }

        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }

        if (!enableEffect || !animate) {
            isChecked = !isChecked
            if (isChecked()) {
                setCheckedViewState(viewState)
            } else {
                setUncheckViewState(viewState)
            }
            postInvalidate()
            if (broadcast) {
                broadcastEvent()
            }
            return
        }

        animateState = ANIMATE_STATE_SWITCH
        beforeState.copy(viewState)

        if (isChecked()) {
            //Switch to unchecked
            setUncheckViewState(afterState)
        } else {
            setCheckedViewState(afterState)
        }
        valueAnimator.start()
    }

    /**
     * set view state which is check
     *
     * @param viewState ViewState
     */
    private fun setCheckedViewState(viewState: ViewState) {
        viewState.radius = viewRadius
        viewState.checkStateColor = checkedColor
        viewState.checkedLineColor = checkLineColor
        viewState.buttonX = buttonMaxX
        buttonPaint.color = checkedButtonColor
    }

    /**
     * set view state which is uncheck
     *
     * @param viewState ViewState
     */
    private fun setUncheckViewState(viewState: ViewState) {
        viewState.radius = 0f
        viewState.checkStateColor = uncheckColor
        viewState.checkedLineColor = Color.TRANSPARENT
        viewState.buttonX = buttonMinX
        buttonPaint.color = uncheckButtonColor
    }

    /**
     * interface which use to listen Check change
     */
    private interface OnCheckedChangeListener {
        fun onCheckedChanged(view: SwitchButton, isChecked: Boolean)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(0, 0, 0, 0)
    }

    override fun onMeasure(widthMeasureSpec_: Int, heightMeasureSpec_: Int) {
        var widthMeasureSpec = widthMeasureSpec_
        var heightMeasureSpec = heightMeasureSpec_
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_WIDTH, MeasureSpec.EXACTLY)
        }
        if (heightMode == MeasureSpec.UNSPECIFIED
            || heightMode == MeasureSpec.AT_MOST
        ) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val viewPadding = max(shadowRadius + shadowOffset, borderWidth).toFloat()
        height = h - viewPadding - viewPadding
        width = w - viewPadding - viewPadding

        viewRadius = height * 0.5f
        buttonRadius = viewRadius - borderWidth

        left = viewPadding
        top = viewPadding
        right = w - viewPadding
        bottom = h - viewPadding

        centerX = (left + right) * .5f
        centerY = (top + bottom) * .5f

        buttonMinX = left + viewRadius
        buttonMaxX = right - viewRadius

        if (isChecked()) {
            setCheckedViewState(viewState)
        } else {
            setUncheckViewState(viewState)
        }

        isUiInited = true

        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.strokeWidth = borderWidth.toFloat()
        paint.style = Paint.Style.FILL
        //Draw a white background
        paint.color = background

        drawRoundRect(canvas, left, top, right, bottom, viewRadius, paint)

        //Draw a closed border
        paint.style = Paint.Style.STROKE
        paint.color = uncheckColor
        drawRoundRect(canvas, left, top, right, bottom, viewRadius, paint)

        //Draw a small circle
        if (showIndicator) {
            drawUncheckIndicator(canvas)
        }

        //Draw to turn on the background color
        val des = viewState.radius * .5f //[0-backgroundRadius*0.5f]
        paint.style = Paint.Style.STROKE
        paint.color = viewState.checkStateColor
        paint.strokeWidth = borderWidth + des * 2f
        drawRoundRect(canvas, left + des, top + des, right - des, bottom - des, viewRadius, paint)

        //Draw button left green strip occlusion
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 1f
        drawArc(canvas, left, top, left + 2 * viewRadius, top + 2 * viewRadius, 90f, 180f, paint)
        canvas!!.drawRect(left + viewRadius, top, viewState.buttonX, top + 2 * viewRadius, paint)

        //Draw small lines
        if (showIndicator) {
            drawCheckedIndicator(canvas)
        }

        //Draw button
        drawButton(canvas, viewState.buttonX, centerY)
    }

    /**
     * Draw selected status indicator
     * @param canvas
     */
    protected fun drawCheckedIndicator(canvas: Canvas?) {
        drawCheckedIndicator(
            canvas,
            viewState.checkedLineColor,
            checkLineWidth.toFloat(),
            left + viewRadius - checkedLineOffsetX, centerY - checkLineLength,
            left + viewRadius - checkedLineOffsetY, centerY + checkLineLength,
            paint
        );
    }

    /**
     * Draw selected status indicator
     * @param canvas
     * @param color
     * @param lineWidth
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     * @param paint
     */
    protected fun drawCheckedIndicator(
        canvas: Canvas?,
        color: Int,
        lineWidth: Float,
        sx: Float, sy: Float, ex: Float, ey: Float,
        paint: Paint
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineWidth
        canvas?.drawLine(sx, sy, ex, ey, paint)
    }

    /**
     * Draw off status indicator
     * @param canvas
     */
    private fun drawUncheckIndicator(canvas: Canvas?) {
        drawUncheckIndicator(
            canvas,
            uncheckCircleColor,
            uncheckCircleWidth.toFloat(),
            right - uncheckCircleOffsetX, centerY,
            uncheckCircleRadius,
            paint
        )
    }

    /**
     * Draw off status indicator
     * @param canvas
     * @param color
     * @param lineWidth
     * @param centerX
     * @param centerY
     * @param radius
     * @param paint
     */
    protected fun drawUncheckIndicator(
        canvas: Canvas?,
        color: Int,
        lineWidth: Float,
        centerX: Float, centerY: Float,
        radius: Float,
        paint: Paint
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineWidth
        canvas?.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawArc(canvas: Canvas?, left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, paint: Paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas?.drawArc(left, top, right, bottom, startAngle, sweepAngle, true, paint)
        } else {
            rect[left, top, right] = bottom
            canvas?.drawArc(rect, startAngle, sweepAngle, true, paint)
        }
    }

    /**
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param backgroundRadius
     * @param paint
     */
    private fun drawRoundRect(canvas: Canvas?, left: Float, top: Float, right: Float, bottom: Float, backgroundRadius: Float, paint: Paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas?.drawRoundRect(left, top, right, bottom, backgroundRadius, backgroundRadius, paint)
        } else {
            rect[left, top, right] = bottom
            canvas?.drawRoundRect(rect, backgroundRadius, backgroundRadius, paint)
        }
    }

    /**
     * @param canvas
     * @param x px
     * @param y px
     */
    private fun drawButton(canvas: Canvas?, x: Float, y: Float) {
        canvas?.drawCircle(x, y, buttonRadius, buttonPaint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = -0x222223
        canvas?.drawCircle(x, y, buttonRadius, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) return false
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchingDown = true
                touchDownTime = System.currentTimeMillis()
                removeCallbacks(postPendingDrag)
                postDelayed(postPendingDrag, 100)
            }

            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                if (isPendingDragState()) {
                    //When you are ready to enter the dragging state, you can drag the button position
                    var fraction: Float = eventX / getWidth()
                    fraction = max(0f, min(1f, fraction))
                    viewState.buttonX = buttonMinX + (buttonMaxX - buttonMinX) * fraction
                } else if (isDragState()) {
                    //Drag the button position and change the corresponding background color
                    var fraction = eventX / getWidth()
                    fraction = max(0f, min(1f, fraction))
                    viewState.buttonX = (buttonMinX + (buttonMaxX - buttonMinX) * fraction)
                    viewState.checkStateColor = argbEvaluator.evaluate(fraction, uncheckColor, checkedColor) as Int
                    postInvalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                isTouchingDown = false
                removeCallbacks(postPendingDrag)
                if (System.currentTimeMillis() - touchDownTime <= 300) {
                    //If the click time is less than 300 ms, it is considered as a click operation
                    toggle()
                } else if (isDragState()) {
                    //In the dragging state, calculate the button position and set whether to switch the state
                    val eventX = event.x
                    var fraction: Float = eventX / getWidth()
                    fraction = max(0f, min(1f, fraction))
                    val newCheck = fraction > .5f
                    if (newCheck == isChecked()) {
                        pendingCancelDragState()
                    } else {
                        isChecked = newCheck
                        pendingSettleState()
                    }
                } else if (isPendingDragState()) {
                    //When preparing to enter the dragging state, cancel it and reset it
                    pendingCancelDragState()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                isTouchingDown = true

                removeCallbacks(postPendingDrag)
                if (isPendingDragState() || isDragState()) {
                    //reset
                    pendingCancelDragState()
                }
            }
        }
        return true
    }

    /**
     * Whether you are entering or leaving the dragging state
     * @return
     */
    private fun isPendingDragState(): Boolean = (animateState == ANIMATE_STATE_PENDING_DRAG || animateState == ANIMATE_STATE_PENDING_RESET)

    /**
     * Whether it is in finger dragging state
     * @return
     */
    private fun isDragState(): Boolean = animateState == ANIMATE_STATE_DRAGING

    /**
     * Cancel drag status
     */
    private fun pendingCancelDragState() {
        if (isDragState() || isPendingDragState()) {
            if (valueAnimator.isRunning) {
                valueAnimator.cancel()
            }
            animateState = ANIMATE_STATE_PENDING_RESET
            beforeState.copy(viewState)
            if (isChecked()) {
                setCheckedViewState(afterState)
            } else {
                setUncheckViewState(afterState)
            }
            valueAnimator.start()
        }
    }

    /**
     * Animation - Set a new state
     */
    private fun pendingSettleState() {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        animateState = ANIMATE_STATE_PENDING_SETTLE
        beforeState.copy(viewState)
        if (isChecked()) {
            setCheckedViewState(afterState)
        } else {
            setUncheckViewState(afterState)
        }
        valueAnimator.start()
    }

    /**
     * Save animation state
     * */
    private class ViewState {

        /**
         * Button x position[buttonMinX-buttonMaxX]
         */
        var buttonX = 0f

        /**
         * Status background color
         */
        var checkStateColor = 0

        /**
         * Color of selected lines
         */
        var checkedLineColor = 0

        /**
         * Radius of the state background
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