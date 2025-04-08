package com.ws.android.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * AIDL方法的具体实现
 */
class RemoteService : Service() {
    private val TAG = "RemoteService"

    // 创建Binder对象实现AIDL接口
    private val binder = object : IRemoteService.Stub() {
        override fun add(a: Int, b: Int): Int {
            Log.d(TAG, "add: $a + $b")
            return a + b
        }

        override fun getMessage(): String {
            return "Hello from RemoteService!"
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RemoteService Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "RemoteService Destroyed")
    }
}