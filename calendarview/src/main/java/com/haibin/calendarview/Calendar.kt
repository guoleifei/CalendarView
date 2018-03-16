package com.haibin.calendarview

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * Created by guolf on 2018/3/15.
 * @author guolf
 */
@Parcelize
 class Calendar: Parcelable {
    /**
     * 年
     */
    var year: Int = 0
    /**
     * 月1-12
     */
    var month: Int = 0
    /**
     * 如果是闰月，则返回闰月
     */
    var leapMonth: Int = 0
    /**
     * 日1-31
     */
    var day: Int = 0
    /**
     * 是否是闰年
     */
    var isLeapYear: Boolean = false
    /**
     * 是否是本月,这里对应的是月视图的本月，而非当前月份，请注意
     */
    var isCurrentMonth: Boolean = false
    /**
     * 是否是今天
     */
    var isCurrentDay: Boolean = false

    /**
     * 农历字符串，没有特别大的意义，用来做简单的农历或者节日标记
     * 建议通过lunarCakendar获取完整的农历日期
     */
    var lunar: String? = null
    /**
     * 24节气
     */
    var solarTerm: String? = null
    /**
     * 公历节日
     */
    var gregorianFestival: String? = null
    /**
     * 传统农历节日
     */
    var traditionFestival: String? = null
    /**
     * 计划，可以用来标记当天是否有任务,这里是默认的，如果使用多标记，请使用下面API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    var scheme: String? = null

    /**
     * 各种自定义标记颜色、没有则选择默认颜色，如果使用多标记，请使用下面API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    var schemeColor: Int = 0

    /**
     * 多标记
     * multi scheme,using addScheme();
     */
    var schemes: MutableList<Scheme> = mutableListOf()

    /**
     * 是否是周末
     */
    var isWeekend: Boolean = false

    /**
     * 星期,0-6 对应周日到周一
     */
    var week: Int = 0
    /**
     * 获取完整的农历日期
     */
    lateinit var lunarCakendar: Calendar

    fun addScheme(scheme: Scheme) {
        schemes.add(scheme)
    }

    fun addScheme(schemeColor: Int, scheme: String) {
        schemes.add(Scheme(schemeColor, scheme))
    }

    fun addScheme(type: Int, schemeColor: Int, scheme: String) {
        schemes.add(Scheme(type, schemeColor, scheme))
    }

    fun addScheme(type: Int, schemeColor: Int, scheme: String, other: String) {
        schemes.add(Scheme(type, schemeColor, scheme, other))
    }

    fun addScheme(schemeColor: Int, scheme: String, other: String) {
        schemes.add(Scheme(schemeColor, scheme, other))
    }

    fun hasScheme(): Boolean {
        return schemes.isNotEmpty() || scheme != null
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Calendar) {
            if (other.year == year && other.month == month && other.day == day)
                return true
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return year.toString() + "" + (if (month < 10) "0" + month else month) + "" + if (day < 10) "0" + day else day
    }

}

/**
 * 事件标记服务，现在建议
 */
@Parcelize
data class Scheme(
        var type: Int,
        var shcemeColor: Int,
        var scheme: String? = null,
        var other: String? = null
) : Parcelable {
    constructor() : this(0, 0, "", "")
    constructor(schemeColor: Int, scheme: String) : this(0, schemeColor, scheme, null)
    constructor(type: Int, schemeColor: Int, scheme: String) : this(type, schemeColor, scheme, null)
    constructor(schemeColor: Int, scheme: String, other: String) : this(0, schemeColor, scheme, other)
}