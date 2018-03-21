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
import java.util.*

/**
 * 一些辅助计算工具
 */
object Util {

    private val ONE_DAY = (1000 * 3600 * 24).toLong()

    fun getDate(type: Int, date: Date): Int {
        val calendar: java.util.Calendar = java.util.Calendar.getInstance()
        calendar.time = date
        return if (type == java.util.Calendar.MONTH) calendar.get(type) + 1 else calendar.get(type)
    }

    /**
     * 判断一个日期是否是周末，即周六日
     *
     * @param calendar calendar
     * @return 判断一个日期是否是周末，即周六日
     */
    fun isWeekend(calendar: Calendar): Boolean {
        val week = getWeekFormCalendar(calendar)
        return week == 0 || week == 6
    }

    /**
     * 获取某月的天数
     *
     * @param year  年
     * @param month 月
     * @return 某月的天数
     */
    fun getMonthDaysCount(year: Int, month: Int): Int {
        var count = 0
        //判断大月份
        if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month == 10 || month == 12) {
            count = 31
        }

        //判断小月
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            count = 30
        }

        //判断平年与闰年
        if (month == 2) {
            if (isLeapYear(year)) {
                count = 29
            } else {
                count = 28
            }
        }
        return count
    }


    /**
     * 是否是闰年
     *
     * @param year year
     * @return return
     */
    fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0
    }

    /**
     * 获取某年的天数
     *
     * @param year 某一年
     * @return 366 or 365
     */
    private fun getYearCount(year: Int): Int {
        return if (isLeapYear(year)) 366 else 365
    }


    /**
     * 获取月视图的确切高度
     *
     * @param year       年
     * @param month      月
     * @param itemHeight 每项的高度
     * @return 不需要多余行的高度
     */
    fun getMonthViewHeight(year: Int, month: Int, itemHeight: Int): Int {
        val date = java.util.Calendar.getInstance()
        date.set(year, month - 1, 1)
        val firstDayOfWeek = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//月第一天为星期几,星期天 == 0
        val mDaysCount = getMonthDaysCount(year, month)
        date.set(year, month - 1, mDaysCount)
        val mLastCount = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//月最后一天为星期几,星期天 == 0
        val nextMonthDaysOffset = 6 - mLastCount//下个月的日偏移天数
        return (firstDayOfWeek + mDaysCount + nextMonthDaysOffset) / 7 * itemHeight
    }


    /**
     * 获取某年第几天是第几个月
     *
     * @param year      年
     * @param dayInYear 某年第几天
     * @return 第几个月
     */
    fun getMonthFromDayInYear(year: Int, dayInYear: Int): Int {
        var count = 0
        for (i in 1..12) {
            count += getMonthDaysCount(year, i)
            if (dayInYear <= count)
                return i
        }
        return 0
    }


    /**
     * 获取某天在该月的第几周
     *
     * @param calendar calendar
     * @return 获取某天在该月的第几周
     */
    fun getWeekFromDayInMonth(calendar: Calendar): Int {
        val date = java.util.Calendar.getInstance()
        date.set(calendar.year, calendar.month - 1, 1)
        val diff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//该月第一天为星期几,星期天 == 0，也就是偏移量
        return (calendar.day + diff - 1) / 7 + 1
    }

    /**
     * 获取某个日期是星期几
     *
     * @param calendar 某个日期
     * @return 返回某个日期是星期几
     */
    fun getWeekFormCalendar(calendar: Calendar): Int {
        val date = java.util.Calendar.getInstance()
        date.set(calendar.year, calendar.month - 1, calendar.day)
        return date.get(java.util.Calendar.DAY_OF_WEEK) - 1
    }


    /**
     * 获取某年第几周是在第几个月
     *
     * @param year       年
     * @param weekInYear 某年第几周
     * @return 第几个月
     */
    fun getMonthFromWeekFirstDayInYear(year: Int, weekInYear: Int): Int {
        val date = java.util.Calendar.getInstance()
        date.set(year, 0, 1)
        val diff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//1月第一天为星期几,星期天 == 0，也就是偏移量
        var count = 0
        val diy = (weekInYear - 1) * 7 - diff + 1
        for (i in 1..12) {
            count += getMonthDaysCount(year, i)
            if (diy <= count)
                return i
        }
        return 0
    }


    /**
     * 获取两个年份之间一共有多少周
     *
     * @param minYear minYear
     * @param maxYear maxYear
     * @return 周数
     */
    fun getWeekCountBetweenYearAndYear(minYear: Int, maxYear: Int): Int {
        if (minYear > maxYear)
            return 0
        val date = java.util.Calendar.getInstance()
        date.set(minYear, 0, 1)//1月1日
        val preDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//1月第一天为星期几,星期天 == 0，也就是偏移量
        date.set(maxYear, 11, 31)//12月31日
        val nextDiff = 7 - date.get(java.util.Calendar.DAY_OF_WEEK)//1月第一天为星期几,星期天 == 0，也就是偏移量
        var count = preDiff + nextDiff
        for (i in minYear..maxYear) {
            count += getYearCount(i)
        }
        return count / 7
    }


    /**
     * 获取两个年份之间一共有多少周
     *
     * @param minYear      minYear 最小年份
     * @param minYearMonth maxYear 最小年份月份
     * @param maxYear      maxYear 最大年份
     * @param maxYearMonth maxYear 最大年份月份
     * @return 周数
     */
    fun getWeekCountBetweenYearAndYear(minYear: Int, minYearMonth: Int, maxYear: Int, maxYearMonth: Int): Int {
        val date = java.util.Calendar.getInstance()
        date.set(minYear, minYearMonth - 1, 1)
        val minTime = date.timeInMillis//给定时间戳
        val preDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//1月第一天为星期几,星期天 == 0，也就是偏移量
        date.set(maxYear, maxYearMonth - 1, getMonthDaysCount(maxYear, maxYearMonth))
        val maxTime = date.timeInMillis//给定时间戳
        val nextDiff = 7 - date.get(java.util.Calendar.DAY_OF_WEEK)//1月第一天为星期几,星期天 == 0，也就是偏移量
        var count = preDiff + nextDiff
        val c = ((maxTime - minTime) / ONE_DAY).toInt() + 1
        count += c
        return count / 7
    }

    /**
     * 根据日期获取两个年份中第几周
     *
     * @param calendar calendar
     * @param minYear  minYear
     * @return 返回两个年份中第几周
     */
    fun getWeekFromCalendarBetweenYearAndYear(calendar: Calendar, minYear: Int, minYearMonth: Int): Int {
        val date = java.util.Calendar.getInstance()
        date.set(minYear, 0, 1)//1月1日
        val firstTime = date.timeInMillis//获得起始时间戳
        val preDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//1月第一天为星期几,星期天 == 0，也就是偏移量
        date.set(calendar.year, calendar.month - 1, if (calendar.week == 0) calendar.day + 1 else calendar.day)
        val curTime = date.timeInMillis//给定时间戳
        val c = ((curTime - firstTime) / ONE_DAY).toInt()
        val count = preDiff + c
        val weekDiff = getWeekCountDiff(minYear, minYearMonth)
        return count / 7 - weekDiff
    }


    /**
     * 是否在日期范围內
     *
     * @param calendar     calendar
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @return 是否在日期范围內
     */
    fun isCalendarInRange(calendar: Calendar, minYear: Int, minYearMonth: Int, maxYear: Int, maxYearMonth: Int): Boolean {
        val c = java.util.Calendar.getInstance()
        c.set(minYear, minYearMonth - 1, 1)
        val minTime = c.timeInMillis
        c.set(maxYear, maxYearMonth - 1, getMonthDaysCount(maxYear, maxYearMonth))
        val maxTime = c.timeInMillis
        c.set(calendar.year, calendar.month - 1, calendar.day)
        val curTime = c.timeInMillis
        return curTime in minTime..maxTime
    }

    fun isCalendarInRange(calendar: Calendar, delegate: CustomCalendarViewDelegate): Boolean {
        return isCalendarInRange(calendar, delegate.minYear, delegate.minYearMonth,
                delegate.maxYear, delegate.maxYearMonth)
    }

    /**
     * 是否在日期范围內
     *
     * @param year         year
     * @param month        month
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @return 是否在日期范围內
     */
    fun isMonthInRange(year: Int, month: Int, minYear: Int, minYearMonth: Int, maxYear: Int, maxYearMonth: Int): Boolean {
        return !(year < minYear || year > maxYear) &&
                !(year == minYear && month < minYearMonth) &&
                !(year == maxYear && month > maxYearMonth)
    }

    /**
     * 根据星期数和最小年份推算出该星期的第一天
     *
     * @param minYear      最小年份如2017
     * @param minYearMonth maxYear 最小年份月份，like : 2017-07
     * @param week         从最小年份1月1日开始的第几周
     * @return 该星期的第一天日期
     */
    fun getFirstCalendarFromWeekCount(minYear: Int, minYearMonth: Int, week: Int): Calendar {
        val date = java.util.Calendar.getInstance()
        date.set(minYear, 0, 1)//1月1日
        val firstTime = date.timeInMillis//获得起始时间戳
        val dayCount = (week + getWeekCountDiff(minYear, minYearMonth)) * 7 + 1
        val timeCount = dayCount * ONE_DAY + firstTime
        date.timeInMillis = timeCount
        val calendar = Calendar()
        calendar.year = date.get(java.util.Calendar.YEAR)
        calendar.month = date.get(java.util.Calendar.MONTH) + 1
        calendar.day = date.get(java.util.Calendar.DAY_OF_MONTH)
        return calendar
    }


    /**
     * 获取星期偏移了多少周
     *
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @return 获取星期偏移了多少周
     */
    fun getWeekCountDiff(minYear: Int, minYearMonth: Int): Int {
        if (minYearMonth == 1) {
            return -1
        }
        val date = java.util.Calendar.getInstance()
        date.set(minYear, 0, 1)//1月1日
        val firstTime = date.timeInMillis//获得起始时间戳
        val preDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//1月第一天为星期几,星期天 == 0，也就是偏移量
        date.set(minYear, minYearMonth - 1, 1)
        val minTime = date.timeInMillis//获得时间戳
        val nextDiff = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//minYearMonth月第一天为星期几,星期天 == 0，也就是偏移量
        val c = ((minTime - firstTime) / ONE_DAY).toInt() - 1
        val count = preDiff + c - nextDiff
        return count / 7
    }

    /**
     * 从一个日期Calendar中获取所处在一年中的第几个星期
     *
     * @param calendar 日期Calendar
     * @return 0 —— 53
     */
    fun getWeekFromCalendarInYear(calendar: Calendar): Int {
        val date = java.util.Calendar.getInstance()
        date.set(calendar.year, 0, 1)
        var count = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//前补位
        for (i in 1 until calendar.month) {
            count += getMonthDaysCount(calendar.year, i)
        }
        count += calendar.day - 1
        return count / 7 + 1
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    fun dipToPx(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}
