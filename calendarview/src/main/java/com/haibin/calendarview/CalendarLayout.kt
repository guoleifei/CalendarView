/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haibin.calendarview


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.AbsListView
import android.widget.LinearLayout


/**
 * 日历布局
 */
class CalendarLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    /**
     * 默认状态
     */
    private val mDefaultStatus: Int

    /**
     * 星期栏
     */
    internal var mWeekBar: WeekBar? = null

    /**
     * 自定义ViewPager，月视图
     */
    internal var mMonthView: MonthViewPager? = null

    /**
     * 自定义的周视图
     */
    internal lateinit var mWeekPager: WeekViewPager

    /**
     * 年视图
     */
    internal var mYearView: YearSelectLayout? = null

    /**
     * ContentView
     */
    internal var mContentView: ViewGroup? = null


    private var mCalendarShowMode = 0

    private val mTouchSlop: Int
    private var mContentViewTranslateY: Int = 0 //ContentView  可滑动的最大距离距离 , 固定
    private var mViewPagerTranslateY = 0// ViewPager可以平移的距离，不代表mMonthView的平移距离

    private var downY: Float = 0.toFloat()
    private var mLastY: Float = 0.toFloat()
    private var isAnimating = false

    /**
     * 内容布局id
     */
    private val mContentViewId: Int

    /**
     * 手速判断
     */
    private val mVelocityTracker: VelocityTracker
    private val mMaximumVelocity: Int

    internal var mItemHeight: Int = 0

    private var mDelegate: CustomCalendarViewDelegate? = null


    /**
     * 是否展开了
     *
     * @return isExpand
     */
    val isExpand: Boolean
        get() = mContentView == null || mMonthView!!.visibility === View.VISIBLE

    /**
     * ContentView是否滚动到顶部
     */
    private val isScrollTop: Boolean
        get() {
            if (mContentView is RecyclerView)
                return (mContentView as RecyclerView).computeVerticalScrollOffset() === 0
            if (mContentView is AbsListView) {
                var result = false
                val listView = mContentView as AbsListView?
                if (listView!!.firstVisiblePosition == 0) {
                    val topChildView = listView.getChildAt(0)
                    result = topChildView.top == 0
                }
                return result
            }
            return mContentView!!.scrollY == 0
        }

    init {
        orientation = LinearLayout.VERTICAL
        val array = context.obtainStyledAttributes(attrs, R.styleable.CalendarLayout)
        mContentViewId = array.getResourceId(R.styleable.CalendarLayout_calendar_content_view_id, 0)
        mDefaultStatus = array.getInt(R.styleable.CalendarLayout_default_status, STATUS_EXPAND)
        mCalendarShowMode = array.getInt(R.styleable.CalendarLayout_calendar_show_mode, CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW)

        array.recycle()
        mVelocityTracker = VelocityTracker.obtain()
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate
        initCalendarPosition(delegate.mSelectedCalendar!!)
        updateContentViewTranslateY()
    }

    /**
     * 初始化当前时间的位置
     *
     * @param cur 当前日期时间
     */
    private fun initCalendarPosition(cur: Calendar) {
        val date = java.util.Calendar.getInstance()
        date.set(cur.year, cur.month - 1, 1)
        val diff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//月第一天为星期几,星期天 == 0,则偏移几天
        val size = diff + cur.day - 1
        setSelectPosition(size)
    }

    /**
     * 当前第几项被选中
     */
    internal fun setSelectPosition(selectPosition: Int) {
        val line = (selectPosition + 7) / 7
        mViewPagerTranslateY = (line - 1) * mItemHeight
    }

    /**
     * 设置选中的周，更新位置
     *
     * @param line line
     */
    internal fun setSelectWeek(line: Int) {
        mViewPagerTranslateY = (line - 1) * mItemHeight
    }


    /**
     * 更新内容ContentView可平移的最大距离
     */
    internal fun updateContentViewTranslateY() {
        if (mDelegate == null)
            return
        val calendar = mDelegate!!.mSelectedCalendar
        if (mDelegate!!.monthViewShowMode == CustomCalendarViewDelegate.MODE_ALL_MONTH) {
            mContentViewTranslateY = 5 * mItemHeight
        } else {
            mContentViewTranslateY = Util.getMonthViewHeight(calendar!!.year, calendar.month, mItemHeight) - mItemHeight
        }
        //已经显示周视图，如果月视图高度是动态改变的，则需要动态平移contentView的高度
        if (mWeekPager.getVisibility() === View.VISIBLE && mDelegate!!.monthViewShowMode != CustomCalendarViewDelegate.MODE_ALL_MONTH) {
            if (mContentView == null)
                return
            mContentView!!.translationY = (-mContentViewTranslateY).toFloat()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mDelegate!!.isShowYearSelectedLayout) {
            return false
        }
        if (mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW)
            return false
        if (mContentView == null)
            return false
        val action = event.action
        val y = event.y
        mVelocityTracker.addMovement(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downY = y
                mLastY = downY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = y - mLastY
                //向上滑动，并且contentView平移到最大距离，显示周视图
                if (dy < 0 && mContentView!!.translationY == (-mContentViewTranslateY).toFloat()) {
                    mContentView!!.onTouchEvent(event)
                    showWeek()
                    return false
                }
                hideWeek()

                //向下滑动，并且contentView已经完全到底部
                if (dy > 0 && mContentView!!.translationY + dy >= 0) {
                    mContentView!!.translationY = 0f
                    translationViewPager()
                    return super.onTouchEvent(event)
                }
                //向上滑动，并且contentView已经平移到最大距离，则contentView平移到最大的距离
                if (dy < 0 && mContentView!!.translationY + dy <= -mContentViewTranslateY) {
                    mContentView!!.translationY = (-mContentViewTranslateY).toFloat()
                    translationViewPager()
                    return super.onTouchEvent(event)
                }
                //否则按比例平移
                mContentView!!.translationY = mContentView!!.translationY + dy
                translationViewPager()
                mLastY = y
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val mYVelocity = velocityTracker.yVelocity
                if (mContentView!!.translationY == 0f || mContentView!!.translationY == mContentViewTranslateY.toFloat()) {

                } else {
                    if (Math.abs(mYVelocity) >= 800) {
                        if (mYVelocity < 0) {
                            shrink()
                        } else {
                            expand()
                        }
                        return super.onTouchEvent(event)
                    }
                    if (event.y - downY > 0) {
                        expand()
                    } else {
                        shrink()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mContentView != null && mMonthView != null) {
            val h = (height - mItemHeight
                    - (if (mDelegate != null)
                mDelegate!!.weekBarHeight
            else
                Util.dipToPx(context, 40f))
                    - Util.dipToPx(context, 1f))
            val heightSpec = View.MeasureSpec.makeMeasureSpec(h,
                    View.MeasureSpec.EXACTLY)
            mContentView!!.measure(widthMeasureSpec, heightSpec)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mMonthView = findViewById<View>(R.id.vp_calendar).findViewById<View>(R.id.vp_calendar) as MonthViewPager
        mWeekPager = findViewById<View>(R.id.vp_week).findViewById<View>(R.id.vp_week) as WeekViewPager
        mContentView = findViewById<View>(mContentViewId) as ViewGroup
        mYearView = findViewById<View>(R.id.selectLayout) as YearSelectLayout
        if (mContentView != null) {
            mContentView!!.overScrollMode = View.OVER_SCROLL_NEVER
        }
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimating) {
            return true
        }
        if (mYearView == null ||
                mContentView == null ||
                mContentView!!.visibility != View.VISIBLE) {
            return super.onInterceptTouchEvent(ev)
        }
        if (mYearView!!.getVisibility() === View.VISIBLE || mDelegate!!.isShowYearSelectedLayout) {
            return super.onInterceptTouchEvent(ev)
        }
        val action = ev.action
        val y = ev.y
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downY = y
                mLastY = downY
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = y - mLastY
                /*
                   如果向上滚动，且ViewPager已经收缩，不拦截事件
                 */
                if (dy < 0 && mContentView!!.translationY == (-mContentViewTranslateY).toFloat()) {
                    return false
                }
                /*
                  * 如果向下滚动，有 2 种情况处理 且y在ViewPager下方
                  * 1、RecyclerView 或者其它滚动的View，当mContentView滚动到顶部时，拦截事件
                  * 2、非滚动控件，直接拦截事件
                */
                if (dy > 0 && mContentView!!.translationY == (-mContentViewTranslateY).toFloat()
                        && y >= Util.dipToPx(context, 98f)) {
                    if (!isScrollTop)
                        return false
                }

                if (dy > 0 && mContentView!!.translationY == 0f && y >= Util.dipToPx(context, 98f)) {
                    return false
                }

                if (Math.abs(dy) > mTouchSlop) {//大于mTouchSlop开始拦截事件，ContentView和ViewPager得到CANCEL事件
                    if (dy > 0 && mContentView!!.translationY <= 0 || dy < 0 && mContentView!!.translationY >= -mContentViewTranslateY) {
                        mLastY = y
                        return true
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
            }
        }
        return super.onInterceptTouchEvent(ev)
    }


    /**
     * 平移ViewPager月视图
     */
    private fun translationViewPager() {
        val percent = mContentView!!.translationY * 1.0f / mContentViewTranslateY
        mMonthView!!.setTranslationY(mViewPagerTranslateY * percent)
    }

    /**
     * 展开
     */
    fun expand(): Boolean {
        if (isAnimating || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW)
            return false
        if (mMonthView!!.getVisibility() !== View.VISIBLE) {
            mWeekPager.setVisibility(View.GONE)
            mMonthView!!.setVisibility(View.VISIBLE)
        }
        val objectAnimator = ObjectAnimator.ofFloat(mContentView,
                "translationY", mContentView!!.translationY, 0f)
        objectAnimator.duration = 240
        objectAnimator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            val percent = currentValue * 1.0f / mContentViewTranslateY
            mMonthView!!.setTranslationY(mViewPagerTranslateY * percent)
            isAnimating = true
        }
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                hideWeek()

            }
        })
        objectAnimator.start()
        return true
    }


    /**
     * 收缩
     */
    fun shrink(): Boolean {
        if (isAnimating) {
            return false
        }
        val objectAnimator = ObjectAnimator.ofFloat(mContentView,"translationY", mContentView!!.translationY.toFloat(), -mContentViewTranslateY.toFloat())
        objectAnimator.setDuration(240)
        objectAnimator.addUpdateListener(AnimatorUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            val percent = currentValue * 1.0f / mContentViewTranslateY
            mMonthView!!.setTranslationY(mViewPagerTranslateY * percent)
            isAnimating = true
        })
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                showWeek()

            }
        })
        objectAnimator.start()
        return true
    }

    /**
     * 初始化状态
     */
    internal fun initStatus() {
        if (mContentView == null) {
            return
        }
        if ((mDefaultStatus == STATUS_SHRINK || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) && mCalendarShowMode != CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW) {
            post {
                val objectAnimator = ObjectAnimator.ofFloat(mContentView,
                        "translationY", mContentView!!.translationY, -mContentViewTranslateY.toFloat())
                objectAnimator.setDuration(0)
                objectAnimator.addUpdateListener(AnimatorUpdateListener { animation ->
                    val currentValue = animation.animatedValue as Float
                    val percent = currentValue * 1.0f / mContentViewTranslateY
                    mMonthView!!.setTranslationY(mViewPagerTranslateY * percent)
                    isAnimating = true
                })
                objectAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        isAnimating = false
                        showWeek()

                    }
                })
                objectAnimator.start()
            }
        }
    }

    /**
     * 隐藏周视图
     */
    private fun hideWeek() {
        mWeekPager.setVisibility(View.GONE)
        mMonthView!!.setVisibility(View.VISIBLE)
    }

    /**
     * 显示周视图
     */
    private fun showWeek() {
        mWeekPager.getAdapter()!!.notifyDataSetChanged()
        mWeekPager.setVisibility(View.VISIBLE)
        mMonthView!!.setVisibility(View.INVISIBLE)
    }


    /**
     * 隐藏内容布局
     */
    internal fun hideContentView() {
        if (mContentView == null)
            return
        mContentView!!.animate()
                .translationY((height - mMonthView!!.getHeight()).toFloat())
                .setDuration(220)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mContentView!!.visibility = View.INVISIBLE
                        mContentView!!.clearAnimation()
                    }
                })
    }

    /**
     * 显示内容布局
     */
    internal fun showContentView() {
        if (mContentView == null)
            return
        mContentView!!.translationY = (height - mMonthView!!.getHeight()).toFloat()
        mContentView!!.visibility = View.VISIBLE
        mContentView!!.animate()
                .translationY(0f)
                .setDuration(180)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
    }

    companion object {

        /**
         * 周月视图
         */
        private val CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW = 0


        /**
         * 仅周视图
         */
        private val CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW = 1

        /**
         * 仅月视图
         */
        private val CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW = 2

        /**
         * 默认展开
         */
        private val STATUS_EXPAND = 0

        /**
         * 默认收缩
         */
        private val STATUS_SHRINK = 1
    }
}
