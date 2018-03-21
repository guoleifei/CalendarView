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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.view.View
import java.util.*

/**
 * 月视图基础控件,请使用 MonthView替换，没有任何不同，只是规范命名
 * pleased using MonthView replace BaseCalendarCardView
 * Created by huanghaibin on 2017/11/15.
 */
abstract class MonthView(context: Context) : BaseView(context) {

    /**
     * 月视图ViewPager
     */
    internal var mMonthViewPager: MonthViewPager? = null

    /**
     * 当前日历卡年份
     */
    private var mYear: Int = 0

    /**
     * 当前日历卡月份
     */
    private var mMonth: Int = 0


    /**
     * 日历的行数
     */
    private var mLineCount: Int = 0

    /**
     * 日历高度
     */
    private var mHeight: Int = 0


    /**
     * 下个月偏移的数量
     */
    private var mNextDiff: Int = 0
    // 选择项
    private val index: Calendar?
        get() {
            return mItems?.let {
                val width = width / 7
                var indexX = mX.toInt() / width
                if (indexX >= 7) {
                    indexX = 6
                }
                val indexY = mY.toInt() / mItemHeight
                mCurrentItem = indexY * 7 + indexX
                if (mCurrentItem >= 0 && mCurrentItem < it.size) it[mCurrentItem] else null
            }
//            val width = width / 7
//            var indexX = mX.toInt() / width
//            if (indexX >= 7) {
//                indexX = 6
//            }
//            val indexY = mY.toInt() / mItemHeight
//            mCurrentItem = indexY * 7 + indexX
//
//            return if (mCurrentItem >= 0 && mCurrentItem < mItems!!.size) mItems!![mCurrentItem] else null
        }

    override fun onDraw(canvas: Canvas) {
        if (mLineCount == 0)
            return
        mItemWidth = width / 7
        onPreviewHook()
        val count = mLineCount * 7
        var d = 0
        for (i in 0 until mLineCount) {
            mItems?.let {
                for (j in 0..6) {
                    val calendar = it[d]
                    if (mDelegate.monthViewShowMode == CustomCalendarViewDelegate.MODE_ONLY_CURRENT_MONTH) {
                        if (d > it.size - mNextDiff) {
                            return
                        }
                        if (!calendar.isCurrentMonth) {
                            ++d
                            continue
                        }
                    } else if (mDelegate.monthViewShowMode == CustomCalendarViewDelegate.MODE_FIT_MONTH) {
                        if (d >= count) {
                            return
                        }
                    }
                    draw(canvas, calendar, i, j, d)
                    ++d
                }
            }
        }
    }


    /**
     * 开始绘制
     *
     * @param canvas   canvas
     * @param calendar 对应日历
     * @param i        i
     * @param j        j
     * @param d        d
     */
    private fun draw(canvas: Canvas, calendar: Calendar, i: Int, j: Int, d: Int) {
        val x = j * mItemWidth
        val y = i * mItemHeight
        onLoopStart(x, y)
        val isSelected = d == mCurrentItem
        val hasScheme = calendar.hasScheme()

        if (hasScheme) {
            //标记的日子
            var isDrawSelected = false//是否继续绘制选中的onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, calendar, x, y, true)
            }
            if (isDrawSelected || !isSelected) {
                //将画笔设置为标记颜色
                mSchemePaint.color = if (calendar.schemeColor != 0) calendar.schemeColor else mDelegate.schemeThemeColor
                onDrawScheme(canvas, calendar, x, y)
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false)
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected)
    }


    override fun onClick(v: View) {
        if (isClick) {
            val calendar = index
            if (calendar != null) {
                if (mDelegate.monthViewShowMode == CustomCalendarViewDelegate.MODE_ONLY_CURRENT_MONTH && !calendar.isCurrentMonth) {
                    mCurrentItem = mItems?.indexOf(mDelegate.mSelectedCalendar) ?: -1
                    return
                }

                if (!Util.isCalendarInRange(calendar, mDelegate.minYear,
                                mDelegate.minYearMonth, mDelegate.maxYear, mDelegate.maxYearMonth)) {
                    mCurrentItem = mItems?.indexOf(mDelegate.mSelectedCalendar) ?: -1
                    return
                }

                if (!(calendar.isCurrentMonth || mMonthViewPager == null)) {
                    mMonthViewPager?.let {
                        val cur = it.currentItem
                        val position = if (mCurrentItem < 7) cur - 1 else cur + 1
                        it.currentItem = position
                    }
                }
                mDelegate.mInnerListener?.onMonthDateSelected(calendar, true)

                if (mParentLayout != null) {
                    mItems?.let {
                        if (calendar.isCurrentMonth) {
                            mParentLayout?.setSelectPosition(it.indexOf(calendar))
                        } else {
                            mParentLayout?.setSelectWeek(Util.getWeekFromDayInMonth(calendar))
                        }
                    }
                }
                mDelegate.mDateSelectedListener?.onDateSelected(calendar, true)
                invalidate()
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        if (mDelegate.mDateLongClickListener == null)
            return false
        if (isClick) {
            val calendar = index
            if (calendar != null) {
                if (mDelegate.monthViewShowMode == CustomCalendarViewDelegate.MODE_ONLY_CURRENT_MONTH && !calendar.isCurrentMonth) {
                    mCurrentItem = mItems?.indexOf(mDelegate.mSelectedCalendar) ?: -1
                    return false
                }

                if (!Util.isCalendarInRange(calendar, mDelegate.minYear,
                                mDelegate.minYearMonth, mDelegate.maxYear, mDelegate.maxYearMonth)) {
                    mCurrentItem = mItems?.indexOf(mDelegate.mSelectedCalendar) ?: -1
                    return false
                }

                if (!calendar.isCurrentMonth) {
                    mMonthViewPager?.let {
                        val cur = it.currentItem
                        val position = if (mCurrentItem < 7) cur - 1 else cur + 1
                        it.currentItem = position
                    }

                }
                mDelegate.mInnerListener?.onMonthDateSelected(calendar, true)

                if (mParentLayout != null) {
                    mItems?.let {
                        if (calendar.isCurrentMonth) {
                            mParentLayout?.setSelectPosition(it.indexOf(calendar))
                        } else {
                            mParentLayout?.setSelectWeek(Util.getWeekFromDayInMonth(calendar))
                        }
                    }
                }
                mDelegate.mDateSelectedListener?.onDateSelected(calendar, true)
                mDelegate.mDateLongClickListener?.onDateLongClick(calendar)
                invalidate()
            }
        }
        return false
    }


    /**
     * 记录已经选择的日期
     *
     * @param calendar calendar
     */
    internal fun setSelectedCalendar(calendar: Calendar) {
        mCurrentItem = mItems?.indexOf(calendar) ?: -1
    }

    /**
     * 初始化日期
     *
     * @param year  year
     * @param month month
     */
    internal fun setCurrentDate(year: Int, month: Int) {
        mYear = year
        mMonth = month
        initCalendar()
        mHeight = if (mDelegate.monthViewShowMode == CustomCalendarViewDelegate.MODE_ALL_MONTH) {
            mItemHeight * mLineCount
        } else {
            Util.getMonthViewHeight(year, month, mItemHeight)
        }

    }

    /**
     * 初始化日历
     */
    @SuppressLint("WrongConstant")
    private fun initCalendar() {
        val date = java.util.Calendar.getInstance()

        date.set(mYear, mMonth - 1, 1)
        val mPreDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1
        val mDayCount = Util.getMonthDaysCount(mYear, mMonth)
        date.set(mYear, mMonth - 1, mDayCount)

        val mLastCount = date.get(java.util.Calendar.DAY_OF_WEEK) - 1
        mNextDiff = 6 - mLastCount//下个月的日偏移天数

        val preYear: Int
        val preMonth: Int
        val nextYear: Int
        val nextMonth: Int

        val size = 42

        val preMonthDaysCount: Int
        when (mMonth) {
            1 -> {//如果是1月
                preYear = mYear - 1
                preMonth = 12
                nextYear = mYear
                nextMonth = mMonth + 1
                preMonthDaysCount = if (mPreDiff == 0) 0 else Util.getMonthDaysCount(preYear, preMonth)
            }
            12 -> {//如果是12月
                preYear = mYear
                preMonth = mMonth - 1
                nextYear = mYear + 1
                nextMonth = 1
                preMonthDaysCount = if (mPreDiff == 0) 0 else Util.getMonthDaysCount(preYear, preMonth)
            }
            else -> {//平常
                preYear = mYear
                preMonth = mMonth - 1
                nextYear = mYear
                nextMonth = mMonth + 1
                preMonthDaysCount = if (mPreDiff == 0) 0 else Util.getMonthDaysCount(preYear, preMonth)
            }
        }
        var nextDay = 1
        if (mItems == null)
            mItems = ArrayList()
        mItems!!.clear()
        for (i in 0 until size) {
            val calendarDate = Calendar()
            when {
                i < mPreDiff -> {
                    calendarDate.year = preYear
                    calendarDate.month = preMonth
                    calendarDate.day = preMonthDaysCount - mPreDiff + i + 1
                }
                i >= mDayCount + mPreDiff -> {
                    calendarDate.year = nextYear
                    calendarDate.month = nextMonth
                    calendarDate.day = nextDay
                    ++nextDay
                }
                else -> {
                    calendarDate.year = mYear
                    calendarDate.month = mMonth
                    calendarDate.isCurrentMonth = true
                    calendarDate.day = i - mPreDiff + 1
                }
            }
            if (calendarDate == mDelegate.currentDay) {
                calendarDate.isCurrentDay = true
                mCurrentItem = i
            }
            LunarCalendar.setupLunarCalendar(calendarDate)
            mItems?.add(calendarDate)
        }
        mLineCount = if (mDelegate.monthViewShowMode == CustomCalendarViewDelegate.MODE_ALL_MONTH) {
            6
        } else {
            (mPreDiff + mDayCount + mNextDiff) / 7
        }
        if (mDelegate.mSchemeDate != null) {
            for (a in mItems!!) {
                for (d in mDelegate.mSchemeDate!!) {
                    if (d == a) {
                        a.scheme = if (TextUtils.isEmpty(d.scheme)) mDelegate.schemeText else d.scheme
                        a.schemeColor = d.schemeColor
                        a.schemes = d.schemes
                    }
                }
            }
        }
        invalidate()
    }


    override fun update() {
        if (mDelegate.mSchemeDate != null) {
            for (a in mItems!!) {
                for (d in mDelegate.mSchemeDate!!) {
                    if (d == a) {
                        a.scheme = if (TextUtils.isEmpty(d.scheme)) mDelegate.schemeText else d.scheme
                        a.schemeColor = d.schemeColor
                        a.schemes = d.schemes
                    }
                }
            }
            invalidate()
        }
    }

    /**
     * 获取选中的项索引
     */
    internal fun getSelectedIndex(calendar: Calendar): Int {
        return mItems?.indexOf(calendar) ?: -1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpecTemp = heightMeasureSpec
        if (mLineCount != 0) {
            heightMeasureSpecTemp = View.MeasureSpec.makeMeasureSpec(mHeight, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpecTemp)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    override fun onPreviewHook() {
        // TODO: 2017/11/16
    }


    /**
     * 循环绘制开始的回调，不需要可忽略
     * 绘制每个日历项的循环，用来计算baseLine、圆心坐标等都可以在这里实现
     *
     * @param x 日历Card x起点坐标
     * @param y 日历Card y起点坐标
     */
    open fun onLoopStart(x: Int, y: Int) {
        // TODO: 2017/11/16
    }

    /**
     * 绘制选中的日期
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return 是否绘制onDrawScheme，true or false
     */
    protected abstract fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean

    /**
     * 绘制标记的日期,这里可以是背景色，标记色什么的
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    protected abstract fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int)


    /**
     * 绘制日历文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean)
}
