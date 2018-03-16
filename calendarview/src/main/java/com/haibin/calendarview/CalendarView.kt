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
import android.content.Context
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout

/**
 * 日历布局
 * 各个类使用包权限，避免不必要的public
 */
class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * 使用google官方推荐的方式抽取自定义属性
     */
    private val mDelegate: CustomCalendarViewDelegate

    /**
     * 自定义自适应高度的ViewPager
     */
    private var mMonthPager: MonthViewPager? = null

    /**
     * 日历周视图
     */
    private var mWeekPager: WeekViewPager? = null

    /**
     * 星期栏的线
     */
    private var mWeekLine: View? = null

    /**
     * 月份快速选取
     */
    private var mSelectLayout: YearSelectLayout? = null

    /**
     * 星期栏
     */
    private var mWeekBar: WeekBar? = null

    /**
     * 日历外部收缩布局
     */
    internal var mParentLayout: CalendarLayout? = null

    /**
     * 获取当天
     *
     * @return 返回今天
     */
    val curDay: Int
        get() = mDelegate.currentDay!!.day

    /**
     * 获取本月
     *
     * @return 返回本月
     */
    val curMonth: Int
        get() = mDelegate.currentDay!!.month

    /**
     * 获取本年
     *
     * @return 返回本年
     */
    val curYear: Int
        get() = mDelegate.currentDay!!.year


    /**
     * 年月份选择视图是否打开
     *
     * @return true or false
     */
    val isYearSelectLayoutVisible: Boolean
        get() = mSelectLayout!!.getVisibility() === View.VISIBLE

    /**
     * 获取选择的日期
     */
    val selectedCalendar: Calendar?
        get() = mDelegate.mSelectedCalendar

    init {
        mDelegate = CustomCalendarViewDelegate(context, attrs)
        init(context)
    }

    /**
     * 初始化
     *
     * @param context context
     */
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.cv_layout_calendar_view, this, true)
        val frameContent = findViewById<View>(R.id.frameContent) as FrameLayout
        this.mWeekPager = findViewById<View>(R.id.vp_week) as WeekViewPager
        this.mWeekPager!!.setup(mDelegate)

        if (TextUtils.isEmpty(mDelegate.weekBarClass)) {
            this.mWeekBar = WeekBar(getContext())
        } else {
            try {
                val cls = Class.forName(mDelegate.weekBarClass)
                val constructor = cls.getConstructor(Context::class.java)
                mWeekBar = constructor.newInstance(getContext()) as WeekBar
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        frameContent.addView(mWeekBar, 2)
        mWeekBar!!.setup(mDelegate)

        this.mWeekLine = findViewById(R.id.line)
        this.mWeekLine!!.setBackgroundColor(mDelegate.weekLineBackground)

        this.mMonthPager = findViewById<View>(R.id.vp_calendar) as MonthViewPager
        this.mMonthPager!!.mWeekPager = mWeekPager
        this.mMonthPager!!.mWeekBar = mWeekBar
        val params = this.mMonthPager!!.getLayoutParams() as FrameLayout.LayoutParams
        params.setMargins(0, mDelegate.weekBarHeight + Util.dipToPx(context, 1f), 0, 0)
        mWeekPager!!.setLayoutParams(params)

        mSelectLayout = findViewById<View>(R.id.selectLayout) as YearSelectLayout
        mSelectLayout!!.setBackgroundColor(mDelegate.yearViewBackground)
        mSelectLayout!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (mWeekPager!!.getVisibility() === View.VISIBLE) {
                    return
                }
                if (mDelegate.mYearChangeListener != null) {
                    mDelegate.mYearChangeListener!!.onYearChange(position + mDelegate.minYear)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mDelegate.mInnerListener = object : OnInnerDateSelectedListener {
            override fun onMonthDateSelected(calendar: Calendar, isClick: Boolean) {
                if (calendar.year == mDelegate.currentDay!!.year &&
                        calendar.month == mDelegate.currentDay!!.month
                        && mMonthPager!!.getCurrentItem() !== mDelegate.mCurrentMonthViewItem) {
                    return
                }
                mDelegate.mSelectedCalendar = calendar
                mWeekPager!!.updateSelected(mDelegate.mSelectedCalendar!!, false)
                mMonthPager!!.updateSelected()
                if (mWeekBar != null) {
                    mWeekBar!!.onDateSelected(calendar, isClick)
                }
            }

            override fun onWeekDateSelected(calendar: Calendar, isClick: Boolean) {
                mDelegate.mSelectedCalendar = calendar
                val y = calendar.year - mDelegate.minYear
                val position = 12 * y + mDelegate.mSelectedCalendar!!.month - mDelegate.minYearMonth
                mMonthPager!!.setCurrentItem(position)
                mMonthPager!!.updateSelected()
                if (mWeekBar != null) {
                    mWeekBar!!.onDateSelected(calendar, isClick)
                }
            }
        }

        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate()
        mWeekBar!!.onDateSelected(mDelegate.mSelectedCalendar!!, false)

        val mCurYear = mDelegate.mSelectedCalendar!!.year
        mMonthPager!!.setup(mDelegate)
        mMonthPager!!.setCurrentItem(mDelegate.mCurrentMonthViewItem)
        mSelectLayout!!.setOnMonthSelectedListener(object : YearRecyclerView.OnMonthSelectedListener {
            override fun onMonthSelected(year: Int, month: Int) {
                val position = 12 * (year - mDelegate.minYear) + month - mDelegate.minYearMonth
                mDelegate.isShowYearSelectedLayout = false
                closeSelectLayout(position)
            }
        })
        mSelectLayout!!.setup(mDelegate)
        mWeekPager!!.updateSelected(mDelegate.mSelectedCalendar!!, false)
    }

    /**
     * 设置日期范围
     *
     * @param minYear      最小年份
     * @param minYearMonth 最小年份对应月份
     * @param maxYear      最大月份
     * @param maxYearMonth 最大月份对应月份
     */
    fun setRange(minYear: Int, minYearMonth: Int,
                 maxYear: Int, maxYearMonth: Int) {
        mDelegate.setRange(minYear, minYearMonth,
                maxYear, maxYearMonth)
        mWeekPager!!.notifyDataSetChanged()
        mSelectLayout!!.notifyDataSetChanged()
        mMonthPager!!.notifyDataSetChanged()
        if (Util.isCalendarInRange(mDelegate.mSelectedCalendar!!, mDelegate)) {
            scrollToCalendar(mDelegate.mSelectedCalendar!!.year,
                    mDelegate.mSelectedCalendar!!.month,
                    mDelegate.mSelectedCalendar!!.day)

        } else {
            scrollToCurrent()
        }

    }


    /**
     * 打开日历年月份快速选择
     *
     * @param year 年
     */
    fun showYearSelectLayout(year: Int) {
        showSelectLayout(year)
    }

    /**
     * 打开日历年月份快速选择
     * 请使用 showYearSelectLayout(final int year) 代替，这个没什么，越来越规范
     *
     * @param year 年
     */
    @Deprecated("")
    fun showSelectLayout(year: Int) {
        if (mParentLayout != null && mParentLayout!!.mContentView != null) {
            if (!mParentLayout!!.isExpand) {
                mParentLayout!!.expand()
                return
            }
        }
        mWeekPager!!.setVisibility(View.GONE)
        mDelegate.isShowYearSelectedLayout = true
        if (mParentLayout != null) {
            mParentLayout!!.hideContentView()
        }
        mWeekBar!!.animate()
                .translationY((-mWeekBar!!.getHeight()).toFloat())
                .setInterpolator(LinearInterpolator())
                .setDuration(260)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mWeekBar!!.setVisibility(View.GONE)
                        mSelectLayout!!.setVisibility(View.VISIBLE)
                        mSelectLayout!!.scrollToYear(year, false)
                        if (mParentLayout != null && mParentLayout!!.mContentView != null) {
                            mParentLayout!!.expand()
                        }
                    }
                })

        mMonthPager!!.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(260)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
    }

    /**
     * 关闭年月视图选择布局
     */
    fun closeYearSelectLayout() {
        val position = 12 * (mDelegate.mSelectedCalendar!!.year - mDelegate.minYear) + mDelegate.mSelectedCalendar!!.month - mDelegate.minYearMonth
        closeSelectLayout(position)
    }

    /**
     * 关闭日历布局，同时会滚动到指定的位置
     *
     * @param position 某一年
     */
    private fun closeSelectLayout(position: Int) {
        mSelectLayout!!.setVisibility(View.GONE)
        mWeekBar!!.setVisibility(View.VISIBLE)
        if (position == mMonthPager!!.getCurrentItem()) {
            if (mDelegate.mDateSelectedListener != null) {
                mDelegate.mDateSelectedListener!!.onDateSelected(mDelegate.mSelectedCalendar!!, false)
            }
        } else {
            mMonthPager!!.setCurrentItem(position, false)
        }
        mWeekBar!!.animate()
                .translationY(0f)
                .setInterpolator(LinearInterpolator())
                .setDuration(280)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mWeekBar!!.setVisibility(View.VISIBLE)
                    }
                })
        mMonthPager!!.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mMonthPager!!.setVisibility(View.VISIBLE)
                        mMonthPager!!.clearAnimation()
                        if (mParentLayout != null) {
                            mParentLayout!!.showContentView()
                        }
                    }
                })
    }

    /**
     * 滚动到当前
     *
     * @param smoothScroll smoothScroll
     */
    @JvmOverloads
    fun scrollToCurrent(smoothScroll: Boolean = false) {
        if (!Util.isCalendarInRange(mDelegate.currentDay!!, mDelegate)) {
            return
        }
        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate()
        mWeekBar!!.onDateSelected(mDelegate.mSelectedCalendar!!, false)
        mWeekPager!!.scrollToCurrent(smoothScroll)
        mMonthPager!!.scrollToCurrent(smoothScroll)
        mSelectLayout!!.scrollToYear(mDelegate.currentDay!!.year, smoothScroll)
    }


    /**
     * 滚动到下一个月
     */
    fun scrollToNext() {
        scrollToNext(false)
    }

    /**
     * 滚动到下一个月
     *
     * @param smoothScroll smoothScroll
     */
    fun scrollToNext(smoothScroll: Boolean) {
        if (mWeekPager!!.getVisibility() === View.VISIBLE) {
            mWeekPager!!.setCurrentItem(mWeekPager!!.getCurrentItem() + 1, smoothScroll)
        } else {
            mMonthPager!!.setCurrentItem(mMonthPager!!.getCurrentItem() + 1, smoothScroll)
        }

    }

    /**
     * 滚动到上一个月
     */
    fun scrollToPre() {
        scrollToPre(false)
    }

    /**
     * 滚动到上一个月
     *
     * @param smoothScroll smoothScroll
     */
    fun scrollToPre(smoothScroll: Boolean) {
        if (mWeekPager!!.getVisibility() === View.VISIBLE) {
            mWeekPager!!.setCurrentItem(mWeekPager!!.getCurrentItem() - 1, smoothScroll)
        } else {
            mMonthPager!!.setCurrentItem(mMonthPager!!.getCurrentItem() - 1, smoothScroll)
        }
    }

    /**
     * 滚动到指定日期
     *
     * @param year  year
     * @param month month
     * @param day   day
     */
    fun scrollToCalendar(year: Int, month: Int, day: Int) {
        scrollToCalendar(year, month, day, false)
    }

    /**
     * 滚动到指定日期
     *
     * @param year         year
     * @param month        month
     * @param day          day
     * @param smoothScroll smoothScroll
     */
    fun scrollToCalendar(year: Int, month: Int, day: Int, smoothScroll: Boolean) {
        if (mWeekPager!!.getVisibility() === View.VISIBLE) {
            mWeekPager!!.scrollToCalendar(year, month, day, smoothScroll)
        } else {
            mMonthPager!!.scrollToCalendar(year, month, day, smoothScroll)
        }
    }

    /**
     * 滚动到某一年
     *
     * @param year 快速滚动的年份
     */
    fun scrollToYear(year: Int) {
        scrollToYear(year, false)
    }

    /**
     * 滚动到某一年
     *
     * @param year         快速滚动的年份
     * @param smoothScroll smoothScroll
     */
    fun scrollToYear(year: Int, smoothScroll: Boolean) {
        mMonthPager!!.setCurrentItem(12 * (year - mDelegate.minYear) + mDelegate.currentDay!!.month - mDelegate.minYearMonth, smoothScroll)
        mSelectLayout!!.scrollToYear(year, smoothScroll)
    }


    /**
     * 年份改变事件
     *
     * @param listener listener
     */
    fun setOnYearChangeListener(listener: OnYearChangeListener) {
        this.mDelegate.mYearChangeListener = listener
    }

    /**
     * 月份改变事件
     *
     * @param listener listener
     */
    fun setOnMonthChangeListener(listener: OnMonthChangeListener) {
        this.mDelegate.mMonthChangeListener = listener
    }

    /**
     * 设置日期选中事件
     *
     * @param listener 日期选中事件
     */
    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        this.mDelegate.mDateSelectedListener = listener
        if (mDelegate.mDateSelectedListener != null) {
            if (!Util.isCalendarInRange(mDelegate.mSelectedCalendar!!, mDelegate)) {
                return
            }
            mDelegate.mDateSelectedListener!!.onDateSelected(mDelegate.mSelectedCalendar!!, false)
        }
    }

    /**
     * 日期长按事件
     *
     * @param listener listener
     */
    fun setOnDateLongClickListener(listener: OnDateLongClickListener) {
        this.mDelegate.mDateLongClickListener = listener
    }

    /**
     * 初始化时初始化日历卡默认选择位置
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent != null && parent is CalendarLayout) {
            mParentLayout = parent as CalendarLayout
            mParentLayout!!.mItemHeight = mDelegate.calendarItemHeight
            mMonthPager!!.mParentLayout = mParentLayout
            mWeekPager!!.mParentLayout = mParentLayout
            mParentLayout!!.mWeekBar = mWeekBar
            mParentLayout!!.setup(mDelegate)
            mParentLayout!!.initStatus()
        }
    }


    /**
     * 标记哪些日期有事件
     *
     * @param mSchemeDate mSchemeDate 通过自己的需求转换即可
     */
    fun setSchemeDate(mSchemeDate: MutableList<Calendar>) {
        this.mDelegate.mSchemeDate = mSchemeDate
        mMonthPager!!.updateScheme()
        mWeekPager!!.updateScheme()
    }

    /**
     * 设置背景色
     *
     * @param yearViewBackground 年份卡片的背景色
     * @param weekBackground     星期栏背景色
     * @param lineBg             线的颜色
     */
    fun setBackground(yearViewBackground: Int, weekBackground: Int, lineBg: Int) {
        mWeekBar!!.setBackgroundColor(weekBackground)
        mSelectLayout!!.setBackgroundColor(yearViewBackground)
        mWeekLine!!.setBackgroundColor(lineBg)
    }


    /**
     * 设置文本颜色
     *
     * @param currentDayTextColor      今天字体颜色
     * @param curMonthTextColor        当前月份字体颜色
     * @param otherMonthColor          其它月份字体颜色
     * @param curMonthLunarTextColor   当前月份农历字体颜色
     * @param otherMonthLunarTextColor 其它农历字体颜色
     */
    fun setTextColor(
            currentDayTextColor: Int,
            curMonthTextColor: Int,
            otherMonthColor: Int,
            curMonthLunarTextColor: Int,
            otherMonthLunarTextColor: Int) {
        mDelegate.setTextColor(currentDayTextColor, curMonthTextColor, otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor)
    }

    /**
     * 设置选择的效果
     *
     * @param selectedThemeColor     选中的标记颜色
     * @param selectedTextColor      选中的字体颜色
     * @param selectedLunarTextColor 选中的农历字体颜色
     */
    fun setSelectedColor(selectedThemeColor: Int, selectedTextColor: Int, selectedLunarTextColor: Int) {
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor)
    }

    /**
     * 定制颜色
     *
     * @param selectedThemeColor 选中的标记颜色
     * @param schemeColor        标记背景色
     */
    fun setThemeColor(selectedThemeColor: Int, schemeColor: Int) {
        mDelegate.setThemeColor(selectedThemeColor, schemeColor)
    }

    /**
     * 设置标记的色
     *
     * @param schemeColor     标记背景色
     * @param schemeTextColor 标记字体颜色
     */
    fun setSchemeColor(schemeColor: Int, schemeTextColor: Int, schemeLunarTextColor: Int) {
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor)
    }

    /**
     * 设置年视图的颜色
     * @param yearViewMonthTextColor 年视图月份颜色
     * @param yearViewDayTextColor 年视图天的颜色
     * @param yarViewSchemeTextColor 年视图标记颜色
     */
    fun setYearViewTextColor(yearViewMonthTextColor: Int, yearViewDayTextColor: Int, yarViewSchemeTextColor: Int) {
        mDelegate.setYearViewTextColor(yearViewMonthTextColor, yearViewDayTextColor, yarViewSchemeTextColor)
    }

    /**
     * 设置星期栏的背景和字体颜色
     *
     * @param weekBackground 背景色
     * @param weekTextColor  字体颜色
     */
    fun setWeeColor(weekBackground: Int, weekTextColor: Int) {
        mWeekBar!!.setBackgroundColor(weekBackground)
        mWeekBar!!.setTextColor(weekTextColor)
    }


    /**
     * 更新界面，
     * 重新设置颜色等都需要调用该方法
     */
    fun update() {
        mSelectLayout!!.update()
        mMonthPager!!.updateScheme()
        mWeekPager!!.updateScheme()
    }


    /**
     * 年份改变事件，快速年份切换
     */
    interface OnYearChangeListener {
        fun onYearChange(year: Int)
    }

    /**
     * 月份切换事件
     */
    interface OnMonthChangeListener {
        fun onMonthChange(year: Int, month: Int)
    }

    /**
     * 内部日期选择，不暴露外部使用
     * 主要是用于更新日历CalendarLayout位置
     */
    interface OnInnerDateSelectedListener {
        /**
         * 月视图点击
         *
         * @param calendar calendar
         * @param isClick  是否是点击
         */
        fun onMonthDateSelected(calendar: Calendar, isClick: Boolean)

        /**
         * 周视图点击
         *
         * @param calendar calendar
         */
        fun onWeekDateSelected(calendar: Calendar, isClick: Boolean)
    }

    /**
     * 外部日期选择事件
     */
    interface OnDateSelectedListener {
        fun onDateSelected(calendar: Calendar, isClick: Boolean)
    }


    /**
     * 外部日期长按事件
     */
    interface OnDateLongClickListener {
        fun onDateLongClick(calendar: Calendar)
    }


}
/**
 * 滚动到当前
 */
