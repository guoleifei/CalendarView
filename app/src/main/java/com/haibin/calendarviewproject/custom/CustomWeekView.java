package com.haibin.calendarviewproject.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekView;

/**
 * 演示一个变态需求的周视图
 * Created by huanghaibin on 2018/2/9.
 */

public class CustomWeekView extends WeekView {


    private int mRadius;

    /**
     * 自定义魅族标记的文本画笔
     */
    private Paint mTextPaint = new Paint();


    /**
     * 24节气画笔
     */
    private Paint mSolarTermTextPaint = new Paint();

    /**
     * 背景圆点
     */
    private Paint mPointPaint = new Paint();

    /**
     * 今天的背景色
     */
    private Paint mCurrentDayPaint = new Paint();


    /**
     * 圆点半径
     */
    private float mPointRadius;

    private int mPadding;

    private float mCircleRadius;
    /**
     * 自定义魅族标记的圆形背景
     */
    private Paint mSchemeBasicPaint = new Paint();

    private float mSchemeBaseLine;

    public CustomWeekView(Context context) {
        super(context);
        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);


        mSolarTermTextPaint.setColor(0xff489dff);
        mSolarTermTextPaint.setAntiAlias(true);
        mSolarTermTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setFakeBoldText(true);
        mSchemeBasicPaint.setColor(Color.WHITE);

        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setTextAlign(Paint.Align.CENTER);
        mPointPaint.setColor(Color.RED);


        mCurrentDayPaint.setAntiAlias(true);
        mCurrentDayPaint.setStyle(Paint.Style.FILL);
        mCurrentDayPaint.setColor(0xFFeaeaea);


        mCircleRadius = dipToPx(getContext(), 7);

        mPadding = dipToPx(getContext(), 3);

        mPointRadius = dipToPx(context, 2);

        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mCircleRadius - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1);
    }


    @Override
    protected void onPreviewHook() {
        mSolarTermTextPaint.setTextSize(getMCurMonthLunarTextPaint().getTextSize());
        mRadius = Math.min(getMItemWidth(), getMItemHeight()) / 11 * 5;
    }


    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        int cx = x + getMItemWidth() / 2;
        int cy = getMItemHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, getMSelectedPaint());
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {

        boolean isSelected = isSelected(calendar);
        if (isSelected) {
            mPointPaint.setColor(Color.WHITE);
        } else {
            mPointPaint.setColor(Color.GRAY);
        }

        canvas.drawCircle(x + getMItemWidth() / 2, getMItemHeight() - 3 * mPadding, mPointRadius, mPointPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + getMItemWidth() / 2;
        int cy = getMItemHeight() / 2;
        int top = -getMItemHeight() / 6;

        if (calendar.isCurrentDay() && !isSelected) {
            canvas.drawCircle(cx, cy, mRadius, mCurrentDayPaint);
        }

        if(hasScheme){
            canvas.drawCircle(x + getMItemWidth() - mPadding - mCircleRadius / 2, mPadding + mCircleRadius, mCircleRadius, mSchemeBasicPaint);

            mTextPaint.setColor(calendar.getSchemeColor());

            canvas.drawText(calendar.getScheme(), x + getMItemWidth() - mPadding - mCircleRadius, mPadding + mSchemeBaseLine, mTextPaint);
        }

        if (calendar.isWeekend() && calendar.isCurrentMonth()) {
            getMCurMonthTextPaint().setColor(0xFF489dff);
            getMCurMonthLunarTextPaint().setColor(0xFF489dff);
            getMSchemeTextPaint().setColor(0xFF489dff);
            getMSchemeLunarTextPaint().setColor(0xFF489dff);
            getMOtherMonthLunarTextPaint().setColor(0xFF489dff);
            getMOtherMonthTextPaint().setColor(0xFF489dff);
        } else {
            getMCurMonthTextPaint().setColor(0xff333333);
            getMCurMonthLunarTextPaint().setColor(0xffCFCFCF);
            getMSchemeTextPaint().setColor(0xff333333);
            getMSchemeLunarTextPaint().setColor(0xffCFCFCF);

            getMOtherMonthTextPaint().setColor(0xFFe1e1e1);
            getMOtherMonthLunarTextPaint().setColor(0xFFe1e1e1);
        }

        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    getMSelectTextPaint());
            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10, getMSelectedLunarTextPaint());
        } else if (hasScheme) {

            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentMonth() ? getMSchemeTextPaint() : getMOtherMonthTextPaint());

            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10,
                    !TextUtils.isEmpty(calendar.getSolarTerm()) ? mSolarTermTextPaint : getMSchemeLunarTextPaint());
        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, getMTextBaseLine() + top,
                    calendar.isCurrentDay() ? getMCurDayTextPaint() :
                            calendar.isCurrentMonth() ? getMCurMonthTextPaint() : getMOtherMonthTextPaint());

            canvas.drawText(calendar.getLunar(), cx, getMTextBaseLine() + getMItemHeight() / 10,
                    calendar.isCurrentDay() ? getMCurDayLunarTextPaint() :
                            !TextUtils.isEmpty(calendar.getSolarTerm()) ? mSolarTermTextPaint :
                                    calendar.isCurrentMonth() ?
                                            getMCurMonthLunarTextPaint() : getMOtherMonthLunarTextPaint());
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
