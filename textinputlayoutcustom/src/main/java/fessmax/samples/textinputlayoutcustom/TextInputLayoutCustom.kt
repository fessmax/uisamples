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
import kotlinx.android.synthetic.main.til_layout_relative.view.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.os.Build
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.view.TouchDelegate
import android.graphics.Rect
import android.graphics.Typeface


open class TextInputLayoutCustom : LinearLayout {

    private var backgroundNormalId: Int = View.NO_ID
    private var backgroundErrorId: Int = View.NO_ID

    private var hintText: String? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    @SuppressLint("ResourceType")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.til_layout_relative, this)

        val map = mapOf(
            android.R.attr.hint to
                    fun(a: TypedArray, i: Int) { setHint(a.getString(i)) },

            android.R.attr.inputType to
                    fun(a: TypedArray, i: Int) { initInputType(a.getInt(i, InputType.TYPE_CLASS_TEXT)) },

            R.attr.hintCustomTextAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_hint_text_collapsed.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            R.attr.hintFakeTextAppearance to
                    fun(a: TypedArray, i: Int) {
                        val resId = a.getResourceId(i, View.NO_ID)

                        til_hint_text_expanded.setTextAppearance(context, resId)
                        til_hint_text_fake.setTextAppearance(context, resId)
                    },

            android.R.attr.textAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_edit_text.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            R.attr.errorCustomTextAppearance to
                    fun(a: TypedArray, i: Int) {
                        til_error_text.setTextAppearance(
                            context,
                            a.getResourceId(i, View.NO_ID)
                        )
                    },

            R.attr.passwordCustomToggleDrawable to
                    fun(a: TypedArray, i: Int) {
                        til_pass_toggle.setBackgroundResource(
                            a.getResourceId(
                                i,
                                View.NO_ID
                            )
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
            R.attr.selectionCustomTintColor to
                    fun(a: TypedArray, i: Int) {
                        setSelectionColor(a.getColor(i, View.NO_ID))
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

        //increase touch area for toggle
        val parent = til_pass_toggle.parent as View  // button: the view you want to enlarge hit area
        parent.post {
            val rect = Rect()
            til_pass_toggle.getHitRect(rect)
            rect.top -= 100    // increase top hit area
            rect.left -= 100   // increase left hit area
            rect.bottom += 100 // increase bottom hit area
            rect.right += 100  // increase right hit area
            parent.touchDelegate = TouchDelegate(rect, til_pass_toggle)
        }
    }

    private fun setSelectionColor(color: Int) {
        try {
            // Left
            val fCursorDrawableRes = TextView::class.java.getDeclaredField("mTextSelectHandleRes")
            fCursorDrawableRes.isAccessible = true
            val mCursorDrawable = ContextCompat.getDrawable(context, fCursorDrawableRes.getInt(til_edit_text))

            // Left
            val fCursorDrawableLeftRes = TextView::class.java.getDeclaredField("mTextSelectHandleLeftRes")
            fCursorDrawableLeftRes.isAccessible = true
            val mCursorDrawableLeft = ContextCompat.getDrawable(context, fCursorDrawableLeftRes.getInt(til_edit_text))

            // Right
            val fCursorDrawableRightRes = TextView::class.java.getDeclaredField("mTextSelectHandleRightRes")
            fCursorDrawableRightRes.isAccessible = true
            val mCursorDrawableRight = ContextCompat.getDrawable(context, fCursorDrawableRightRes.getInt(til_edit_text))

            mCursorDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            mCursorDrawableLeft?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            mCursorDrawableRight?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        } catch (error: NoSuchFieldException) {
            Log.e("setSelectionColor", error.message, error)
        }
    }

    private var inputType: Int = InputType.TYPE_NULL
    private fun isPassword(): Boolean {
        return inputType.and(InputType.TYPE_TEXT_VARIATION_PASSWORD) > 0
    }


    private fun initInputType(inputType: Int) {
        this.inputType = inputType
        //check password inputType
        if (isPassword()) {
            til_edit_text.transformationMethod = PasswordTransformationMethod.getInstance()
            til_pass_toggle.visibility = View.VISIBLE
            til_pass_toggle.setOnCheckedChangeListener { _, flag ->
                til_edit_text.transformationMethod =
                    if (flag) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            }
        } else {
            til_pass_toggle.visibility = View.GONE
        }
    }

    private fun setupAttrs(context: Context, attrs: AttributeSet?, map: Map<Int, (TypedArray, Int) -> Unit>) {
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
        til_hint_text_collapsed.text = hint
        til_hint_text_collapsed.visibility = View.INVISIBLE
        til_hint_text_expanded.text = hint
        til_hint_text_expanded.visibility = View.INVISIBLE
        til_hint_text_fake.text = hint
        onFocusChangeListener = null
//        til_edit_text.setOnFocusChangeListener { view, b -> updateHintPosition(b) }
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

    private fun animateHint(collapsing: Boolean) {
        val ANIMATION_DURATION = 200L
        val fromY: Float
        val toY: Float
        val fromFont: Float
        val toFont: Float

        if (collapsing) {
            fromY = til_hint_text_fake.y
            toY = til_hint_text_collapsed.y + til_hint_text_collapsed.paddingTop

            fromFont = til_hint_text_fake.textSize
            toFont = til_hint_text_collapsed.textSize
        } else {
            fromY = til_hint_text_fake.y
            toY = til_hint_text_expanded.y

            fromFont = til_hint_text_fake.textSize
            toFont = til_hint_text_expanded.textSize
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

    override fun clearFocus() {
        super.clearFocus()
        til_edit_text.clearFocus()
    }

    override fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        til_edit_text.setOnFocusChangeListener(object : OnFocusChangeListener {
            override fun onFocusChange(view: View?, hasFocus: Boolean) {
                listener?.onFocusChange(view, hasFocus)
                updateHintPosition(hasFocus)
            }

        })
    }

    fun addTextChangedListener(watcher: TextWatcher) {
        til_edit_text.addTextChangedListener(watcher)
    }

    fun removeTextChangedListener(watcher: TextWatcher) {
        til_edit_text.removeTextChangedListener(watcher)
    }

    fun getText(): String {
        return til_edit_text.text.toString()
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