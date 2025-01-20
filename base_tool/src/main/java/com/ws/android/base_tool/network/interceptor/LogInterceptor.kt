package com.ws.android.base_tool.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LogInterceptor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d("LogInterceptor", "request: ${request.url}")
        val response = chain.proceed(request)
        return response
    }
}