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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 星期栏，如果你要使用星期栏自定义，切记XML使用 merge，不要使用LinearLayout
 * Created by huanghaibin on 2017/11/30.
 */
class WeekBar(context: Context) : LinearLayout(context) {
    private var mDelegate: CustomCalendarViewDelegate? = null

    init {
        if ("com.haibin.calendarview.WeekBar" == javaClass.name) {
            LayoutInflater.from(context).inflate(R.layout.cv_week_bar, this, true)
        }
    }

    /**
     * 传递属性
     *
     * @param delegate delegate
     */
    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate
        if ("com.haibin.calendarview.WeekBar".equals(javaClass.name, ignoreCase = true)) {
            setTextColor(delegate.weekTextColor)
            setBackgroundColor(delegate.weekBackground)
        }
    }

    /**
     * 设置文本颜色，
     * 如果这里报错了，请确定你自定义XML文件跟布局是不是使用merge，而不是LinearLayout
     *
     * @param color color
     */
    internal fun setTextColor(color: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as TextView).setTextColor(color)
        }
    }


    /**
     * 日期选择事件，这里提供这个回调，可以方便定制WeekBar需要
     *
     * @param calendar calendar 选择的日期
     * @param isClick  isClick 点击
     */
     fun onDateSelected(calendar: Calendar, isClick: Boolean) {

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpecTemp =  mDelegate?.let {
            View.MeasureSpec.makeMeasureSpec(it.weekBarHeight, View.MeasureSpec.EXACTLY)
        }?:View.MeasureSpec.makeMeasureSpec(Util.dipToPx(context, 40f), View.MeasureSpec.EXACTLY)
//        if (mDelegate != null) {
//            heightMeasureSpecTemp = View.MeasureSpec.makeMeasureSpec(mDelegate!!.weekBarHeight, View.MeasureSpec.EXACTLY)
//        } else {
//            heightMeasureSpecTemp = View.MeasureSpec.makeMeasureSpec(Util.dipToPx(context, 40f), View.MeasureSpec.EXACTLY)
//        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpecTemp)
    }

}
