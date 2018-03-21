package com.haibin.calendarviewproject.simple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekView;

/**
 * 简单周视图
 * Created by huanghaibin on 2017/11/29.
 */

public class SimpleWeekView extends WeekView {
    private int mRadius;


    public SimpleWeekView(Context context) {
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
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        int cx = x + getMItemWidth() / 2;
        int cy = getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSelectedPaint());
        return false;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {
        int cx = x + getMItemWidth() / 2;
        int cy = getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSchemePaint());
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        float baselineY = getMTextBaseLine();
        int cx = x + getMItemWidth() / 2;
        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()),
                    cx,
                    baselineY,
                    getMSelectTextPaint());
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()),
                    cx,
                    baselineY,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMSchemeTextPaint());

        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, baselineY,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMCurMonthTextPaint());
        }
    }
}
