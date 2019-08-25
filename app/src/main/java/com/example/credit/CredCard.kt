package com.example.credit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.TextPaint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout

class CredCard : FrameLayout {

    private var exampleString: String = ""
    private var exampleColor: Int = Color.RED
    private var exampleDimension: Float = 0f
    private var hintString = Array(16) { '0' }
    private var hintPaint: TextPaint? = null
    private var textPaint: TextPaint? = null
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CredCard, defStyle, 0
        )
        init()
        this.exampleString = ""
        this.exampleColor = a.getColor(
            R.styleable.CredCard_exampleColor,
            this.exampleColor
        )
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        this.exampleDimension = a.getDimension(
            R.styleable.CredCard_exampleDimension,
            this.exampleDimension
        )

        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        hintPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint?.let {
            it.textSize = this.exampleDimension
            it.color = Color.BLACK
            textWidth = it.measureText(this.exampleString)
            textHeight = it.fontMetrics.bottom
            it.typeface = Typeface.create("Arial", Typeface.ITALIC)
        }
        hintPaint?.let {
            it.textSize = this.exampleDimension
            it.color = Color.GRAY
            it.typeface = Typeface.create("Arial", Typeface.ITALIC)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val contentHeight = height - paddingTop - paddingBottom

        this.exampleString.let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                paddingTop + (contentHeight + textHeight) / 2,
                textPaint!!
            )
        }

        hintString.joinToString("").let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                paddingTop + (contentHeight + textHeight) / 2,
                hintPaint!!
            )
        }
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val fic = BaseInputConnection(this, false)
        outAttrs.actionLabel = null
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        return fic
    }

    private fun init() {
        isFocusable = true
        isFocusableInTouchMode = true
        setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() === KeyEvent.ACTION_DOWN) {
                    setChar(keyCode, event)
                }
                return false
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
        }
        return true
    }

    fun setChar(keyCode: Int, event: KeyEvent) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 && exampleString.length < 16) {
            this.exampleString = this.exampleString + event.getUnicodeChar().toChar()
            hintString[exampleString.length - 1] = '1'
            invalidate()
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (exampleString.isNotEmpty()) {
                hintString[exampleString.length - 1] = '0'
                this.exampleString = this.exampleString.substring(0, (this.exampleString.length - 1))
            } else {
                hintString[0] = '0'
            }
            invalidate()
        }
    }
}
