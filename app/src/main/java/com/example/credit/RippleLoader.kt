package com.example.credit

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

/**
 *
 * All the constants are taken by trail and error.
 * The values are NOT obtained via any calculations
 * Changing any of the values will render the animation out of sync and will require manual correction
 *
 * Components : Ripple View, searchItem and Location
 *
 * Animation is restarted programmatically ( not via repeat mode )
 * searchItem are placed randomly for every restart
 * searchItem become visible when the Initial ripple's radius matches the distance of searchItem from center
 *
 * Animation continues until the stop is called
 * View uses data binding for starting and stopping the animation
 *
 */
class RippleLoader : RelativeLayout {

    private val primaryRipple = RippleView(context)
    private var locationViewBase: View? = null

    private var searchItemAnimationData = SearchItemAnimationData(context)

    private var centerParams: LayoutParams? = null
    private var primaryRippleAnimatorSet: AnimatorSet? = null

    // For devices below 8 keeping in same animator set is causing bugs hence the second one
    private var additionalRipplesAnimatorSet: AnimatorSet? = null
    private var animatorList: ArrayList<Animator>? = null
    private var animatorList2: ArrayList<Animator>? = null

    private val objectSize = convertDpToPixel(32f, context).toInt()
    private val locationIconJumpDistance = convertDpToPixel(10f, context)
    private var rippleStrokeWidth = convertDpToPixel(2f, context)
    private var maxRippleRadius = convertDpToPixel(150f, context)

    private var hasAnimationEnded = false
    private val animationHandler = Handler()
    private var continueAnimation = true

    object Durations {
        internal const val locationUpDelay = 0L
        internal const val locationUpDuration = 200L
        internal const val locationDownDelay = locationUpDuration
        internal const val locationDownDuration = 200L
        internal const val primaryRippleDelay = locationUpDuration + locationDownDuration - 100
        internal const val rippleDurationTime = 3000L
        internal const val searchItemVisibleDuration = 700L
        internal const val additionalRippleDelay = 800L
        internal const val initialDelay = 100L
        internal const val animationStopDuration = 500L
        internal const val animationDuration = 3800L
    }

    object Scales {
        internal const val searchItemMaxScale = 0.9f
        internal const val searchItemBackgroundMaxScale = 0.95f
        internal const val locationIconScale = 1f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        if (isInEditMode) return
        initialiseParams()
        addLocation()
        searchItemAnimationData.addsearchItemAnimations()
        addView(
            primaryRipple,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                .apply {
                    addRule(CENTER_IN_PARENT, TRUE)
                })
        addPrimaryRippleAnimations().apply {
            addUpdateListener {
                if (!hasAnimationEnded) {
                    searchItemAnimationData.updatesearchItemVisibility(primaryRipple.radius)
                }
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!hasAnimationEnded) {
                        searchItemAnimationData.showEndAnimations()
                        hasAnimationEnded = true
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    if (hasAnimationEnded) {
                        hasAnimationEnded = false
                        searchItemAnimationData.randomisesearchItemPositions(
                            locationViewBase?.x ?: 0f,
                            locationViewBase?.y ?: 0f
                        )
                    }
                }

            })
        }
        addAdditionalRipples(1)
        primaryRippleAnimatorSet!!.playTogether(animatorList)
        additionalRipplesAnimatorSet!!.playTogether(animatorList2)
    }

    private fun initialiseParams() {
        centerParams = LayoutParams(objectSize, objectSize)
        centerParams!!.addRule(CENTER_IN_PARENT, TRUE)

        primaryRippleAnimatorSet = AnimatorSet()
        additionalRipplesAnimatorSet = AnimatorSet()
        primaryRippleAnimatorSet!!.interpolator = AccelerateDecelerateInterpolator()
        additionalRipplesAnimatorSet!!.interpolator = AccelerateDecelerateInterpolator()
        animatorList = ArrayList()
        animatorList2 = ArrayList()
    }

    /**
     *  Add the primary ripple view.
     *
     *  The searchItem become visible when radius of this view matches to the distance between
     *  the searchItem and the center of the primary view.
     */
    private fun addPrimaryRippleAnimations(): ObjectAnimator {
        val primaryRippleAnimation = ObjectAnimator.ofPropertyValuesHolder(
            primaryRipple,
            PropertyValuesHolder.ofFloat("radius", 0.0f, maxRippleRadius),
            PropertyValuesHolder.ofFloat("Alpha", 1.0f, 0f)
        ).apply {
            startDelay = Durations.primaryRippleDelay
            duration = Durations.rippleDurationTime
        }

        animatorList!!.add(primaryRippleAnimation)

        return primaryRippleAnimation
    }

    /**
     *  Add the location view with the circular base
     */
    private fun addLocation() {

        locationViewBase = LocationView(context)
        addView(locationViewBase, centerParams)

        val locationView = ImageView(context).apply {
            setImageDrawable(
                VectorDrawableCompat.create(
                    context.resources,
                    R.drawable.ic_location,
                    null
                )
            )
        }
        addView(locationView, centerParams)
        locationView.scaleX = Scales.locationIconScale
        locationView.scaleY = Scales.locationIconScale
        //Moving the location image to center of the location base circle
        locationView.y -= convertDpToPixel(24f, context)

        val locAnim1 = ObjectAnimator.ofFloat(
            locationView,
            "TranslationY",
            locationView.y,
            locationView.y - locationIconJumpDistance
        )
        locAnim1.interpolator = AccelerateInterpolator(5f)
        locAnim1.startDelay = Durations.locationUpDelay
        locAnim1.duration = Durations.locationUpDuration
        animatorList!!.add(locAnim1)


        val locAnim2 = ObjectAnimator.ofFloat(
            locationView,
            "TranslationY",
            locationView.y,
            locationView.y + locationIconJumpDistance
        )
        locAnim2.interpolator = DecelerateInterpolator(5f)
        locAnim2.startDelay = Durations.locationDownDelay
        locAnim2.duration = Durations.locationDownDuration
        animatorList!!.add(locAnim2)
    }

    private fun addAdditionalRipples(count: Int) {
        for (i in 0 until count + 1) {
            val additionalRipple = RippleView(context)
            addView(
                additionalRipple,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    .apply {
                        addRule(CENTER_IN_PARENT, TRUE)
                    }
            )
            val rippleAnimation =
                ObjectAnimator.ofPropertyValuesHolder(
                    additionalRipple,
                    PropertyValuesHolder.ofFloat("radius", 0.0f, maxRippleRadius),
                    PropertyValuesHolder.ofFloat("Alpha", 1.0f, 0f)
                ).apply {
                    startDelay = Durations.primaryRippleDelay + i * Durations.additionalRippleDelay
                    duration = Durations.rippleDurationTime
                }
            animatorList2!!.add(rippleAnimation)
        }
    }

    private inner class RippleView(context: Context) : View(context) {

        val paint = Paint()
        var radius: Float = (min(width, height) / 2).toFloat()
            set(value) {
                field = value
                invalidate()
            }

        init {
            paint.apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = rippleStrokeWidth
                color = ContextCompat.getColor(context, R.color.loader_ripple)
            }
            setWillNotDraw(false)
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawCircle(width / 2f, height / 2f, radius - rippleStrokeWidth, paint)
        }
    }

    private inner class SearchItemView(context: Context) : View(context) {

        val searchItemPaint = Paint()

        init {
            searchItemPaint.apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = ContextCompat.getColor(context, R.color.loader_glow)
            }

        }

        override fun onDraw(canvas: Canvas) {
            val radius = (max(width, height) / 2)
            canvas.drawCircle(width / 2f, height / 2f, radius.toFloat(), searchItemPaint)
        }
    }

    private inner class LocationView(context: Context) : View(context) {

        val paint = Paint()

        init {
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.loader_center)
        }

        override fun onDraw(canvas: Canvas) {
            val radius = (max(width, height) / 2)
            canvas.drawCircle(width / 2f, height / 2f, radius.toFloat() / 2, paint)
        }
    }

    private inner class SearchItemAnimationData(context: Context) {

        private val rn = Random()
        private val searchItemDistanceRange = convertDpToPixel(50f, context).toInt()
        private val searchItemMinimumDistance = convertDpToPixel(25f, context).toInt()

        private val searchItemViewList = ArrayList<ImageView>()
        private val searchItemViewBackgroundList = ArrayList<SearchItemView>()
        private val searchItemViewDistanceList = ArrayList<Double>()
        private val searchItemViewAnimationStarted = ArrayList<Boolean>()
        private val searchItemViewAnimationList = ArrayList<AnimatorSet>()
        private val searchItemViewEndAnimationList = ArrayList<AnimatorSet>()

        fun addSearchItem(
            searchItemImageView: ImageView,
            background: SearchItemView,
            distance: Double,
            animationStarted: Boolean,
            animationStart: AnimatorSet,
            animationEnd: AnimatorSet
        ) {
            searchItemViewList.add(searchItemImageView)
            searchItemViewBackgroundList.add(background)
            searchItemViewDistanceList.add(distance)
            searchItemViewAnimationStarted.add(animationStarted)
            searchItemViewAnimationList.add(animationStart)
            searchItemViewEndAnimationList.add(animationEnd)

        }

        fun showEndAnimations() {
            for (i in 0 until searchItemViewAnimationList.size) {
                searchItemViewEndAnimationList[i].start()
                searchItemViewAnimationList[i].end()
                searchItemViewAnimationStarted[i] = false
            }
        }

        fun randomisesearchItemPositions(centerX: Float, centerY: Float) {
            for (i in 0 until searchItemViewList.size) {
                val angle =
                    rn.nextInt((2 * Math.PI / searchItemViewList.size).toInt()) + 2 * Math.PI / searchItemViewList.size * i
                val xDist =
                    Math.sin(angle) * (rn.nextInt(searchItemDistanceRange) + searchItemMinimumDistance * (i + 1))
                val yDist =
                    Math.cos(angle) * (rn.nextInt(searchItemDistanceRange) + searchItemMinimumDistance * (i + 1))
                searchItemViewList[i].x = (centerX + xDist).toFloat()
                searchItemViewBackgroundList[i].x = (centerX + xDist).toFloat()
                searchItemViewList[i].y = (centerY + yDist).toFloat()
                searchItemViewBackgroundList[i].y = (centerY + yDist).toFloat()
                searchItemViewDistanceList[i] =
                    Math.sqrt(Math.pow(xDist, 2.0) + Math.pow(yDist, 2.0))
                searchItemViewList[i].alpha = 1f
                searchItemViewList[i].scaleX = 0f
                searchItemViewList[i].scaleY = 0f
                searchItemViewBackgroundList[i].alpha = 1f
                searchItemViewBackgroundList[i].scaleX = 0f
                searchItemViewBackgroundList[i].scaleY = 0f
                searchItemViewAnimationStarted[i] = false
            }
        }

        fun updatesearchItemVisibility(radius: Float) {
            for (i in 0 until searchItemViewList.size) {
                if (radius >= searchItemViewDistanceList[i] && !searchItemViewAnimationStarted[i] && radius != maxRippleRadius) {
                    searchItemViewAnimationList[i].start()
                    searchItemViewAnimationStarted[i] = true
                }
            }
        }

        fun addsearchItemAnimations() {

            for (i in 0..2) {
                val searchItemImageView =
                    ImageView(context).apply {
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_search_item
                            )
                        )
                    }
                val background = SearchItemView(context)

                searchItemImageView.scaleX = 0f
                searchItemImageView.scaleY = 0f
                background.scaleX = 0f
                background.scaleY = 0f

                addView(background, centerParams)
                addView(searchItemImageView, centerParams)

                val searchItemtartAnimation = ObjectAnimator.ofPropertyValuesHolder(
                    searchItemImageView,
                    PropertyValuesHolder.ofFloat("ScaleX", 0f, Scales.searchItemMaxScale),
                    PropertyValuesHolder.ofFloat("ScaleY", 0f, Scales.searchItemMaxScale),
                    PropertyValuesHolder.ofFloat("Alpha", 0f, 1f)
                ).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    duration = Durations.searchItemVisibleDuration
                }

                val backgroundStartAnimation = ObjectAnimator.ofPropertyValuesHolder(
                    background,
                    PropertyValuesHolder.ofFloat("ScaleX", 0f, Scales.searchItemBackgroundMaxScale),
                    PropertyValuesHolder.ofFloat("ScaleY", 0f, Scales.searchItemBackgroundMaxScale),
                    PropertyValuesHolder.ofFloat("Alpha", 0f, 1f)
                ).apply {
                    interpolator = OvershootInterpolator(4f)
                    duration = Durations.searchItemVisibleDuration
                }

                val searchItemEndAnimation =
                    ObjectAnimator.ofPropertyValuesHolder(
                        searchItemImageView,
                        PropertyValuesHolder.ofFloat("Alpha", 1f, 0f),
                        PropertyValuesHolder.ofFloat("ScaleX", Scales.searchItemMaxScale, 0f),
                        PropertyValuesHolder.ofFloat("ScaleY", Scales.searchItemMaxScale, 0f)
                    ).apply {
                        interpolator = AccelerateDecelerateInterpolator()
                        duration = Durations.searchItemVisibleDuration
                    }

                val bgEndAnimation = ObjectAnimator.ofPropertyValuesHolder(
                    background,
                    PropertyValuesHolder.ofFloat("Alpha", 1f, 0f),
                    PropertyValuesHolder.ofFloat("ScaleX", Scales.searchItemBackgroundMaxScale, 0f),
                    PropertyValuesHolder.ofFloat("ScaleY", Scales.searchItemBackgroundMaxScale, 0f)
                ).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    duration = Durations.searchItemVisibleDuration
                }
                addSearchItem(searchItemImageView, background, 0.0, false, AnimatorSet().apply {
                    playTogether(searchItemtartAnimation, backgroundStartAnimation)
                }, AnimatorSet().apply {
                    playTogether(searchItemEndAnimation, bgEndAnimation)
                })
            }
        }
    }

    fun startRippleAnimation() {
        if (!primaryRippleAnimatorSet!!.isRunning) {
            primaryRippleAnimatorSet!!.end()
            primaryRippleAnimatorSet!!.start()
            additionalRipplesAnimatorSet!!.end()
            additionalRipplesAnimatorSet!!.start()
        }
    }

    fun start() {
        continueAnimation = true
        this.visibility = View.VISIBLE
        this.translationY = 0f
        this.alpha = 1f
        searchItemAnimationData.randomisesearchItemPositions(
            locationViewBase?.x ?: 0f,
            locationViewBase?.y ?: 0f
        )
        animationHandler.postDelayed(object : Runnable {
            override fun run() {
                startRippleAnimation()
                if (continueAnimation) {
                    animationHandler.postDelayed(this, Durations.animationDuration)
                }
            }
        }, Durations.initialDelay)
    }

    fun stop(showParent: Boolean) {
        continueAnimation = false

        val viewHideAnimation = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("TranslationY", this.y - height),
            PropertyValuesHolder.ofFloat("Alpha", 1.0f, 0f)
        ).apply {
            interpolator = AccelerateInterpolator(2f)
            duration = Durations.animationStopDuration
        }

        val set = AnimatorSet().apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    this@RippleLoader.visibility = View.GONE
                    if (!showParent) {
                        (this@RippleLoader.parent as ViewGroup).visibility = View.GONE
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            playTogether(viewHideAnimation)
        }
        set.start()
    }

    companion object {

        @JvmStatic
        @BindingAdapter("loader_start")
        fun bindStartLoader(view: RippleLoader?, start: Boolean?) {
            (view?.parent as ViewGroup).visibility = View.VISIBLE
            if (start == true) {
                view.start()
            } else {
                view.stop(false)
            }
        }

        fun convertDpToPixel(dp: Float, context: Context): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

}
