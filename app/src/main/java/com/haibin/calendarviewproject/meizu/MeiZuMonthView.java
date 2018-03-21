package com.haibin.calendarviewproject.meizu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;

import com.haibin.calendarview.MonthView;

/**
 * 高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */

public class MeiZuMonthView extends MonthView {

    /**
     * 自定义魅族标记的文本画笔
     */
    private Paint mTextPaint = new Paint();

    /**
     * 自定义魅族标记的圆形背景
     */
    private Paint mSchemeBasicPaint = new Paint();
    private float mRadio;
    private int mPadding;
    private float mSchemeBaseLine;

    public MeiZuMonthView(Context context) {
        super(context);

        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setFakeBoldText(true);
        mRadio = dipToPx(getContext(), 7);
        mPadding = dipToPx(getContext(), 4);
        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1);

    }

    /**
     * 绘制选中的日子
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        getMSelectedPaint().setStyle(Paint.Style.FILL);
        canvas.drawRect(x + mPadding, y + mPadding, x + getMItemWidth() - mPadding, y + getMItemHeight() - mPadding, getMSelectedPaint());
        return true;
    }

    /**
     * 绘制标记的事件日子
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        mSchemeBasicPaint.setColor(calendar.getSchemeColor());

        canvas.drawCircle(x + getMItemWidth() - mPadding - mRadio / 2, y + mPadding + mRadio, mRadio, mSchemeBasicPaint);

        canvas.drawText(calendar.getScheme(), x + getMItemWidth() - mPadding - mRadio, y + mPadding + mSchemeBaseLine, mTextPaint);
    }

    /**
     * 绘制文本
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        int cx = x + getMItemWidth() / 2;
        int top = y - getMItemHeight() / 6;

        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    getMSelectTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + y + getMItemHeight() / 10, getMSelectedLunarTextPaint());
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMOtherMonthTextPaint());

            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + y + getMItemHeight() / 10, getMCurMonthLunarTextPaint());
        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMOtherMonthTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + y + getMItemHeight() / 10,
                    calendar.isCurrentDay() ? getMCurDayLunarTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthLunarTextPaint() : getMOtherMonthLunarTextPaint());
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
