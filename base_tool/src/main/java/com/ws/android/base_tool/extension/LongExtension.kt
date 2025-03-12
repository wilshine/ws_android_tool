package com.ws.android.base_tool.extension


fun Long.bytesToMB(): Long {
    return this / (1024 * 1024)
}

fun Long.bytesToGB(): Long {
    return this / (1024 * 1024 * 1024)
}
