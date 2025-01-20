package com.ws.android.base_tool.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期工具类
 */
object DateUtil {
    private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private val defaultLocale = Locale.getDefault()

    /**
     * 格式化日期为字符串
     * @param date 日期对象
     * @param pattern 格式模式，默认为 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的字符串
     */
    fun format(date: Date?, pattern: String = DEFAULT_FORMAT): String {
        if (date == null) return ""
        return try {
            SimpleDateFormat(pattern, defaultLocale).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 解析字符串为日期
     * @param dateStr 日期字符串
     * @param pattern 格式模式，默认为 "yyyy-MM-dd HH:mm:ss"
     * @return 解析后的日期对象，解析失败返回null
     */
    fun parse(dateStr: String?, pattern: String = DEFAULT_FORMAT): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            SimpleDateFormat(pattern, defaultLocale).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long = System.currentTimeMillis()

    /**
     * 获取当前日期对象
     */
    fun currentDate(): Date = Date()

    /**
     * 获取指定时间与当前时间的差值描述
     * @param date 指定时间
     * @return 时间差描述，如"1分钟前"、"1小时前"等
     */
    fun getTimeAgo(date: Date?): String {
        if (date == null) return ""
        
        val now = System.currentTimeMillis()
        val diff = now - date.time
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)}分钟前"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)}小时前"
            diff < TimeUnit.DAYS.toMillis(30) -> "${diff / TimeUnit.DAYS.toMillis(1)}天前"
            diff < TimeUnit.DAYS.toMillis(365) -> "${diff / TimeUnit.DAYS.toMillis(30)}个月前"
            else -> "${diff / TimeUnit.DAYS.toMillis(365)}年前"
        }
    }

    /**
     * 获取日期是否是今天
     * @param date 日期对象
     * @return 是否是今天
     */
    fun isToday(date: Date?): Boolean {
        if (date == null) return false
        val format = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
        return format.format(date) == format.format(Date())
    }

    /**
     * 获取指定天数前后的日期
     * @param days 天数，正数为后，负数为前
     * @return 计算后的日期
     */
    fun addDays(date: Date?, days: Int): Date? {
        if (date == null) return null
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }

    /**
     * 获取指定月数前后的日期
     * @param months 月数，正数为后，负数为前
     * @return 计算后的日期
     */
    fun addMonths(date: Date?, months: Int): Date? {
        if (date == null) return null
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MONTH, months)
        return calendar.time
    }

    /**
     * 获取两个日期之间的天数差
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 天数差
     */
    fun getDaysBetween(date1: Date?, date2: Date?): Long {
        if (date1 == null || date2 == null) return 0
        val diff = date2.time - date1.time
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    /**
     * 获取日期是星期几
     * @param date 日期对象
     * @return 星期几的描述（星期一 ~ 星期日）
     */
    fun getDayOfWeek(date: Date?): String {
        if (date == null) return ""
        val calendar = Calendar.getInstance()
        calendar.time = date
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "星期日"
            Calendar.MONDAY -> "星期一"
            Calendar.TUESDAY -> "星期二"
            Calendar.WEDNESDAY -> "星期三"
            Calendar.THURSDAY -> "星期四"
            Calendar.FRIDAY -> "星期五"
            Calendar.SATURDAY -> "星期六"
            else -> ""
        }
    }

    /**
     * 获取日期所在月份的天数
     * @param date 日期对象
     * @return 所在月份的天数
     */
    fun getDaysInMonth(date: Date?): Int {
        if (date == null) return 0
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * 获取日期所在年份是否是闰年
     * @param date 日期对象
     * @return 是否是闰年
     */
    fun isLeapYear(date: Date?): Boolean {
        if (date == null) return false
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }

    /**
     * 获取日期的开始时间（00:00:00）
     * @param date 日期对象
     * @return 开始时间
     */
    fun getStartOfDay(date: Date?): Date? {
        if (date == null) return null
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * 获取日期的结束时间（23:59:59）
     * @param date 日期对象
     * @return 结束时间
     */
    fun getEndOfDay(date: Date?): Date? {
        if (date == null) return null
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
} 