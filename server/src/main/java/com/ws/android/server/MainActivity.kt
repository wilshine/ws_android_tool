package com.ws.android.server

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ws.android.server.ui.theme.Ws_android_toolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ws_android_toolTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = "Server",
                            modifier = Modifier.padding(innerPadding)
                        )
                        Button({
                            randomStudent()
                        }) {
                            Text("Random Student")
                        }
                    }
                }
            }
        }

        // 绑定服务
        bindRemoteService()
    }

    /**
     * 触发客户端回调
     */
    private fun randomStudent() {
        remoteService?.changeScore()
    }


    private var remoteService: IRemoteService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            remoteService = IRemoteService.Stub.asInterface(service)
            isBound = true
            Log.d("MainActivity", "RemoteService connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remoteService = null
            isBound = false
            Log.d("MainActivity", "RemoteService disconnected")
        }
    }


    private fun bindRemoteService() {
        Intent(this, RemoteService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Ws_android_toolTheme {
        Greeting("Android")
    }
}