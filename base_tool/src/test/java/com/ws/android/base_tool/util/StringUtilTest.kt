package com.ws.android.base_tool.util

import com.ws.android.base_tool.StringUtil
import org.junit.Assert.*
import org.junit.Test

class StringUtilTest {

    @Test
    fun testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null))
        assertTrue(StringUtil.isEmpty(""))
        assertTrue(StringUtil.isEmpty(" "))
        assertTrue(StringUtil.isEmpty("\t"))
        assertTrue(StringUtil.isEmpty("\n"))
        assertFalse(StringUtil.isEmpty("test"))
        assertFalse(StringUtil.isEmpty(" test "))
    }

    @Test
    fun testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null))
        assertFalse(StringUtil.isNotEmpty(""))
        assertFalse(StringUtil.isNotEmpty(" "))
        assertTrue(StringUtil.isNotEmpty("test"))
    }

    @Test
    fun testGetOrDefault() {
        assertEquals("", StringUtil.getOrDefault(null))
        assertEquals("default", StringUtil.getOrDefault(null, "default"))
        assertEquals("test", StringUtil.getOrDefault("test"))
        assertEquals("test", StringUtil.getOrDefault("test", "default"))
    }

    @Test
    fun testEllipsis() {
        assertEquals("test...", StringUtil.ellipsis("test123456", 7))
        assertEquals("test", StringUtil.ellipsis("test", 7))
        assertEquals("", StringUtil.ellipsis(null, 7))
        assertEquals("te..", StringUtil.ellipsis("test", 4))
    }

    @Test
    fun testRemoveWhitespace() {
        assertEquals("test123", StringUtil.removeWhitespace("test 123"))
        assertEquals("test123", StringUtil.removeWhitespace("test\t123"))
        assertEquals("test123", StringUtil.removeWhitespace("test\n123"))
        assertEquals("", StringUtil.removeWhitespace(null))
    }

    @Test
    fun testIsValidEmail() {
        assertTrue(StringUtil.isValidEmail("test@example.com"))
        assertTrue(StringUtil.isValidEmail("test.name@example.com"))
        assertFalse(StringUtil.isValidEmail("test@"))
        assertFalse(StringUtil.isValidEmail("test"))
        assertFalse(StringUtil.isValidEmail(null))
    }

    @Test
    fun testIsValidPhoneNumber() {
        assertTrue(StringUtil.isValidPhoneNumber("13888888888"))
        assertTrue(StringUtil.isValidPhoneNumber("15888888888"))
        assertFalse(StringUtil.isValidPhoneNumber("1388888888")) // 10位
        assertFalse(StringUtil.isValidPhoneNumber("23888888888")) // 不是1开头
        assertFalse(StringUtil.isValidPhoneNumber(null))
    }

    @Test
    fun testFormatPhoneNumber() {
        assertEquals("138 8888 8888", StringUtil.formatPhoneNumber("13888888888"))
        assertEquals("", StringUtil.formatPhoneNumber(null))
        assertEquals("1234", StringUtil.formatPhoneNumber("1234")) // 非11位返回原值
    }

    @Test
    fun testMaskPhoneNumber() {
        assertEquals("138****8888", StringUtil.maskPhoneNumber("13888888888"))
        assertEquals("", StringUtil.maskPhoneNumber(null))
        assertEquals("1234", StringUtil.maskPhoneNumber("1234")) // 非11位返回原值
    }

    @Test
    fun testContainsChinese() {
        assertTrue(StringUtil.containsChinese("测试"))
        assertTrue(StringUtil.containsChinese("test测试"))
        assertFalse(StringUtil.containsChinese("test123"))
        assertFalse(StringUtil.containsChinese(null))
    }

    @Test
    fun testGetByteLength() {
        assertEquals(4, StringUtil.getByteLength("test"))
        assertEquals(6, StringUtil.getByteLength("测试")) // 中文字符占3个字节
        assertEquals(0, StringUtil.getByteLength(null))
    }

    @Test
    fun testReverse() {
        assertEquals("tset", StringUtil.reverse("test"))
        assertEquals("", StringUtil.reverse(null))
        assertEquals("321", StringUtil.reverse("123"))
    }

    @Test
    fun testCapitalize() {
        assertEquals("Test", StringUtil.capitalize("test"))
        assertEquals("Test", StringUtil.capitalize("Test"))
        assertEquals("", StringUtil.capitalize(null))
        assertEquals("", StringUtil.capitalize(""))
    }
} 