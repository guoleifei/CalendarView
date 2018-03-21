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
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 年份视图
 * Created by haibin on 2017/3/6.
 */

class YearView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var mDiff: Int = 0//第一天偏离周日多少天
    private var mCount: Int = 0//总数
    private var mLastCount: Int = 0//最后一行的天数
    private var mLine: Int = 0//多少行
    private val mPaint = Paint()
    private val mSchemePaint = Paint()
    private var mSchemes: List<Calendar>? = null
    private var mCalendar: Calendar? = null

    init {
        mPaint.isAntiAlias = true
        mPaint.textAlign = Paint.Align.CENTER
        mSchemePaint.isAntiAlias = true
        mSchemePaint.textAlign = Paint.Align.CENTER
        measureLine()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val pLeft = paddingLeft
        val w = (width - paddingLeft - paddingRight) / 7
        val h = (height - paddingTop - paddingBottom) / 6
        var d = 0
        for (i in 0 until mLine) {
            if (i == 0) {//第一行
                for (j in 0 until 7 - mDiff) {
                    ++d
                    canvas.drawText((j + 1).toString(), (mDiff * w + j * w + pLeft + w / 2).toFloat(), h.toFloat(), if (isScheme(d)) mSchemePaint else mPaint)
                }
            } else if (i == mLine - 1 && mLastCount != 0) {
                var first = mCount - mLastCount + 1
                for (j in 0 until mLastCount) {
                    ++d
                    canvas.drawText(first.toString(), (j * w + pLeft + w / 2).toFloat(), ((i + 1) * h).toFloat(), if (isScheme(d)) mSchemePaint else mPaint)
                    ++first
                }
            } else {
                var first = i * 7 - mDiff + 1
                for (j in 0..6) {
                    ++d
                    canvas.drawText(first.toString(), (j * w + pLeft + w / 2).toFloat(), ((i + 1) * h).toFloat(), if (isScheme(d)) mSchemePaint else mPaint)
                    ++first
                }
            }
        }
    }

    /**
     * 计算行数
     */
    private fun measureLine() {
        val offset = mCount - (7 - mDiff)
        mLine = 1 + (if (offset % 7 == 0) 0 else 1) + offset / 7
        mLastCount = offset % 7
    }

    /**
     * 初始化月份卡
     *
     * @param mDiff  偏离天数
     * @param mCount 当月总天数
     * @param mYear  哪一年
     * @param mMonth 哪一月
     */
    internal fun init(mDiff: Int, mCount: Int, mYear: Int, mMonth: Int) {
        this.mDiff = mDiff
        this.mCount = mCount
        mCalendar = Calendar()
        mCalendar!!.year = mYear
        mCalendar!!.month = mMonth
        measureLine()
        invalidate()
    }

    internal fun setSchemes(mSchemes: List<Calendar>) {
        this.mSchemes = mSchemes
    }

    internal fun setup(delegate: CustomCalendarViewDelegate) {
        mSchemePaint.color = delegate.yearViewSchemeTextColor
        mSchemePaint.textSize = delegate.yearViewDayTextSize.toFloat()
        mPaint.textSize = delegate.yearViewDayTextSize.toFloat()
        mPaint.color = delegate.yearViewDayTextColor
    }


    internal fun setSchemeColor(schemeColor: Int) {
        if (schemeColor != 0)
            mSchemePaint.color = schemeColor
        if (schemeColor == -0xcfc6c2)
            mSchemePaint.color = Color.RED
    }

    internal fun setTextStyle(textSize: Int, textColor: Int) {
        mSchemePaint.textSize = textSize.toFloat()
        mPaint.textSize = textSize.toFloat()
        mPaint.color = textColor
    }

    private fun isScheme(day: Int): Boolean {
        if (mSchemes == null || mSchemes?.isEmpty() != false)
            return false
        mCalendar?.day = day
        return mSchemes?.contains(mCalendar!!)?:false
    }
}
