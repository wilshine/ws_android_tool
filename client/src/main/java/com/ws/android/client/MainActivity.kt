package com.ws.android.client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ws.android.server.IRemoteService
import com.ws.android.server.model.Student

class MainActivity : AppCompatActivity() {
    private val TAG = "ClientMainActivity"
    private var remoteService: IRemoteService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            remoteService = IRemoteService.Stub.asInterface(service)
            isBound = true
            Log.d(TAG, "Service Connected")
            updateServiceStatus("服务已连接")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remoteService = null
            isBound = false
            Log.d(TAG, "Service Disconnected")
            updateServiceStatus("服务已断开")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBind = findViewById<Button>(R.id.btnBind)
        val btnUnbind = findViewById<Button>(R.id.btnUnbind)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnGetMessage = findViewById<Button>(R.id.btnGetMessage)

        btnBind.setOnClickListener {
            bindRemoteService()
        }

        btnUnbind.setOnClickListener {
            unbindRemoteService()
        }

        findViewById<Button>(R.id.btnGetStudent).setOnClickListener {
            try {
                val student = Student().apply {
                    name = "张三"
                    age = 10
                    grade = "Grade 5"
                }
                val updatedStudent = remoteService?.getStudentInfo(12, student)
                updatedStudent?.let {
                    Toast.makeText(
                        this, "更新后的学生信息：${student.name}, ${student.age}岁, ${student.grade}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        btnAdd.setOnClickListener {
            if (isBound) {
                try {
                    val result = remoteService?.add(10, 20)
                    updateResult("10 + 20 = $result")
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling remote add", e)
                    updateResult("调用远程服务失败：${e.message}")
                }
            } else {
                updateResult("服务未连接")
            }
        }

        btnGetMessage.setOnClickListener {
            if (isBound) {
                try {
                    val message = remoteService?.message
                    updateResult("服务端消息：$message")
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting remote message", e)
                    updateResult("获取消息失败：${e.message}")
                }
            } else {
                updateResult("服务未连接")
            }
        }
    }

    private fun bindRemoteService() {
        Intent().also { intent ->
            intent.action = "com.ws.android.server.IRemoteService"
            intent.setPackage("com.ws.android.server")
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    private fun unbindRemoteService() {
        if (isBound) {
            unbindService(connection)
            isBound = false
            updateServiceStatus("服务已断开")
        }
    }

    private fun updateServiceStatus(status: String) {
        findViewById<TextView>(R.id.tvStatus).text = status
    }

    private fun updateResult(result: String) {
        findViewById<TextView>(R.id.tvResult).text = result
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindRemoteService()
    }
}