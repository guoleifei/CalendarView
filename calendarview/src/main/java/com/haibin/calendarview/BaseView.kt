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
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 基本的日历View，派生出MonthView 和 WeekView
 * Created by huanghaibin on 2018/1/23.
 */

abstract class BaseView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), View.OnClickListener, View.OnLongClickListener {

    internal lateinit var mDelegate: CustomCalendarViewDelegate

    /**
     * 当前月份日期的笔
     */
    protected var mCurMonthTextPaint = Paint()

    /**
     * 其它月份日期颜色
     */
    protected var mOtherMonthTextPaint = Paint()

    /**
     * 当前月份农历文本颜色
     */
    protected var mCurMonthLunarTextPaint = Paint()


    /**
     * 当前月份农历文本颜色
     */
    protected var mSelectedLunarTextPaint = Paint()

    /**
     * 其它月份农历文本颜色
     */
    protected var mOtherMonthLunarTextPaint = Paint()

    /**
     * 其它月份农历文本颜色
     */
    protected var mSchemeLunarTextPaint = Paint()

    /**
     * 标记的日期背景颜色画笔
     */
    protected var mSchemePaint = Paint()

    /**
     * 被选择的日期背景色
     */
    protected var mSelectedPaint = Paint()

    /**
     * 标记的文本画笔
     */
    protected var mSchemeTextPaint = Paint()

    /**
     * 选中的文本画笔
     */
    protected var mSelectTextPaint = Paint()

    /**
     * 当前日期文本颜色画笔
     */
    protected var mCurDayTextPaint = Paint()

    /**
     * 当前日期文本颜色画笔
     */
    protected var mCurDayLunarTextPaint = Paint()

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    internal var mParentLayout: CalendarLayout? = null

    /**
     * 日历项
     */
    internal var mItems: MutableList<Calendar>? = null

    /**
     * 每一项的高度
     */
    var mItemHeight: Int = 0

    /**
     * 每一项的宽度
     */
    var mItemWidth: Int = 0

    /**
     * Text的基线
     */
    protected var mTextBaseLine: Float = 0.toFloat()

    /**
     * 点击的x、y坐标
     */
    internal var mX: Float = 0.toFloat()
    internal var mY: Float = 0.toFloat()

    /**
     * 是否点击
     */
    internal var isClick = true

    /**
     * 当前点击项
     */
    internal var mCurrentItem = -1

    init {
        initPaint(context)
    }

    /**
     * 初始化配置
     *
     * @param context context
     */
    private fun initPaint(context: Context) {
        mCurMonthTextPaint.isAntiAlias = true
        mCurMonthTextPaint.textAlign = Paint.Align.CENTER
        mCurMonthTextPaint.color = -0xeeeeef
        mCurMonthTextPaint.isFakeBoldText = true
        mCurMonthTextPaint.textSize = Util.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mOtherMonthTextPaint.isAntiAlias = true
        mOtherMonthTextPaint.textAlign = Paint.Align.CENTER
        mOtherMonthTextPaint.color = -0x1e1e1f
        mOtherMonthTextPaint.isFakeBoldText = true
        mOtherMonthTextPaint.textSize = Util.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mCurMonthLunarTextPaint.isAntiAlias = true
        mCurMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        mSelectedLunarTextPaint.isAntiAlias = true
        mSelectedLunarTextPaint.textAlign = Paint.Align.CENTER

        mOtherMonthLunarTextPaint.isAntiAlias = true
        mOtherMonthLunarTextPaint.textAlign = Paint.Align.CENTER


        mSchemeLunarTextPaint.isAntiAlias = true
        mSchemeLunarTextPaint.textAlign = Paint.Align.CENTER

        mSchemeTextPaint.isAntiAlias = true
        mSchemeTextPaint.style = Paint.Style.FILL
        mSchemeTextPaint.textAlign = Paint.Align.CENTER
        mSchemeTextPaint.color = -0x12acad
        mSchemeTextPaint.isFakeBoldText = true
        mSchemeTextPaint.textSize = Util.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mSelectTextPaint.isAntiAlias = true
        mSelectTextPaint.style = Paint.Style.FILL
        mSelectTextPaint.textAlign = Paint.Align.CENTER
        mSelectTextPaint.color = -0x12acad
        mSelectTextPaint.isFakeBoldText = true
        mSelectTextPaint.textSize = Util.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mSchemePaint.isAntiAlias = true
        mSchemePaint.style = Paint.Style.FILL
        mSchemePaint.strokeWidth = 2f
        mSchemePaint.color = -0x101011

        mCurDayTextPaint.isAntiAlias = true
        mCurDayTextPaint.textAlign = Paint.Align.CENTER
        mCurDayTextPaint.color = Color.RED
        mCurDayTextPaint.isFakeBoldText = true
        mCurDayTextPaint.textSize = Util.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mCurDayLunarTextPaint.isAntiAlias = true
        mCurDayLunarTextPaint.textAlign = Paint.Align.CENTER
        mCurDayLunarTextPaint.color = Color.RED
        mCurDayLunarTextPaint.isFakeBoldText = true
        mCurDayLunarTextPaint.textSize = Util.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mSelectedPaint.isAntiAlias = true
        mSelectedPaint.style = Paint.Style.FILL
        mSelectedPaint.strokeWidth = 2f

        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    /**
     * 初始化所有UI配置
     *
     * @param delegate delegate
     */
    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate

        this.mCurDayTextPaint.color = delegate.curDayTextColor
        this.mCurDayLunarTextPaint.color = delegate.curDayLunarTextColor
        this.mCurMonthTextPaint.color = delegate.currentMonthTextColor
        this.mOtherMonthTextPaint.color = delegate.otherMonthTextColor
        this.mCurMonthLunarTextPaint.color = delegate.currentMonthLunarTextColor
        this.mSelectedLunarTextPaint.color = delegate.selectedLunarTextColor
        this.mSelectTextPaint.color = delegate.selectedTextColor
        this.mOtherMonthLunarTextPaint.color = delegate.otherMonthLunarTextColor
        this.mSchemeLunarTextPaint.color = delegate.schemeLunarTextColor

        this.mSchemePaint.color = delegate.schemeThemeColor
        this.mSchemeTextPaint.color = delegate.schemeTextColor


        this.mCurMonthTextPaint.textSize = delegate.dayTextSize.toFloat()
        this.mOtherMonthTextPaint.textSize = delegate.dayTextSize.toFloat()
        this.mCurDayTextPaint.textSize = delegate.dayTextSize.toFloat()
        this.mSchemeTextPaint.textSize = delegate.dayTextSize.toFloat()
        this.mSelectTextPaint.textSize = delegate.dayTextSize.toFloat()

        this.mCurMonthLunarTextPaint.textSize = delegate.lunarTextSize.toFloat()
        this.mSelectedLunarTextPaint.textSize = delegate.lunarTextSize.toFloat()
        this.mCurDayLunarTextPaint.textSize = delegate.lunarTextSize.toFloat()
        this.mOtherMonthLunarTextPaint.textSize = delegate.lunarTextSize.toFloat()
        this.mSchemeLunarTextPaint.textSize = delegate.lunarTextSize.toFloat()

        this.mSelectedPaint.style = Paint.Style.FILL
        this.mSelectedPaint.color = delegate.selectedThemeColor
        setItemHeight(delegate.calendarItemHeight)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1)
            return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                isClick = true
            }
            MotionEvent.ACTION_MOVE -> {
                val mDY: Float
                if (isClick) {
                    mDY = event.y - mY
                    isClick = Math.abs(mDY) <= 50
                }
            }
            MotionEvent.ACTION_UP -> {
                mX = event.x
                mY = event.y
            }
        }
        return super.onTouchEvent(event)
    }


    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    protected open fun onPreviewHook() {
        // TODO: 2017/11/16
    }


    /**
     * 设置高度
     *
     * @param itemHeight itemHeight
     */
    private fun setItemHeight(itemHeight: Int) {
        this.mItemHeight = itemHeight
        val metrics = mCurMonthTextPaint.fontMetrics
        mTextBaseLine = mItemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2
    }


    /**
     * 是否是选中的
     *
     * @param calendar calendar
     * @return true or false
     */
    protected fun isSelected(calendar: Calendar): Boolean {
        return mItems?.indexOf(calendar) == mCurrentItem
//        return mItems != null && mItems!!.indexOf(calendar) == mCurrentItem
    }

    abstract fun update()

    companion object {

        /**
         * 字体大小
         */
        internal const val TEXT_SIZE = 14
    }
}
