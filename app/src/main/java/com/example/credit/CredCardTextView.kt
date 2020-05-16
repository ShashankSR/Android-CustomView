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
import androidx.databinding.BindingAdapter

class CredCardTextView : FrameLayout {

    private var inputString: String = ""
    private var inputDimension: Float = 0f
    private var hintString: String = ""
    private var hintPaint: TextPaint? = null
    private var textPaint: TextPaint? = null
    private var onTexChangedListener: CreditCardInterface? = null

    private var tempString: String = ""

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
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                setChar(keyCode, event)
            }
            false
        }

        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
            textSize = inputDimension
            color = Color.WHITE
            typeface = Typeface.MONOSPACE
        }

        hintPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
            textSize = inputDimension
            color = Color.WHITE
            typeface = Typeface.MONOSPACE
        }
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val contentHeight = height - paddingTop / 2

        inputString.let {
            canvas.drawText(
                it,
                paddingLeft.toFloat(),
                contentHeight.toFloat(),
                textPaint!!
            )
        }

        hintString.let {
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
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 && tempString.length < 16) {
            tempString += event.unicodeChar.toChar()
            onTexChangedListener?.onTextChanged(tempString)
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (tempString.isNotEmpty()) {
                tempString = tempString.substring(0, (tempString.length - 1))
                onTexChangedListener?.onTextChanged(tempString)
            }
        }
    }

    fun setText(hint: String?, input: String?) {
        inputString = input ?: ""
        hintString = hint ?: ""
        invalidate()
    }

    fun setOnTextChangedListener(listener: CreditCardInterface) {
        onTexChangedListener = listener
    }

    interface CreditCardInterface {
        fun onTextChanged(input: String)
    }

    companion object {

        @JvmStatic
        @BindingAdapter("bind:hint", "bind:input", requireAll = true)
        fun bindHintText(view: CredCardTextView, hint: String?, input: String?) {
            view.setText(hint, input)
        }

        @JvmStatic
        @BindingAdapter("bind:onTextChanged")
        fun onTexChanged(view: CredCardTextView, listener: CreditCardInterface?) {
            if (listener != null) {
                view.setOnTextChangedListener(listener)
            }
        }
    }
}
