package com.ws.android.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ws.android.server.model.Student

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

        override fun getStudentInfo(age: Int, student: Student): Student {
            // 处理接收到的Student对象
            Log.d(TAG, "Received student: name=${student.name}, age=${student.age}, grade=${student.grade}")
            // 修改学生信息
            student.age = age
            student.grade = "Grade ${age/6 + 1}"
            Log.d(TAG, "Updated student: name=${student.name}, age=${student.age}, grade=${student.grade}")
            return student
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