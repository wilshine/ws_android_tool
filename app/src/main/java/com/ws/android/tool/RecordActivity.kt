package com.ws.android.tool

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ws.android.base_tool.util.AudioRecordManager
import com.ws.android.base_tool.util.PermissionUtil
import com.ws.android.tool.databinding.ActivityMainBinding
import com.ws.android.tool.databinding.ActivityRecordBinding

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }


    private var currentAudioPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        // 设置录音监听器
        AudioRecordManager.instance.setListener(object : AudioRecordManager.AudioRecordListener {
            override fun onStateChanged(state: AudioRecordManager.RecordState) {
                when (state) {
                    is AudioRecordManager.RecordState.Recording -> {
                        // 更新UI为录音状态
                        updateRecordingUI()
                    }

                    is AudioRecordManager.RecordState.Idle -> {
                        // 更新UI为空闲状态
                        updateIdleUI()
                    }

                    is AudioRecordManager.RecordState.Error -> {
                        // 显示错误信息
                        showError(state.message)
                    }

                    AudioRecordManager.RecordState.Paused -> {

                    }
                }
            }

            override fun onProgress(duration: Long) {
                // 更新录音时长
                updateDuration(duration)
            }

            override fun onAmplitude(amplitude: Int) {
                // 更新音量指示器
//                updateVolumeIndicator(amplitude)
            }
        })

        // 开始录音
        binding.btnStartRecord.setOnClickListener {
            checkPermissions { granted ->
                if (!granted) {
                    return@checkPermissions
                }
                val outputDir = getExternalFilesDir("audio")
                currentAudioPath = AudioRecordManager.instance.startRecording(
                    context = this,
                    outputDir = outputDir!!,
                    config = AudioRecordManager.RecordConfig(
                        maxDuration = 30000 // 设置最大录音时长为30秒
                    )
                )
            }
        }

        // 停止录音
        binding.btnStopRecord.setOnClickListener {
            currentAudioPath = AudioRecordManager.instance.stopRecording()
        }
    }

    private fun updateDuration(duration: Long) {
        binding.tvRecordDuration.text = "录音时长：${duration / 1000}秒"
    }

    private fun showError(message: String) {
        binding.tvRecordStatus.text = message
    }

    private fun updateIdleUI() {
        binding.tvRecordStatus.text = "点击开始录音"
    }

    private fun updateRecordingUI() {
        binding.tvRecordStatus.text = "正在录音"
    }

    private fun checkPermissions(callback: (Boolean) -> Unit): Unit {


        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.RECORD_AUDIO)
        } else {
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }


        if (!PermissionUtil.areGranted(this, permissions)) {
            PermissionUtil.request(this, permissions, object : PermissionUtil.PermissionCallback {
                override fun onGranted(permissions: List<String>) {
                    Log.d("Permission", "onGranted: $permissions")
                    callback.invoke(true)
                }

                override fun onDenied(permissions: List<String>) {
                    Log.d("Permission", "onDenied: $permissions")
                    callback.invoke(false)
                }

                override fun onPermanentlyDenied(permissions: List<String>) {
                    Log.d("Permission", "onPermanentlyDenied: $permissions")
                    callback.invoke(false)
                }
            })
        }
        callback.invoke(true)
    }
}