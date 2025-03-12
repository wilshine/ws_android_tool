package com.ws.android.base_tool.extension


fun Int.bytesToMB(): Int {
    return this / (1024 * 1024)
}

fun Int.bytesToGB(): Int {
    return this / (1024 * 1024 * 1024)
}
