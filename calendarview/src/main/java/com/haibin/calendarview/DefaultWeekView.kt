package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

/**
 * 默认高仿魅族周视图
 * Created by huanghaibin on 2017/11/29.
 */

class DefaultWeekView(context: Context) : WeekView(context) {
    private val mTextPaint = Paint()
    private val mSchemeBasicPaint = Paint()
    private val mRadio: Float
    private val mPadding: Int
    private val mSchemeBaseLine: Float

    init {

        mTextPaint.textSize = Util.dipToPx(context, 8f).toFloat()
        mTextPaint.color = -0x1
        mTextPaint.isAntiAlias = true
        mTextPaint.isFakeBoldText = true

        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.color = -0x12acad
        mSchemeBasicPaint.isFakeBoldText = true
        mRadio = Util.dipToPx(getContext(), 7f).toFloat()
        mPadding = Util.dipToPx(getContext(), 4f)
        val metrics = mSchemeBasicPaint.fontMetrics
        mSchemeBaseLine = mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + Util.dipToPx(getContext(), 1f).toFloat()

    }

    /**
     * 如果需要点击Scheme没有效果，则return true
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        canvas.drawRect((x + mPadding).toFloat(), mPadding.toFloat(), (x + mItemWidth - mPadding).toFloat(), (mItemHeight - mPadding).toFloat(), mSelectedPaint)
        return true
    }


    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor

        canvas.drawCircle((x + mItemWidth).toFloat() - mPadding.toFloat() - mRadio / 2, mPadding + mRadio, mRadio, mSchemeBasicPaint)

        canvas.drawText(calendar.scheme!!, (x + mItemWidth).toFloat() - mPadding.toFloat() - mRadio, mPadding + mSchemeBaseLine, mTextPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = -mItemHeight / 6

        when {
            isSelected -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                        mSelectTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, mSelectedLunarTextPaint)
            }
            hasScheme -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                        if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)

                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, mSchemeLunarTextPaint)
            }
            else -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                        when {
                            calendar.isCurrentDay -> mCurDayTextPaint
                            calendar.isCurrentMonth -> mCurMonthTextPaint
                            else -> mOtherMonthTextPaint
                        })
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10,
                        when {
                            calendar.isCurrentDay -> mCurDayLunarTextPaint
                            calendar.isCurrentMonth -> mCurMonthLunarTextPaint
                            else -> mOtherMonthLunarTextPaint
                        })
            }
        }
    }
}
