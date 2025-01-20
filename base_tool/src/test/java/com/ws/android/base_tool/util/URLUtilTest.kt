package com.ws.android.base_tool.util

import org.junit.Assert.*
import org.junit.Test

class URLUtilTest {

    @Test
    fun testIsValid() {
        assertTrue(URLUtil.isValid("https://www.example.com"))
        assertTrue(URLUtil.isValid("http://localhost:8080"))
        assertFalse(URLUtil.isValid("not a url"))
        assertFalse(URLUtil.isValid(""))
        assertFalse(URLUtil.isValid(null))
    }

    @Test
    fun testIsHttpUrl() {
        assertTrue(URLUtil.isHttpUrl("http://example.com"))
        assertTrue(URLUtil.isHttpUrl("https://example.com"))
        assertFalse(URLUtil.isHttpUrl("ftp://example.com"))
        assertFalse(URLUtil.isHttpUrl(""))
        assertFalse(URLUtil.isHttpUrl(null))
    }

    @Test
    fun testGetDomain() {
        assertEquals("example.com", URLUtil.getDomain("https://example.com/path"))
        assertEquals("localhost", URLUtil.getDomain("http://localhost:8080"))
        assertNull(URLUtil.getDomain("not a url"))
        assertNull(URLUtil.getDomain(""))
        assertNull(URLUtil.getDomain(null))
    }

    @Test
    fun testGetProtocol() {
        assertEquals("https", URLUtil.getProtocol("https://example.com"))
        assertEquals("http", URLUtil.getProtocol("http://example.com"))
        assertNull(URLUtil.getProtocol("not a url"))
        assertNull(URLUtil.getProtocol(""))
        assertNull(URLUtil.getProtocol(null))
    }

    @Test
    fun testGetPath() {
        assertEquals("/path", URLUtil.getPath("https://example.com/path"))
        assertEquals("/path/to/resource", URLUtil.getPath("https://example.com/path/to/resource"))
        assertEquals("", URLUtil.getPath("https://example.com"))
        assertNull(URLUtil.getPath("not a url"))
        assertNull(URLUtil.getPath(null))
    }

    @Test
    fun testGetQuery() {
        assertEquals("key=value", URLUtil.getQuery("https://example.com?key=value"))
        assertEquals("key1=value1&key2=value2", 
            URLUtil.getQuery("https://example.com?key1=value1&key2=value2"))
        assertNull(URLUtil.getQuery("https://example.com"))
        assertNull(URLUtil.getQuery("not a url"))
        assertNull(URLUtil.getQuery(null))
    }

    @Test
    fun testParseQueryParameters() {
        val params = URLUtil.parseQueryParameters("https://example.com?key1=value1&key2=value2")
        assertEquals("value1", params["key1"])
        assertEquals("value2", params["key2"])
        assertTrue(URLUtil.parseQueryParameters("https://example.com").isEmpty())
        assertTrue(URLUtil.parseQueryParameters(null).isEmpty())
    }

    @Test
    fun testAddQueryParameters() {
        val params = mapOf("key1" to "value1", "key2" to "value2")
        assertEquals(
            "https://example.com?key1=value1&key2=value2",
            URLUtil.addQueryParameters("https://example.com", params)
        )
        assertEquals(
            "https://example.com?existing=value&key1=value1&key2=value2",
            URLUtil.addQueryParameters("https://example.com?existing=value", params)
        )
        assertEquals("", URLUtil.addQueryParameters(null, params))
    }

    @Test
    fun testNormalize() {
        assertEquals("https://example.com", URLUtil.normalize("example.com"))
        assertEquals("http://example.com", URLUtil.normalize("http://example.com"))
        assertEquals("https://example.com", URLUtil.normalize("https://example.com"))
        assertEquals("", URLUtil.normalize(null))
    }

    @Test
    fun testIsImageUrl() {
        assertTrue(URLUtil.isImageUrl("https://example.com/image.jpg"))
        assertTrue(URLUtil.isImageUrl("https://example.com/image.png"))
        assertTrue(URLUtil.isImageUrl("https://example.com/image.gif"))
        assertFalse(URLUtil.isImageUrl("https://example.com/document.pdf"))
        assertFalse(URLUtil.isImageUrl(""))
        assertFalse(URLUtil.isImageUrl(null))
    }
} 