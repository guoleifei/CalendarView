package com.haibin.calendarviewproject.index;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekView;

/**
 * 下标周视图
 * Created by huanghaibin on 2017/11/29.
 */

public class IndexWeekView extends WeekView {
    private Paint mSchemeBasicPaint = new Paint();
    private int mPadding;
    private int mH, mW;

    public IndexWeekView(Context context) {
        super(context);
        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setColor(0xff333333);
        mSchemeBasicPaint.setFakeBoldText(true);
        mPadding = dipToPx(getContext(), 4);
        mH = dipToPx(getContext(), 2);
        mW = dipToPx(getContext(), 8);
    }

    @Override
    protected void onPreviewHook() {
        getMCurMonthTextPaint().setTextSize(dipToPx(getContext(), 16));
        getMOtherMonthTextPaint().setTextSize(dipToPx(getContext(), 16));
        getMSchemeTextPaint().setTextSize(dipToPx(getContext(), 16));
        getMCurDayTextPaint().setTextSize(dipToPx(getContext(), 16));
        getMCurMonthLunarTextPaint().setTextSize(dipToPx(getContext(), 12));
        getMOtherMonthLunarTextPaint().setTextSize(dipToPx(getContext(), 12));
    }

    /**
     * 如果这里和 onDrawScheme 是互斥的，则 return false，
     * return true 会先绘制 onDrawSelected，再绘制onDrawSelected
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        getMSelectedPaint().setStyle(Paint.Style.FILL);
        canvas.drawRect(x + mPadding, mPadding, x + getMItemWidth() - mPadding, getMItemHeight() - mPadding, getMSelectedPaint());
        return true;
    }

    /**
     * 绘制下标标记
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     */
    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {
        mSchemeBasicPaint.setColor(calendar.getSchemeColor());
        canvas.drawRect(x + getMItemWidth() / 2 - mW / 2,
                getMItemHeight() - mH * 2 - mPadding,
                x + getMItemWidth() / 2 + mW / 2,
                getMItemHeight() - mH - mPadding, mSchemeBasicPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + getMItemWidth() / 2;
        int top = -getMItemHeight() / 6;
        if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMCurMonthTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10, getMCurMonthLunarTextPaint());
        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMCurMonthTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10, getMCurMonthLunarTextPaint());
        }
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
