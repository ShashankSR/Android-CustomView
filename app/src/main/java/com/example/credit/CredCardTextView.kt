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
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout

class CredCardTextView : FrameLayout {

    private var inputString: String = ""
    private var inputDimension: Float = 0f
    private var hintString = Array(16) { '0' }
    private var hintPaint: TextPaint? = null
    private var textPaint: TextPaint? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.CredCardTextView, defStyle, 0)
        inputDimension = attr.getDimension(
            R.styleable.CredCardTextView_exampleDimension,
            inputDimension
        )
        attr.recycle()


        isFocusable = true
        isFocusableInTouchMode = true
        setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                setChar(keyCode, event)
            }
            false
        }

        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
            textSize = inputDimension
            color = Color.BLACK
            typeface = Typeface.MONOSPACE
        }

        hintPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
            textSize = inputDimension
            color = Color.GRAY
            typeface = Typeface.MONOSPACE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val contentHeight = height - paddingTop/2

        inputString.let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                contentHeight.toFloat(),
                textPaint!!
            )
        }

        hintString.joinToString("").let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                contentHeight.toFloat(),
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

    private fun setChar(keyCode: Int, event: KeyEvent) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 && inputString.length < 16) {
            inputString = inputString + event.getUnicodeChar().toChar()
            hintString[inputString.length - 1] = '1'
            invalidate()
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (inputString.isNotEmpty()) {
                hintString[inputString.length - 1] = '0'
                inputString = inputString.substring(0, (inputString.length - 1))
            } else {
                hintString[0] = '0'
            }
            invalidate()
        }
    }
}