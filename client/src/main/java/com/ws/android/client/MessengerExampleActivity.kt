package com.ws.android.client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Messenger示例
 * 发送消息到服务端，服务端返回消息到客户端
 */
class MessengerExampleActivity : AppCompatActivity() {

    private val TAG = "MessengerExampleActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_messenger_example)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindRemoteService()

    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val name = msg.data.getString("name")
            val age = msg.data.getInt("age")
            Log.i(TAG, "Received from service: name=$name, age=$age")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 连接成功
            val messenger = Messenger(service)
            val message = Message.obtain()
            val bundle = Bundle()
            bundle.putInt("id", 1000)
            message.data = bundle
            message.replyTo = Messenger(handler) // 设置handler，接收服务端发送的消息
            try {
                messenger.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // 连接断开
            Log.w(TAG, "Service disconnected")
        }
    }

    private fun bindRemoteService() {
        Intent().also { intent ->
            intent.action = "com.ws.android.server.REMOTE_SERVICE2_ACTION"
            intent.setPackage("com.ws.android.server")
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }
}