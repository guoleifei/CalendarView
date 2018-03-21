package com.haibin.calendarviewproject.index;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

/**
 * 下标标记的日历控件
 * Created by huanghaibin on 2017/11/15.
 */

public class IndexMonthView extends MonthView {
    private Paint mSchemeBasicPaint = new Paint();
    private int mPadding;
    private int mH, mW;

    public IndexMonthView(Context context) {
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

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        getMSelectedPaint().setStyle(Paint.Style.FILL);
        canvas.drawRect(x + mPadding, y + mPadding, x + getMItemWidth() - mPadding, y + getMItemHeight() - mPadding, getMSelectedPaint());
        return true;
    }

    /**
     * onDrawSelected
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        mSchemeBasicPaint.setColor(calendar.getSchemeColor());
        canvas.drawRect(x + getMItemWidth() / 2 - mW / 2,
                y + getMItemHeight() - mH * 2 - mPadding,
                x + getMItemWidth() / 2 + mW / 2,
                y + getMItemHeight() - mH - mPadding, mSchemeBasicPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        int cx = x + getMItemWidth() / 2;
        int top = y - getMItemHeight() / 6;
        if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMOtherMonthTextPaint());

            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + y + getMItemHeight() / 10, getMCurMonthLunarTextPaint());

        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMOtherMonthTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + y + getMItemHeight() / 10, getMCurMonthLunarTextPaint());
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
