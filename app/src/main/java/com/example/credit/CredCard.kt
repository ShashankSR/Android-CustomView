package com.example.credit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
        val attr = context.obtainStyledAttributes(
            attrs, R.styleable.CredCard, defStyle, 0
        )

        attr.let {
            exampleString = ""
            exampleColor = it.getColor(
                R.styleable.CredCard_exampleColor,
                exampleColor
            )

            exampleDimension = it.getDimension(
                R.styleable.CredCard_exampleDimension,
                exampleDimension
            )

            it.recycle()
        }

        isFocusable = true
        isFocusableInTouchMode = true
        setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    setChar(keyCode, event)
                }
                return false
            }
        })

        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        hintPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint?.let {
            it.textSize = exampleDimension
            it.color = Color.BLACK
            it.typeface = Typeface.create("Arial", Typeface.BOLD)
        }
        hintPaint?.let {
            it.textSize = exampleDimension
            it.color = Color.GRAY
            it.typeface = Typeface.create("Arial", Typeface.BOLD)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val contentHeight = height - paddingTop - paddingBottom

        exampleString.let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                (paddingTop + contentHeight / 2).toFloat(),
                textPaint!!
            )
        }

        hintString.joinToString("").let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                (paddingTop + contentHeight / 2).toFloat(),
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
            exampleString = exampleString + event.getUnicodeChar().toChar()
            hintString[exampleString.length - 1] = '1'
            invalidate()
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (exampleString.isNotEmpty()) {
                hintString[exampleString.length - 1] = '0'
                exampleString = exampleString.substring(0, (exampleString.length - 1))
            } else {
                hintString[0] = '0'
            }
            invalidate()
        }
    }
}
