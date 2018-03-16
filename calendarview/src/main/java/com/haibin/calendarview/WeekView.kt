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
import android.graphics.Canvas
import android.text.TextUtils
import android.view.View
import java.util.*

/**
 * 周视图，因为日历UI采用热插拔实现，所以这里必须继承实现，达到UI一致即可
 * Created by huanghaibin on 2017/11/21.
 */

abstract class WeekView(context: Context) : BaseView(context) {

    /**
     * 获取点击的日历
     *
     * @return 获取点击的日历
     */
    private// 选择项
    val index: Calendar?
        get() {
            val width = width / 7
            var indexX = mX.toInt() / width
            if (indexX >= 7) {
                indexX = 6
            }
            val indexY = mY.toInt() / mItemHeight
            mCurrentItem = indexY * 7 + indexX
            return if (mCurrentItem >= 0 && mCurrentItem < mItems!!.size) mItems!![mCurrentItem] else null
        }

    /**
     * 绘制日历文本
     *
     * @param canvas canvas
     */
    override fun onDraw(canvas: Canvas) {
        if (mItems!!.size == 0)
            return
        mItemWidth = width / 7
        onPreviewHook()

        for (i in 0..6) {
            val x = i * mItemWidth
            onLoopStart(x)
            val calendar = mItems!![i]
            val isSelected = i == mCurrentItem
            val hasScheme = calendar.hasScheme()
            if (hasScheme) {
                var isDrawSelected = false//是否继续绘制选中的onDrawScheme
                if (isSelected) {
                    isDrawSelected = onDrawSelected(canvas, calendar, x, true)
                }
                if (isDrawSelected || !isSelected) {
                    //将画笔设置为标记颜色
                    mSchemePaint.color = if (calendar.schemeColor != 0) calendar.schemeColor else mDelegate.schemeThemeColor
                    onDrawScheme(canvas, calendar, x)
                }
            } else {
                if (isSelected) {
                    onDrawSelected(canvas, calendar, x, false)
                }
            }
            onDrawText(canvas, calendar, x, hasScheme, isSelected)
        }
    }

    override fun onClick(v: View) {
        if (isClick) {
            val calendar = index
            if (calendar != null) {
                if (!Util.isCalendarInRange(calendar, mDelegate.minYear,
                                mDelegate.minYearMonth, mDelegate.maxYear, mDelegate.maxYearMonth)) {
                    mCurrentItem = mItems!!.indexOf(mDelegate.mSelectedCalendar)
                    return
                }
                if (mDelegate.mInnerListener != null) {
                    mDelegate.mInnerListener!!.onWeekDateSelected(calendar, true)
                }
                if (mParentLayout != null) {
                    val i = Util.getWeekFromDayInMonth(calendar)
                    mParentLayout!!.setSelectWeek(i)
                }

                if (mDelegate.mDateSelectedListener != null) {
                    mDelegate.mDateSelectedListener!!.onDateSelected(calendar, true)
                }

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
                if (!Util.isCalendarInRange(calendar, mDelegate.minYear,
                                mDelegate.minYearMonth, mDelegate.maxYear, mDelegate.maxYearMonth)) {
                    mCurrentItem = mItems!!.indexOf(mDelegate.mSelectedCalendar)
                    return false
                }
                if (mDelegate.mInnerListener != null) {
                    mDelegate.mInnerListener!!.onWeekDateSelected(calendar, true)
                }
                if (mParentLayout != null) {
                    val i = Util.getWeekFromDayInMonth(calendar)
                    mParentLayout!!.setSelectWeek(i)
                }

                if (mDelegate.mDateSelectedListener != null) {
                    mDelegate.mDateSelectedListener!!.onDateSelected(calendar, true)
                }

                mDelegate.mDateLongClickListener!!.onDateLongClick(calendar)

                invalidate()
            }
        }
        return false
    }

    /**
     * 周视图切换点击默认位置
     *
     * @param calendar calendar
     */
    internal fun performClickCalendar(calendar: Calendar, isNotice: Boolean) {
        if (mParentLayout == null || mDelegate.mInnerListener == null || mItems == null || mItems!!.size == 0) {
            return
        }

        var week = Util.getWeekFormCalendar(calendar)
        if (mItems!!.contains(mDelegate.currentDay)) {
            week = Util.getWeekFormCalendar(mDelegate.currentDay!!)
        }

        mCurrentItem = week

        var currentCalendar = mItems!![week]

        if (!Util.isCalendarInRange(currentCalendar, mDelegate.minYear,
                        mDelegate.minYearMonth, mDelegate.maxYear, mDelegate.maxYearMonth)) {
            mCurrentItem = getEdgeIndex(isLeftEdge(currentCalendar))
            currentCalendar = mItems!![mCurrentItem]
        }

        currentCalendar.isCurrentDay = currentCalendar == mDelegate.currentDay
        mDelegate.mInnerListener!!.onWeekDateSelected(currentCalendar, false)

        val i = Util.getWeekFromDayInMonth(currentCalendar)
        mParentLayout!!.setSelectWeek(i)

        if (mDelegate.mDateSelectedListener != null && isNotice) {
            mDelegate.mDateSelectedListener!!.onDateSelected(currentCalendar, false)
        }
        mParentLayout!!.updateContentViewTranslateY()
        invalidate()
    }

    private fun isLeftEdge(calendar: Calendar): Boolean {
        val c = java.util.Calendar.getInstance()
        c.set(mDelegate.minYear, mDelegate.minYearMonth - 1, 1)
        val minTime = c.timeInMillis
        c.set(calendar.year, calendar.month - 1, calendar.day)
        val curTime = c.timeInMillis
        return curTime < minTime
    }

    private fun getEdgeIndex(isMinEdge: Boolean): Int {
        for (i in mItems!!.indices) {
            val item = mItems!![i]
            if (isMinEdge && Util.isCalendarInRange(item, mDelegate.minYear, mDelegate.minYearMonth,
                            mDelegate.maxYear, mDelegate.maxYearMonth)) {
                return i
            } else if (!isMinEdge && !Util.isCalendarInRange(item, mDelegate.minYear, mDelegate.minYearMonth,
                            mDelegate.maxYear, mDelegate.maxYearMonth)) {
                return i - 1
            }
        }
        return if (isMinEdge) 6 else 0
    }


    /**
     * 记录已经选择的日期
     *
     * @param calendar calendar
     */
    internal fun setSelectedCalendar(calendar: Calendar) {
        mCurrentItem = mItems!!.indexOf(calendar)
    }


    /**
     * 初始化周视图控件
     *
     * @param calendar calendar
     */
    internal fun setup(calendar: Calendar) {
        val date = java.util.Calendar.getInstance()
        date.set(calendar.year, calendar.month - 1, calendar.day)
        val week = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//星期几,星期天 == 0，也就是前面偏差多少天
        val dayCount = Util.getMonthDaysCount(calendar.year, calendar.month)//获取某个月有多少天

        if (mItems == null) {
            mItems = ArrayList()
        }

        var preDiff = 0
        var nextDiff = 0
        var preMonthDaysCount = 0
        var preYear = 0
        var preMonth = 0
        var nextYear = 0
        var nextMonth = 0

        if (calendar.day - week <= 0) {//如果某月某天-星期<0，则说明前面需要上个月的补数
            date.set(calendar.year, calendar.month - 1, 1)
            preDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//月第一天为星期几,星期天 == 0，补数量就是偏差量diff;
            if (calendar.month == 1) {//取上一年的12月份
                preMonthDaysCount = 31
                preYear = calendar.year - 1
                preMonth = 12
            } else {//否则取上一个月份天数
                preMonthDaysCount = Util.getMonthDaysCount(calendar.year, calendar.month - 1)
                preYear = calendar.year
                preMonth = calendar.month - 1
            }
        } else if (calendar.day + 6 - week > dayCount) {//往后偏移多少天，即当前月份的最后一天不是星期6，则需要往后取补数
            nextDiff = calendar.day + 6 - week - dayCount//往后偏移多少天，补差diff
            if (calendar.month == 12) {
                nextMonth = 1
                nextYear = calendar.year + 1
            } else {
                nextMonth = calendar.month + 1
                nextYear = calendar.year
            }
        }
        var nextDay = 1
        var day = calendar.day - week
        for (i in 0..6) {
            val calendarDate = Calendar()
            if (i < preDiff) {//如果前面有补数
                calendarDate.year = preYear
                calendarDate.month = preMonth
                calendarDate.day = preMonthDaysCount - preDiff + i + 1
                day += 1
            } else if (nextDiff > 0 && i >= 7 - nextDiff) {
                calendarDate.year = nextYear
                calendarDate.month = nextMonth
                calendarDate.day = nextDay
                nextDay += 1
            } else {
                calendarDate.year = calendar.year
                calendarDate.month = calendar.month
                calendarDate.day = day
                day += 1
            }
            calendarDate.isCurrentDay = calendarDate == mDelegate.currentDay
            LunarCalendar.setupLunarCalendar(calendarDate)
            calendarDate.isCurrentMonth = true
            mItems?.add(calendarDate)
        }
        if (mDelegate.mSchemeDate != null) {
            mItems?.let {
                for (a in it) {
                    mDelegate.mSchemeDate?.forEach {
                        if (it == a) {
                            a.scheme = if (TextUtils.isEmpty(it.scheme)) mDelegate.schemeText else it.scheme
                            a.schemeColor = it.schemeColor
                            a.schemes = it.schemes
                        }
                    }
                }
            }

        }
        invalidate()
    }


    /**
     * 更新界面
     */
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpecTemp = View.MeasureSpec.makeMeasureSpec(mItemHeight, View.MeasureSpec.EXACTLY)
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
     */
    protected fun onLoopStart(x: Int) {
        // TODO: 2017/11/16
    }

    /**
     * 绘制选中的日期
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     */
    protected abstract fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean): Boolean

    /**
     * 绘制标记的日期
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     */
    protected abstract fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int)


    /**
     * 绘制日历文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean)
}
