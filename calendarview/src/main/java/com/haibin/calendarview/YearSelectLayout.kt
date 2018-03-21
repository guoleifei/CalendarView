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
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

/**
 * 年份+月份选择布局
 * ViewPager + RecyclerView
 */
class YearSelectLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {
    private var mYearCount: Int = 0
    private var mDelegate: CustomCalendarViewDelegate? = null
    private var mListener: YearRecyclerView.OnMonthSelectedListener? = null


    internal fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate
        this.mYearCount = mDelegate!!.maxYear - mDelegate!!.minYear + 1
        adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return mYearCount
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = YearRecyclerView(context)
                container.addView(view)
                view.setup(mDelegate!!)
                view.setOnMonthSelectedListener(mListener!!)
                view.init(position + mDelegate!!.minYear)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                if (`object` is YearRecyclerView)
                    container.removeView(`object`)
            }
        }
        currentItem = mDelegate!!.currentDay!!.year - mDelegate!!.minYear
    }

    internal fun notifyDataSetChanged() {
        this.mYearCount = mDelegate!!.maxYear - mDelegate!!.minYear + 1
        adapter!!.notifyDataSetChanged()
    }

    internal fun scrollToYear(year: Int, smoothScroll: Boolean) {
        setCurrentItem(year - mDelegate!!.minYear, smoothScroll)
    }


    internal fun update() {
        (0 until childCount)
                .map { getChildAt(it) as YearRecyclerView }
                .forEach { it.adapter.notifyDataSetChanged() }
    }

    fun setOnMonthSelectedListener(listener: YearRecyclerView.OnMonthSelectedListener) {
        this.mListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpecTemp = View.MeasureSpec.makeMeasureSpec(getHeight(context, this), View.MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpecTemp)
    }

    /**
     * 计算相对高度
     * @param context context
     * @param view view
     * @return 月视图选择器最适合的高度
     */
    private fun getHeight(context: Context, view: View): Int {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val h = display.height
        val location = IntArray(2)
        view.getLocationInWindow(location)
        view.getLocationOnScreen(location)
        return h - location[1]
    }
}
