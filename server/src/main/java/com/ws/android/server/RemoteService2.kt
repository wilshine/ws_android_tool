package com.ws.android.server

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log

/**
 * Messenger的使用
 */
class RemoteService2 : Service() {

    private val TAG = "RemoteService2"

    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: android.os.Message) {
            val bundle = msg.data
            val id = bundle.getInt("id")
            Log.i(TAG, "handleMessage: $id")

            val replyMessenger = msg.replyTo
            replyMessenger?.let {
                val msg = Message.obtain()
                val replyBundle = Bundle()
                replyBundle.putString("name", "zhangsan")
                replyBundle.putInt("age", 18)
                msg.data = replyBundle

                try {
                    it.send(msg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        return Messenger(handler).binder
    }

}