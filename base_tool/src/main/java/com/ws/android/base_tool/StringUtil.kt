package com.ws.android.base_tool

/**
 * 字符串工具类
 */
object StringUtil {
    /**
     * 判断字符串是否为空或空白字符
     * @param str 待检查的字符串
     * @return 是否为空或空白字符
     */
    fun isEmpty(str: String?): Boolean {
        return str == null || str.trim().isEmpty()
    }

    /**
     * 判断字符串是否不为空且不为空白字符
     * @param str 待检查的字符串
     * @return 是否不为空且不为空白字符
     */
    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)

    /**
     * 获取字符串的安全值，为空时返回默认值
     * @param str 原字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    fun getOrDefault(str: String?, defaultValue: String = ""): String {
        return str ?: defaultValue
    }

    /**
     * 截取字符串，超出长度时添加省略号
     * @param str 原字符串
     * @param maxLength 最大长度
     * @param ellipsis 省略号，默认为"..."
     * @return 处理后的字符串
     */
    fun ellipsis(str: String?, maxLength: Int, ellipsis: String = "..."): String {
        if (str == null || str.length <= maxLength) return str ?: ""
        return str.substring(0, maxLength - ellipsis.length) + ellipsis
    }

    /**
     * 移除字符串中的空白字符
     * @param str 原字符串
     * @return 处理后的字符串
     */
    fun removeWhitespace(str: String?): String {
        return str?.replace("\\s+".toRegex(), "") ?: ""
    }

    /**
     * 检查字符串是否是有效的邮箱地址
     * @param email 待检查的邮箱地址
     * @return 是否是有效的邮箱地址
     */
    fun isValidEmail(email: String?): Boolean {
        if (email == null) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    /**
     * 检查字符串是否是有效的手机号（中国大陆）
     * @param phone 待检查的手机号
     * @return 是否是有效的手机号
     */
    fun isValidPhoneNumber(phone: String?): Boolean {
        if (phone == null) return false
        val phoneRegex = "^1[3-9]\\d{9}$"
        return phone.matches(phoneRegex.toRegex())
    }

    /**
     * 格式化手机号为 344 格式
     * @param phone 原手机号
     * @return 格式化后的手机号，如：138 8888 8888
     */
    fun formatPhoneNumber(phone: String?): String {
        if (phone == null || phone.length != 11) return phone ?: ""
        return "${phone.substring(0, 3)} ${phone.substring(3, 7)} ${phone.substring(7)}"
    }

    /**
     * 隐藏手机号中间四位
     * @param phone 原手机号
     * @return 处理后的手机号，如：138****8888
     */
    fun maskPhoneNumber(phone: String?): String {
        if (phone == null || phone.length != 11) return phone ?: ""
        return "${phone.substring(0, 3)}****${phone.substring(7)}"
    }

    /**
     * 检查字符串是否包含中文字符
     * @param str 待检查的字符串
     * @return 是否包含中文字符
     */
    fun containsChinese(str: String?): Boolean {
        if (str == null) return false
        val chineseRegex = "[\u4e00-\u9fa5]"
        return str.contains(chineseRegex.toRegex())
    }

    /**
     * 计算字符串的字节长度（UTF-8编码）
     * @param str 待计算的字符串
     * @return 字节长度
     */
    fun getByteLength(str: String?): Int {
        return str?.toByteArray(Charsets.UTF_8)?.size ?: 0
    }

    /**
     * 反转字符串
     * @param str 待反转的字符串
     * @return 反转后的字符串
     */
    fun reverse(str: String?): String {
        return str?.reversed() ?: ""
    }

    /**
     * 首字母大写
     * @param str 原字符串
     * @return 首字母大写后的字符串
     */
    fun capitalize(str: String?): String {
        if (str.isNullOrEmpty()) return ""
        return str.substring(0, 1).uppercase() + str.substring(1)
    }
}