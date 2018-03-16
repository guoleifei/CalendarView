package com.haibin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

/**
 * 默认高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */

class DefaultMonthView(context: Context) : MonthView(context) {

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
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        canvas.drawRect((x + mPadding).toFloat(), (y + mPadding).toFloat(), (x + mItemWidth - mPadding).toFloat(), (y + mItemHeight - mPadding).toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor

        canvas.drawCircle((x + mItemWidth).toFloat() - mPadding.toFloat() - mRadio / 2, y.toFloat() + mPadding.toFloat() + mRadio, mRadio, mSchemeBasicPaint)

        canvas.drawText(calendar.scheme!!, (x + mItemWidth).toFloat() - mPadding.toFloat() - mRadio, y.toFloat() + mPadding.toFloat() + mSchemeBaseLine, mTextPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 6

        when {
            isSelected -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                        mSelectTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(), mSelectedLunarTextPaint)
            }
            hasScheme -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                        if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)

                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(), mSchemeLunarTextPaint)
            }
            else -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                        if (calendar.isCurrentDay)
                            mCurDayTextPaint
                        else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(),
                        when {
                            calendar.isCurrentDay -> mCurDayLunarTextPaint
                            calendar.isCurrentMonth -> mCurMonthLunarTextPaint
                            else -> mOtherMonthLunarTextPaint
                        })
            }
        }
    }
}
