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

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup


/**
 * 月份切换ViewPager，自定义适应高度
 */
class MonthViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private var mMonthCount: Int = 0

    private var mDelegate: CustomCalendarViewDelegate? = null

    private var mNextViewHeight: Int = 0
    private var mPreViewHeight: Int = 0
    private var mCurrentViewHeight: Int = 0

    internal var mParentLayout: CalendarLayout? = null

    internal var mWeekPager: WeekViewPager? = null

    internal var mWeekBar: WeekBar? = null

    /**
     * 是否使用滚动到某一天
     */
    private var isUsingScrollToCalendar = false

    /**
     * 初始化
     *
     * @param delegate delegate
     */
    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate

        updateMonthViewHeight(mDelegate!!.currentDay!!.year,
                mDelegate!!.currentDay!!.month)

        val params = layoutParams
        params.height = mCurrentViewHeight
        layoutParams = params
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        mMonthCount = 12 * (mDelegate!!.maxYear - mDelegate!!.minYear) - mDelegate!!.minYearMonth + 1 +
                mDelegate!!.maxYearMonth
        adapter = MonthViewPagerAdapter()
        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (mDelegate!!.monthViewShowMode == CustomCalendarViewDelegate.MODE_ALL_MONTH) {
                    return
                }
                val height: Int
                if (position < currentItem) {//右滑-1
                    height = (mPreViewHeight * (1 - positionOffset) + mCurrentViewHeight * positionOffset).toInt()
                } else {//左滑+！
                    height = (mCurrentViewHeight * (1 - positionOffset) + mNextViewHeight * positionOffset).toInt()
                }
                val params = layoutParams
                params.height = height
                layoutParams = params
            }

            override fun onPageSelected(position: Int) {
                val calendar = Calendar()
                calendar.year = (position + mDelegate!!.minYearMonth - 1) / 12 + mDelegate!!.minYear
                calendar.month = (position + mDelegate!!.minYearMonth - 1) % 12 + 1
                calendar.day = 1
                calendar.isCurrentMonth = calendar.year == mDelegate!!.currentDay!!.year && calendar.month == mDelegate!!.currentDay!!.month
                calendar.isCurrentDay = calendar == mDelegate!!.currentDay
                LunarCalendar.setupLunarCalendar(calendar)

                if (mDelegate!!.mMonthChangeListener != null) {
                    mDelegate!!.mMonthChangeListener!!.onMonthChange(calendar.year, calendar.month)
                }

                if (mDelegate!!.monthViewShowMode != CustomCalendarViewDelegate.MODE_ALL_MONTH && visibility != View.VISIBLE) {
                    updateMonthViewHeight(calendar.year, calendar.month)
                }

                if (mWeekPager!!.visibility == View.VISIBLE) {
                    return
                }

                if (!calendar.isCurrentMonth) {
                    mDelegate!!.mSelectedCalendar = calendar
                } else {
                    mDelegate!!.mSelectedCalendar = mDelegate!!.createCurrentDate()
                }

                if (mDelegate!!.mDateSelectedListener != null && !isUsingScrollToCalendar) {
                    mWeekBar!!.onDateSelected(mDelegate!!.mSelectedCalendar!!, false)
                    mDelegate!!.mDateSelectedListener!!.onDateSelected(mDelegate!!.mSelectedCalendar!!, false)
                }

                val view = findViewWithTag<View>(position) as? MonthView
                if (view != null) {
                    val index = view.getSelectedIndex(mDelegate!!.mSelectedCalendar!!)
                    view.mCurrentItem = index
                    if (index >= 0 && mParentLayout != null) {
                        mParentLayout!!.setSelectPosition(index)
                    }
                    view.invalidate()
                }
                mWeekPager!!.updateSelected(mDelegate!!.mSelectedCalendar!!, false)
                updateMonthViewHeight(calendar.year, calendar.month)
                isUsingScrollToCalendar = false
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    /**
     * 更新月视图的高度
     * @param year year
     * @param month month
     */
    private fun updateMonthViewHeight(year: Int, month: Int) {

        if (mDelegate!!.monthViewShowMode == CustomCalendarViewDelegate.MODE_ALL_MONTH) {
            mCurrentViewHeight = 6 * mDelegate!!.calendarItemHeight
            return
        }

        if (mParentLayout != null) {
            if (visibility != View.VISIBLE) {//如果已经显示周视图，则需要动态改变月视图高度
                val params = layoutParams
                params.height = Util.getMonthViewHeight(year, month, mDelegate!!.calendarItemHeight)
                layoutParams = params
            }
            mParentLayout!!.updateContentViewTranslateY()
        }
        mCurrentViewHeight = Util.getMonthViewHeight(year, month, mDelegate!!.calendarItemHeight)
        if (month == 1) {
            mPreViewHeight = Util.getMonthViewHeight(year - 1, 12, mDelegate!!.calendarItemHeight)
            mNextViewHeight = Util.getMonthViewHeight(year, 2, mDelegate!!.calendarItemHeight)
        } else {
            mPreViewHeight = Util.getMonthViewHeight(year, month - 1, mDelegate!!.calendarItemHeight)
            if (month == 12) {
                mNextViewHeight = Util.getMonthViewHeight(year + 1, 1, mDelegate!!.calendarItemHeight)
            } else {
                mNextViewHeight = Util.getMonthViewHeight(year, month + 1, mDelegate!!.calendarItemHeight)
            }
        }
    }

    internal fun notifyDataSetChanged() {
        mMonthCount = 12 * (mDelegate!!.maxYear - mDelegate!!.minYear) - mDelegate!!.minYearMonth + 1 +
                mDelegate!!.maxYearMonth
        adapter!!.notifyDataSetChanged()
    }

    /**
     * 滚动到指定日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    internal fun scrollToCalendar(year: Int, month: Int, day: Int, smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.isCurrentDay = calendar == mDelegate!!.currentDay
        LunarCalendar.setupLunarCalendar(calendar)
        mDelegate!!.mSelectedCalendar = calendar

        val y = calendar.year - mDelegate!!.minYear
        val position = 12 * y + calendar.month - mDelegate!!.minYearMonth
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)

        val view = findViewWithTag<View>(position) as MonthView
        if (view != null) {
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar!!)
            view.invalidate()
            if (mParentLayout != null) {
                mParentLayout!!.setSelectPosition(view.getSelectedIndex(mDelegate!!.mSelectedCalendar!!))
            }
        }
        if (mParentLayout != null) {
            val i = Util.getWeekFromDayInMonth(calendar)
            mParentLayout!!.setSelectWeek(i)
        }


        if (mDelegate!!.mInnerListener != null) {
            mDelegate!!.mInnerListener!!.onMonthDateSelected(calendar, false)
        }

        if (mDelegate!!.mDateSelectedListener != null) {
            mDelegate!!.mDateSelectedListener!!.onDateSelected(calendar, false)
        }
        updateSelected()
    }

    /**
     * 滚动到当前日期
     */
    internal fun scrollToCurrent(smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val position = 12 * (mDelegate!!.currentDay!!.year - mDelegate!!.minYear) + mDelegate!!.currentDay!!.month - mDelegate!!.minYearMonth
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)
        val view = findViewWithTag<View>(position) as MonthView
        if (view != null) {
            view.setSelectedCalendar(mDelegate!!.currentDay!!)
            view.invalidate()
            if (mParentLayout != null) {
                mParentLayout!!.setSelectPosition(view.getSelectedIndex(mDelegate!!.currentDay!!))
            }
        }
        if (mDelegate!!.mDateSelectedListener != null && visibility == View.VISIBLE) {
            mDelegate!!.mDateSelectedListener!!.onDateSelected(mDelegate!!.createCurrentDate(), false)
        }
    }


    /**
     * 更新选择效果
     */
    internal fun updateSelected() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as MonthView
            mDelegate?.mSelectedCalendar?.let {
                view.setSelectedCalendar(it)
            }
            view.invalidate()
        }
    }

    /**
     * 更新标记日期
     */
    internal fun updateScheme() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as MonthView
            view.update()
        }
    }

    /**
     * 日历卡月份Adapter
     */
    private inner class MonthViewPagerAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return mMonthCount
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val year = (position + mDelegate!!.minYearMonth - 1) / 12 + mDelegate!!.minYear
            val month = (position + mDelegate!!.minYearMonth - 1) % 12 + 1
            var view: MonthView
            if (TextUtils.isEmpty(mDelegate!!.monthViewClass)) {
                view = DefaultMonthView(context)
            } else {
                view = try {
                    val cls = Class.forName(mDelegate!!.monthViewClass)
                    val constructor = cls.getConstructor(Context::class.java)
                    constructor.newInstance(context) as MonthView
                } catch (e: Exception) {
                    e.printStackTrace()
                    DefaultMonthView(context)
                }

            }
            view.mParentLayout = mParentLayout
            view.mMonthViewPager = this@MonthViewPager
            view.setup(mDelegate!!)
            view.tag = position
            view.setCurrentDate(year, month)
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar!!)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

}
