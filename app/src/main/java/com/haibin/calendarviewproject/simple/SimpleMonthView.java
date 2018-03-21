package com.haibin.calendarviewproject.simple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

/**
 * 高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */

public class SimpleMonthView extends MonthView {

    private int mRadius;

    public SimpleMonthView(Context context) {
        super(context);
    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(getMItemWidth(), getMItemHeight()) / 5 * 2;
        getMSchemePaint().setStyle(Paint.Style.STROKE);
        getMSchemePaint().setShadowLayer(15, 1, 3, 0xAA333333);
        setLayerType( LAYER_TYPE_SOFTWARE , null);
    }

    @Override
    public void onLoopStart(int x, int y) {

    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        int cx = x + getMItemWidth() / 2;
        int cy = y + getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSelectedPaint());
        return false;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        int cx = x + getMItemWidth() / 2;
        int cy = y + getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSchemePaint());
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        float baselineY = getMTextBaseLine() + y;
        int cx = x + getMItemWidth() / 2;

        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()),
                    cx,
                    baselineY,
                    getMSelectTextPaint());
        }else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()),
                    cx,
                    baselineY,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMOtherMonthTextPaint());

        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, baselineY,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMOtherMonthTextPaint());
        }
    }
}
