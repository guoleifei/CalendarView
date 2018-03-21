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
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

/**
 * 年份布局选择View
 */
class YearRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {
    private var mDelegate: CustomCalendarViewDelegate? = null
    private val mAdapter: YearAdapter = YearAdapter(context)
    private var mListener: OnMonthSelectedListener? = null

    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = mAdapter
        mAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, itemId: Long) {
                if (mListener != null && mDelegate != null) {
                    val month = mAdapter.getItem(position) ?: return
                    if (!Util.isMonthInRange(month.year, month.month,
                                    mDelegate!!.minYear, mDelegate!!.minYearMonth,
                                    mDelegate!!.maxYear, mDelegate!!.maxYearMonth)) {
                        return
                    }
                    mListener!!.onMonthSelected(month.year, month.month)
                }
            }
        })
    }

    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate
        this.mAdapter.setup(delegate)
    }

    /**
     * 初始化年视图
     * @param year year
     */
    internal fun init(year: Int) {
        val date = java.util.Calendar.getInstance()
        for (i in 1..12) {
            date.set(year, i - 1, 1)
            val firstDayOfWeek = date.get(java.util.Calendar.DAY_OF_WEEK) - 1//月第一天为星期几,星期天 == 0
            val mDaysCount = Util.getMonthDaysCount(year, i)
            val month = Month()
            month.diff = firstDayOfWeek
            month.count = mDaysCount
            month.month = i
            month.year = year
            mAdapter.addItem(month)
        }
    }

    /**
     * 月份选择事件
     * @param listener listener
     */
    internal fun setOnMonthSelectedListener(listener: OnMonthSelectedListener) {
        this.mListener = listener
    }


    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        val h = View.MeasureSpec.getSize(heightSpec)
        mAdapter.setItemHeight(h / 4)
    }

    interface OnMonthSelectedListener {
        fun onMonthSelected(year: Int, month: Int)
    }
}
