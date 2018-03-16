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
 * 周视图滑动ViewPager，需要动态固定高度
 * 周视图是连续不断的视图，因此不能简单的得出每年都有52+1周，这样会计算重叠的部分
 * WeekViewPager需要和CalendarView关联:
 */

class WeekViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private var mWeekCount: Int = 0
    private var mDelegate: CustomCalendarViewDelegate? = null

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    internal var mParentLayout: CalendarLayout? = null

    /**
     * 是否使用滚动到某一天
     */
    private var isUsingScrollToCalendar = false

    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate
        init()
    }

    private fun init() {
        mWeekCount = Util.getWeekCountBetweenYearAndYear(mDelegate!!.minYear, mDelegate!!.minYearMonth,
                mDelegate!!.maxYear, mDelegate!!.maxYearMonth)
        adapter = WeekViewPagerAdapter()
        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                //默认的显示星期四，周视图切换就显示星期4
                if (visibility != View.VISIBLE) {
                    isUsingScrollToCalendar = false
                    return
                }
                val view = findViewWithTag<View>(position) as WeekView
                view?.performClickCalendar(mDelegate!!.mSelectedCalendar!!, !isUsingScrollToCalendar)
                isUsingScrollToCalendar = false
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    internal fun notifyDataSetChanged() {
        mWeekCount = Util.getWeekCountBetweenYearAndYear(mDelegate!!.minYear, mDelegate!!.minYearMonth,
                mDelegate!!.maxYear, mDelegate!!.maxYearMonth)
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
        updateSelected(calendar, smoothScroll)
        if (mDelegate!!.mInnerListener != null) {
            mDelegate!!.mInnerListener!!.onWeekDateSelected(calendar, false)
        }
        if (mDelegate!!.mDateSelectedListener != null) {
            mDelegate!!.mDateSelectedListener!!.onDateSelected(calendar, false)
        }
    }

    /**
     * 滚动到当前
     */
    internal fun scrollToCurrent(smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val position = Util.getWeekFromCalendarBetweenYearAndYear(mDelegate!!.currentDay!!,
                mDelegate!!.minYear,
                mDelegate!!.minYearMonth) - 1
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)
        val view = findViewWithTag<View>(position) as WeekView
        if (view != null) {
            view.performClickCalendar(mDelegate!!.currentDay!!, false)
            view.setSelectedCalendar(mDelegate!!.currentDay!!)
            view.invalidate()
        }
        if (mDelegate!!.mDateSelectedListener != null && visibility == View.VISIBLE) {
            mDelegate!!.mDateSelectedListener!!.onDateSelected(mDelegate!!.createCurrentDate(), false)
        }
    }

    /**
     * 更新任意一个选择的日期
     */
    internal fun updateSelected(calendar: Calendar, smoothScroll: Boolean) {
        val position = Util.getWeekFromCalendarBetweenYearAndYear(calendar, mDelegate!!.minYear, mDelegate!!.minYearMonth) - 1
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)
        val view = findViewWithTag<View>(position) as WeekView
        if (view != null) {
            view.setSelectedCalendar(calendar)
            view.invalidate()
        }
    }


    /**
     * 更新标记日期
     */
    internal fun updateScheme() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as WeekView
            view.update()
        }
    }

    /**
     * 周视图的高度应该与日历项的高度一致
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mDelegate!!.calendarItemHeight, View.MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 周视图切换
     */
    private inner class WeekViewPagerAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return mWeekCount
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val calendar = Util.getFirstCalendarFromWeekCount(mDelegate!!.minYear, mDelegate!!.minYearMonth, position + 1)
            val view: WeekView
            view = if (TextUtils.isEmpty(mDelegate!!.weekViewClass)) {
                DefaultWeekView(context)
            } else {
                try {
                    val cls = Class.forName(mDelegate!!.weekViewClass)
                    val constructor = cls.getConstructor(Context::class.java)
                    constructor.newInstance(context) as WeekView
                } catch (e: Exception) {
                    e.printStackTrace()
                    DefaultWeekView(context)
                }

            }
            view.mParentLayout = mParentLayout
            view.setup(mDelegate!!)
            view.setup(calendar)
            view.tag = position
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar!!)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }
}
