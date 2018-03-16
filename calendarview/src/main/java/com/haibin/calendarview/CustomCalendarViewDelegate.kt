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
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import java.util.*

/**
 * Google规范化的属性委托,
 * 这里基本是没有逻辑的，代码量多，但是不影响阅读性
 */
class CustomCalendarViewDelegate(context: Context, attrs: AttributeSet?) {


    /**
     * 月份显示模式
     */
    val monthViewShowMode: Int

    /**
     * 各种字体颜色，看名字知道对应的地方
     */
    var curDayTextColor: Int = 0
        private set
    val curDayLunarTextColor: Int
    val weekTextColor: Int
    var schemeTextColor: Int = 0
        private set
    var schemeLunarTextColor: Int = 0
        private set
    var otherMonthTextColor: Int = 0
        private set
    var currentMonthTextColor: Int = 0
        private set
    var selectedTextColor: Int = 0
        private set
    var selectedLunarTextColor: Int = 0
        private set
    var currentMonthLunarTextColor: Int = 0
        private set
    var otherMonthLunarTextColor: Int = 0
        private set

    /**
     * 年视图字体大小
     */
    val yearViewMonthTextSize: Int
    val yearViewDayTextSize: Int

    /**
     * 年视图字体和标记颜色
     */
    var yearViewMonthTextColor: Int = 0
        private set
    var yearViewDayTextColor: Int = 0
        private set
    var yearViewSchemeTextColor: Int = 0
        private set

    /**
     * 星期栏的背景、线的背景、年份背景
     */
    val weekLineBackground: Int
    val yearViewBackground: Int
    val weekBackground: Int


    /**
     * 标记的主题色和选中的主题色
     */
    var schemeThemeColor: Int = 0
        private set
    var selectedThemeColor: Int = 0
        private set


    /**
     * 自定义的日历路径
     */
    val monthViewClass: String?

    /**
     * 自定义周视图路径
     */
    val weekViewClass: String?

    /**
     * 自定义周栏路径
     */
    val weekBarClass: String?


    /**
     * 年月视图是否打开
     */
    var isShowYearSelectedLayout: Boolean = false

    /**
     * 标记文本
     */
    var schemeText: String? = null
        private set

    /**
     * 最小年份和最大年份
     */
    var minYear: Int = 0
        private set
    var maxYear: Int = 0
        private set

    /**
     * 最小年份和最大年份对应最小月份和最大月份
     * when you want set 2015-07 to 2017-08
     */
    var minYearMonth: Int = 0
        private set
    var maxYearMonth: Int = 0
        private set

    /**
     * 日期和农历文本大小
     */
    val dayTextSize: Int
    val lunarTextSize: Int

    /**
     * 日历卡的项高度
     */
    val calendarItemHeight: Int

    /**
     * 星期栏的高度
     */
    val weekBarHeight: Int

    /**
     * 今天的日子
     */
    var currentDay: Calendar? = null
        private set

    /**
     * 当前月份和周视图的item位置
     */
    var mCurrentMonthViewItem: Int = 0
    var mCurrentWeekViewItem: Int = 0

    /**
     * 标记的日期
     */
    var mSchemeDate: MutableList<Calendar>? = null


    /**
     * 日期被选中监听
     */
    var mDateSelectedListener: CalendarView.OnDateSelectedListener? = null

    /**
     * 外部日期长按事件
     */
    var mDateLongClickListener: CalendarView.OnDateLongClickListener? = null

    /**
     * 内部日期切换监听，用于内部更新计算
     */
    var mInnerListener: CalendarView.OnInnerDateSelectedListener? = null

    /**
     * 快速年份切换
     */
    var mYearChangeListener: CalendarView.OnYearChangeListener? = null


    /**
     * 月份切换事件
     */
    var mMonthChangeListener: CalendarView.OnMonthChangeListener? = null

    /**
     * 保存选中的日期
     */
    var mSelectedCalendar: Calendar? = null

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)

        LunarCalendar.init(context)

        schemeTextColor = array.getColor(R.styleable.CalendarView_scheme_text_color, -0x1)
        schemeLunarTextColor = array.getColor(R.styleable.CalendarView_scheme_lunar_text_color, -0x1e1e1f)
        schemeThemeColor = array.getColor(R.styleable.CalendarView_scheme_theme_color, 0x50CFCFCF)
        monthViewClass = array.getString(R.styleable.CalendarView_month_view)

        weekViewClass = array.getString(R.styleable.CalendarView_week_view)
        weekBarClass = array.getString(R.styleable.CalendarView_week_bar_view)
        weekBarHeight = array.getDimension(R.styleable.CalendarView_week_bar_height, Util.dipToPx(context, 40f).toFloat()).toInt()

        schemeText = array.getString(R.styleable.CalendarView_scheme_text)
        if (TextUtils.isEmpty(schemeText)) {
            schemeText = "记"
        }
        monthViewShowMode = array.getInt(R.styleable.CalendarView_month_view_show_mode, MODE_ALL_MONTH)

        weekBackground = array.getColor(R.styleable.CalendarView_week_background, Color.WHITE)
        weekLineBackground = array.getColor(R.styleable.CalendarView_week_line_background, Color.TRANSPARENT)
        yearViewBackground = array.getColor(R.styleable.CalendarView_year_view_background, Color.WHITE)
        weekTextColor = array.getColor(R.styleable.CalendarView_week_text_color, -0xcccccd)

        curDayTextColor = array.getColor(R.styleable.CalendarView_current_day_text_color, Color.RED)
        curDayLunarTextColor = array.getColor(R.styleable.CalendarView_current_day_lunar_text_color, Color.RED)

        selectedThemeColor = array.getColor(R.styleable.CalendarView_selected_theme_color, 0x50CFCFCF)
        selectedTextColor = array.getColor(R.styleable.CalendarView_selected_text_color, -0xeeeeef)

        selectedLunarTextColor = array.getColor(R.styleable.CalendarView_selected_lunar_text_color, -0xeeeeef)
        currentMonthTextColor = array.getColor(R.styleable.CalendarView_current_month_text_color, -0xeeeeef)
        otherMonthTextColor = array.getColor(R.styleable.CalendarView_other_month_text_color, -0x1e1e1f)

        currentMonthLunarTextColor = array.getColor(R.styleable.CalendarView_current_month_lunar_text_color, -0x1e1e1f)
        otherMonthLunarTextColor = array.getColor(R.styleable.CalendarView_other_month_lunar_text_color, -0x1e1e1f)
        minYear = array.getInt(R.styleable.CalendarView_min_year, 1971)
        maxYear = array.getInt(R.styleable.CalendarView_max_year, 2055)
        minYearMonth = array.getInt(R.styleable.CalendarView_min_year_month, 1)
        maxYearMonth = array.getInt(R.styleable.CalendarView_max_year_month, 12)

        dayTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_day_text_size, Util.dipToPx(context, 16f))
        lunarTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_lunar_text_size, Util.dipToPx(context, 10f))
        calendarItemHeight = array.getDimension(R.styleable.CalendarView_calendar_height, Util.dipToPx(context, 56f).toFloat()).toInt()

        //年视图相关
        yearViewMonthTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_month_text_size, Util.dipToPx(context, 18f))
        yearViewDayTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_day_text_size, Util.dipToPx(context, 8f))
        yearViewMonthTextColor = array.getColor(R.styleable.CalendarView_year_view_month_text_color, -0xeeeeef)
        yearViewDayTextColor = array.getColor(R.styleable.CalendarView_year_view_day_text_color, -0xeeeeef)
        yearViewSchemeTextColor = array.getColor(R.styleable.CalendarView_year_view_scheme_color, schemeThemeColor)

        if (minYear <= MIN_YEAR) minYear = 1971
        if (maxYear >= MAX_YEAR) maxYear = 2055
        array.recycle()
        init()
    }

    private fun init() {
        currentDay = Calendar()
        val d = Date()
        currentDay?.year = Util.getDate(java.util.Calendar.YEAR, d)
        currentDay?.month = Util.getDate(java.util.Calendar.MONTH, d)
        currentDay?.day = Util.getDate(java.util.Calendar.DAY_OF_MONTH, d)
        currentDay?.isCurrentDay = true
        currentDay?.let {
            LunarCalendar.setupLunarCalendar(it)
        }
        setRange(minYear, minYearMonth, maxYear, maxYearMonth)
    }


    fun setRange(minYear: Int, minYearMonth: Int,
                 maxYear: Int, maxYearMonth: Int) {
        this.minYear = minYear
        this.minYearMonth = minYearMonth
        this.maxYear = maxYear
        this.maxYearMonth = maxYearMonth
        if (this.maxYear < currentDay!!.year) {
            this.maxYear = currentDay!!.year
        }
        val y = currentDay!!.year - this.minYear
        mCurrentMonthViewItem = 12 * y + currentDay!!.month - this.minYearMonth
        mCurrentWeekViewItem = Util.getWeekFromCalendarBetweenYearAndYear(currentDay!!, this.minYear, this.minYearMonth)
    }

    fun setTextColor(curDayTextColor: Int, curMonthTextColor: Int, otherMonthTextColor: Int, curMonthLunarTextColor: Int, otherMonthLunarTextColor: Int) {
        this.curDayTextColor = curDayTextColor
        this.otherMonthTextColor = otherMonthTextColor
        currentMonthTextColor = curMonthTextColor
        currentMonthLunarTextColor = curMonthLunarTextColor
        this.otherMonthLunarTextColor = otherMonthLunarTextColor
    }

    fun setSchemeColor(schemeColor: Int, schemeTextColor: Int, schemeLunarTextColor: Int) {
        this.schemeThemeColor = schemeColor
        this.schemeTextColor = schemeTextColor
        this.schemeLunarTextColor = schemeLunarTextColor
    }

    fun setYearViewTextColor(yearViewMonthTextColor: Int, yearViewDayTextColor: Int, yarViewSchemeTextColor: Int) {
        this.yearViewMonthTextColor = yearViewMonthTextColor
        this.yearViewDayTextColor = yearViewDayTextColor
        this.yearViewSchemeTextColor = yarViewSchemeTextColor
    }

    fun setSelectColor(selectedColor: Int, selectedTextColor: Int, selectedLunarTextColor: Int) {
        this.selectedThemeColor = selectedColor
        this.selectedTextColor = selectedTextColor
        this.selectedLunarTextColor = selectedLunarTextColor
    }

    fun setThemeColor(selectedThemeColor: Int, schemeColor: Int) {
        this.selectedThemeColor = selectedThemeColor
        this.schemeThemeColor = schemeColor
    }


    fun createCurrentDate(): Calendar {
        val calendar = Calendar()
        calendar.year = currentDay!!.year
        calendar.week = currentDay!!.week
        calendar.month = currentDay!!.month
        calendar.day = currentDay!!.day
        LunarCalendar.setupLunarCalendar(calendar)
        return calendar
    }

    companion object {


        /**
         * 全部显示
         */
        val MODE_ALL_MONTH = 0
        /**
         * 仅显示当前月份
         */
        val MODE_ONLY_CURRENT_MONTH = 1

        /**
         * 自适应显示，不会多出一行，但是会自动填充
         */
        val MODE_FIT_MONTH = 2

        /**
         * 支持转换的最小农历年份
         */
        val MIN_YEAR = 1900
        /**
         * 支持转换的最大农历年份
         */
        private val MAX_YEAR = 2099
    }
}
