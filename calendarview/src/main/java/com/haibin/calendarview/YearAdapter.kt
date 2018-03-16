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
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class YearAdapter(context: Context) : BaseRecyclerAdapter<Month>(context) {
    private var mDelegate: CustomCalendarViewDelegate? = null
    private var mItemHeight: Int = 0
    private val mTextHeight: Int

    init {
        mTextHeight = Util.dipToPx(context, 56f)
    }

    fun setup(delegate: CustomCalendarViewDelegate) {
        this.mDelegate = delegate
    }

    fun setItemHeight(itemHeight: Int) {
        this.mItemHeight = itemHeight
    }

    override fun onCreateDefaultViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        return YearViewHolder(mInflater.inflate(R.layout.cv_item_list_year, parent, false), this.mDelegate!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Month, position: Int) {
        val h = holder as YearViewHolder
        val view = h.mYearView
        view.setSchemes(mDelegate!!.mSchemeDate!!)
        view.setSchemeColor(mDelegate!!.yearViewSchemeTextColor)
        view.setTextStyle(mDelegate!!.yearViewDayTextSize,
                mDelegate!!.yearViewDayTextColor)
        view.init(item.diff, item.count, item.year, item.month)
        view.getLayoutParams().height = mItemHeight - mTextHeight
        h.mTextMonth.text = String.format("%s月", item.month)
        h.mTextMonth.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDelegate!!.yearViewMonthTextSize.toFloat())
        h.mTextMonth.setTextColor(mDelegate!!.yearViewMonthTextColor)
    }

    private class YearViewHolder internal constructor(itemView: View, delegate: CustomCalendarViewDelegate) : RecyclerView.ViewHolder(itemView) {
        internal var mYearView: YearView
        internal var mTextMonth: TextView

        init {
            mYearView = itemView.findViewById<View>(R.id.selectView) as YearView
            mYearView.setup(delegate)
            mTextMonth = itemView.findViewById<View>(R.id.tv_month) as TextView
        }
    }
}
