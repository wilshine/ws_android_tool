package com.ws.android.base_tool.util

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateUtilTest {

    @Test
    fun testFormat() {
        val date = Date(1609459200000) // 2021-01-01 00:00:00
        assertEquals("2021-01-01 00:00:00", DateUtil.format(date))
        assertEquals("2021-01-01", DateUtil.format(date, "yyyy-MM-dd"))
        assertEquals("", DateUtil.format(null))
    }

    @Test
    fun testParse() {
        val dateStr = "2021-01-01 00:00:00"
        val date = DateUtil.parse(dateStr)
        assertNotNull(date)
        assertEquals(1609459200000, date?.time)
        assertNull(DateUtil.parse("invalid date"))
        assertNull(DateUtil.parse(null))
    }

    @Test
    fun testCurrentTimeMillis() {
        assertTrue(DateUtil.currentTimeMillis() > 0)
    }

    @Test
    fun testCurrentDate() {
        assertNotNull(DateUtil.currentDate())
    }

    @Test
    fun testGetTimeAgo() {
        val now = Date()
        val oneMinuteAgo = Date(now.time - 60 * 1000)
        val oneHourAgo = Date(now.time - 60 * 60 * 1000)
        val oneDayAgo = Date(now.time - 24 * 60 * 60 * 1000)
        
        assertEquals("刚刚", DateUtil.getTimeAgo(now))
        assertTrue(DateUtil.getTimeAgo(oneMinuteAgo).contains("分钟前"))
        assertTrue(DateUtil.getTimeAgo(oneHourAgo).contains("小时前"))
        assertTrue(DateUtil.getTimeAgo(oneDayAgo).contains("天前"))
        assertEquals("", DateUtil.getTimeAgo(null))
    }

    @Test
    fun testIsToday() {
        assertTrue(DateUtil.isToday(Date()))
        assertFalse(DateUtil.isToday(Date(0))) // 1970-01-01
        assertFalse(DateUtil.isToday(null))
    }

    @Test
    fun testAddDays() {
        val date = Date(1609459200000) // 2021-01-01 00:00:00
        val result = DateUtil.addDays(date, 1)
        assertNotNull(result)
        assertEquals(1609545600000, result?.time) // 2021-01-02 00:00:00
        assertNull(DateUtil.addDays(null, 1))
    }

    @Test
    fun testAddMonths() {
        val date = Date(1609459200000) // 2021-01-01 00:00:00
        val result = DateUtil.addMonths(date, 1)
        assertNotNull(result)
        assertTrue(DateUtil.format(result, "yyyy-MM").endsWith("02")) // 2021-02
        assertNull(DateUtil.addMonths(null, 1))
    }

    @Test
    fun testGetDaysBetween() {
        val date1 = Date(1609459200000) // 2021-01-01 00:00:00
        val date2 = Date(1609545600000) // 2021-01-02 00:00:00
        assertEquals(1, DateUtil.getDaysBetween(date1, date2))
        assertEquals(0, DateUtil.getDaysBetween(null, date2))
        assertEquals(0, DateUtil.getDaysBetween(date1, null))
    }

    @Test
    fun testGetDayOfWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(2021, 0, 1) // 2021-01-01 星期五
        assertEquals("星期五", DateUtil.getDayOfWeek(calendar.time))
        assertEquals("", DateUtil.getDayOfWeek(null))
    }

    @Test
    fun testGetDaysInMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(2021, 0, 1) // 2021-01
        assertEquals(31, DateUtil.getDaysInMonth(calendar.time))
        calendar.set(2021, 1, 1) // 2021-02
        assertEquals(28, DateUtil.getDaysInMonth(calendar.time))
        assertEquals(0, DateUtil.getDaysInMonth(null))
    }

    @Test
    fun testIsLeapYear() {
        val calendar = Calendar.getInstance()
        calendar.set(2020, 0, 1)
        assertTrue(DateUtil.isLeapYear(calendar.time))
        calendar.set(2021, 0, 1)
        assertFalse(DateUtil.isLeapYear(calendar.time))
        assertFalse(DateUtil.isLeapYear(null))
    }

    @Test
    fun testGetStartOfDay() {
        val date = DateUtil.parse("2021-01-01 15:30:45")
        val result = DateUtil.getStartOfDay(date)
        assertNotNull(result)
        assertEquals("2021-01-01 00:00:00", DateUtil.format(result))
        assertNull(DateUtil.getStartOfDay(null))
    }

    @Test
    fun testGetEndOfDay() {
        val date = DateUtil.parse("2021-01-01 15:30:45")
        val result = DateUtil.getEndOfDay(date)
        assertNotNull(result)
        assertEquals("2021-01-01 23:59:59", DateUtil.format(result))
        assertNull(DateUtil.getEndOfDay(null))
    }
} 