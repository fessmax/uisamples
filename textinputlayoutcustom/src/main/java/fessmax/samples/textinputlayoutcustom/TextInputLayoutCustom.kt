package fessmax.samples.textinputlayoutcustom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.til_layout.view.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.widget.TextView


class TextInputLayoutCustom : LinearLayout {

    private var backgroundNormalId: Int = View.NO_ID
    private var backgroundErrorId: Int = View.NO_ID

    private var hintText: String? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    @SuppressLint("ResourceType")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.til_layout, this)

        val map = mapOf(
            android.R.attr.hint to
                    fun(a: TypedArray, i: Int) { setHint(a.getString(i)) },

            android.R.attr.inputType to
                    fun(a: TypedArray, i: Int) { initInputType(a.getInt(i, InputType.TYPE_CLASS_TEXT)) },

            R.attr.hintTextAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_hint_text.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            R.attr.hintFakeTextAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_hint_text_fake.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            android.R.attr.textAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_edit_text.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            R.attr.errorTextAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_error_text.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            R.attr.errorDrawable to
                    fun(a: TypedArray, i: Int) { til_error_icon.setImageDrawable(a.getDrawable(i)) },

            R.attr.colorControlNormal to
                    fun(a: TypedArray, i: Int) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            til_edit_text.background.setTint(a.getColor(i, View.NO_ID))
                        }
                    },

            android.R.attr.textCursorDrawable to
                    fun(a: TypedArray, i: Int) {
                        try {
                            til_edit_text.setTextCursorDrawable(a.getResourceId(i, View.NO_ID))
                        } catch (e: NoSuchMethodError) {
                            Log.e("setTextCursorDrawable", "${e.message} \r\n replace with reflection")
                            setTextCursorDrawable(a.getResourceId(i, View.NO_ID))
                        }
                    },

            R.styleable.TextInputLayoutCustom[R.styleable.TextInputLayoutCustom_backgroundNormal] to
                    fun(a: TypedArray, i: Int) {
                        backgroundNormalId = a.getResourceId(i, View.NO_ID)
                        setBackground(backgroundNormalId)
                    },

            R.styleable.TextInputLayoutCustom[R.styleable.TextInputLayoutCustom_backgroundError] to
                    fun(a: TypedArray, i: Int) {
                        backgroundErrorId = a.getResourceId(i, View.NO_ID)
                    }
        )
        setupAttrs(context, attrs, map)
    }


    private fun isPassword(): Boolean {
        return til_edit_text.inputType.and(InputType.TYPE_TEXT_VARIATION_PASSWORD) > 0
    }

    private fun initInputType(inputType: Int) {
        til_edit_text.inputType = inputType
        //check password inputType
        if (isPassword()) {
            til_pass_toggle.visibility = View.VISIBLE
            til_pass_toggle.setOnCheckedChangeListener { _, flag ->
                til_edit_text.transformationMethod =
                    if (flag) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            }
        } else {
            til_pass_toggle.visibility = View.GONE
        }
    }

    fun setupAttrs(context: Context, attrs: AttributeSet?, map: Map<Int, (TypedArray, Int) -> Unit>) {
        // for android limitation, you need to sort all attrs you want to find in ascending order
        // then find attrs, otherwise some attrs cannot be found
        // https://developer.android.com/reference/android/content/res/Resources.Theme.html#obtainStyledAttributes(android.util.AttributeSet, int[], int, int)
        val sorted = map.toSortedMap()
        val set = sorted.keys.toIntArray().apply { sort() }

        // extract out attribute from your layout xml
        val a = context.obtainStyledAttributes(attrs, set)

        (0 until a.indexCount)
            .map(a::getIndex)
            .forEach {
                // map to what you want to do
                sorted[set[it]]?.invoke(a, it)
            }

        a.recycle()
    }

    fun setError(error: String?) {
        if (error.isNullOrBlank()) {
            hideError()
        } else {
            showError(error)
        }
    }

    private fun setBackground(resId: Int) {
        til_main_layout.setBackgroundResource(resId)
    }

    private fun showError(error: String) {
        til_error_text.text = error
        setBackground(backgroundErrorId)
        til_error_text.visibility = View.VISIBLE
        if (!isPassword()) til_error_icon.visibility = View.VISIBLE
    }

    private fun hideError() {
        setBackground(backgroundNormalId)
        til_error_text.visibility = View.GONE
        til_error_icon.visibility = View.GONE
    }

    private fun setHint(hint: String?) {
        hintText = hint
        til_hint_text.text = hint
        til_hint_text.visibility = View.INVISIBLE
        til_hint_text_fake.text = hint
        til_edit_text.setOnFocusChangeListener { view, b -> updateHintPosition(b) }
    }

    private fun updateHintPosition(onEditTextFocused: Boolean) {
        if (onEditTextFocused) {
            //Hide fake hint and show normal
            animateHint(true)
        } else {
            if (til_edit_text.text.isNullOrEmpty()) {
                //Hide normal hint and show fake
                animateHint(false)
            }
        }
    }

    private var defaultHintPosition: Float = 0.0f
    private var defaultHintFont: Float = 0.0f
    private fun animateHint(collapsing: Boolean) {
        val ANIMATION_DURATION = 200L
        val fromY: Float
        val toY: Float
        val fromFont: Float
        val toFont: Float

        if (defaultHintPosition == 0.0f) defaultHintPosition = til_hint_text_fake.y
        if (defaultHintFont == 0.0f) defaultHintFont = til_hint_text_fake.textSize

        if (collapsing) {
            fromY = til_hint_text_fake.y
            toY = til_hint_text.y + til_hint_text.paddingTop

            fromFont = til_hint_text_fake.textSize
            toFont = til_hint_text.textSize
        } else {
            fromY = til_hint_text_fake.y
            toY = defaultHintPosition

            fromFont = til_hint_text_fake.textSize
            toFont = defaultHintFont
        }

        val moveAnimation =
            ObjectAnimator.ofFloat(til_hint_text_fake, View.Y, fromY, toY).apply { duration = ANIMATION_DURATION }
        val fontSizeAnimation = ValueAnimator.ofFloat(fromFont, toFont).apply { duration = ANIMATION_DURATION }
        fontSizeAnimation.addUpdateListener {
            val value = it.animatedValue as Float
            til_hint_text_fake.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveAnimation, fontSizeAnimation)

        animatorSet.start()

    }

    private fun setTextCursorDrawable(resId: Int) {
        try {
            val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            f.isAccessible = true
            f.set(til_edit_text, resId)
        } catch (ignored: Exception) {
        }
    }


}