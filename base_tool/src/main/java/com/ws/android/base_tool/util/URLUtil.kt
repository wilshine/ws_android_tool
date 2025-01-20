package com.ws.android.base_tool.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import java.net.MalformedURLException
import java.net.URL

/**
 * URL工具类
 */
object URLUtil {
    /**
     * 检查URL是否有效
     * @param url 待检查的URL
     * @return 是否是有效的URL
     */
    fun isValid(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return try {
            URL(url)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    /**
     * 检查是否是HTTP或HTTPS URL
     * @param url 待检查的URL
     * @return 是否是HTTP或HTTPS URL
     */
    fun isHttpUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)
    }

    /**
     * 使用外部浏览器打开URL
     * @param context Context对象
     * @param url 要打开的URL
     * @return 是否成功打开
     */
    fun openInBrowser(context: Context, url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取URL的域名
     * @param url URL字符串
     * @return 域名，如果解析失败返回null
     */
    fun getDomain(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            URL(url).host
        } catch (e: MalformedURLException) {
            null
        }
    }

    /**
     * 获取URL的协议
     * @param url URL字符串
     * @return 协议名称，如果解析失败返回null
     */
    fun getProtocol(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            URL(url).protocol
        } catch (e: MalformedURLException) {
            null
        }
    }

    /**
     * 获取URL的路径
     * @param url URL字符串
     * @return 路径，如果解析失败返回null
     */
    fun getPath(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            URL(url).path
        } catch (e: MalformedURLException) {
            null
        }
    }

    /**
     * 获取URL的查询参数
     * @param url URL字符串
     * @return 查询参数，如果解析失败返回null
     */
    fun getQuery(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            URL(url).query
        } catch (e: MalformedURLException) {
            null
        }
    }

    /**
     * 解析URL的查询参数为Map
     * @param url URL字符串
     * @return 参数Map，如果解析失败返回空Map
     */
    fun parseQueryParameters(url: String?): Map<String, String> {
        val query = getQuery(url) ?: return emptyMap()
        return query.split("&")
            .map { it.split("=", limit = 2) }
            .filter { it.size == 2 }
            .associate { it[0] to it[1] }
    }

    /**
     * 添加查询参数到URL
     * @param url 原URL
     * @param params 要添加的参数
     * @return 新的URL字符串
     */
    fun addQueryParameters(url: String?, params: Map<String, String>): String {
        if (url.isNullOrBlank() || params.isEmpty()) return url ?: ""
        
        val baseUrl = if (url.contains("?")) {
            if (url.endsWith("?")) url else "$url&"
        } else {
            "$url?"
        }
        
        val queryString = params.entries.joinToString("&") { 
            "${it.key}=${Uri.encode(it.value)}" 
        }
        
        return baseUrl + queryString
    }

    /**
     * 规范化URL（添加协议头如果缺少）
     * @param url 原URL
     * @param defaultProtocol 默认协议，默认为"https"
     * @return 规范化后的URL
     */
    fun normalize(url: String?, defaultProtocol: String = "https"): String {
        if (url.isNullOrBlank()) return ""
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "$defaultProtocol://$url"
        } else url
    }

    /**
     * 检查URL是否是图片URL
     * @param url 待检查的URL
     * @return 是否是图片URL
     */
    fun isImageUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
        return imageExtensions.any { url.lowercase().endsWith(it) }
    }
} 