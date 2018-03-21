package com.haibin.calendarviewproject.colorful;

import android.content.Context;
import android.graphics.Canvas;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekView;

/**
 * 多彩周视图
 * Created by huanghaibin on 2017/11/29.
 */

public class ColorfulWeekView extends WeekView {

    private int mRadius;

    public ColorfulWeekView(Context context) {
        super(context);
    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(getMItemWidth(), getMItemHeight()) / 5 * 2;
    }

    /**
     * 如果需要点击Scheme没有效果，则return true
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return false 则不绘制onDrawScheme，因为这里背景色是互斥的
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        int cx = x + getMItemWidth() / 2;
        int cy = getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSelectedPaint());
        return true;
    }


    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {
        int cx = x + getMItemWidth() / 2;
        int cy = getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSchemePaint());
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + getMItemWidth() / 2;
        int top = -getMItemHeight() / 8;
        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    getMSelectTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10, getMSelectedLunarTextPaint());
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMSchemeTextPaint());

            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10, getMSchemeLunarTextPaint());
        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMCurMonthTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10, getMCurMonthLunarTextPaint());
        }
    }
}
