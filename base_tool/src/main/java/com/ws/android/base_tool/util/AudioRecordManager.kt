package com.ws.android.base_tool.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 录音管理类
 */
class AudioRecordManager private constructor() {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTime: Long = 0
    private var listener: AudioRecordListener? = null
    private var amplitudeTimer: Timer? = null

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AudioRecordManager() }
    }

    /**
     * 录音状态
     */
    sealed class RecordState {
        object Recording : RecordState()
        object Paused : RecordState()
        object Idle : RecordState()
        data class Error(val message: String, val throwable: Throwable? = null) : RecordState()
    }

    /**
     * 录音监听器
     */
    interface AudioRecordListener {
        fun onStateChanged(state: RecordState)
        fun onProgress(duration: Long)
        fun onAmplitude(amplitude: Int)
    }

    /**
     * 设置监听器
     */
    fun setListener(listener: AudioRecordListener) {
        this.listener = listener
    }

    // Add to AudioRecordManager class
    private var isBackgroundRecording = false

    // Method to start background recording
    fun startBackgroundRecording(
        context: Context,
        outputDir: File,
        pendingIntent: PendingIntent? = null,
        config: RecordConfig = RecordConfig()
    ): String? {
        val filePath = startRecording(context, outputDir, config)

        if (filePath != null) {
            isBackgroundRecording = true
            val serviceIntent = Intent(context, AudioRecordService::class.java).apply {
                action = "START_RECORDING"
                if (pendingIntent != null) {
                    putExtra("pending_intent", pendingIntent)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }

        return filePath
    }

    // Method to stop background recording
    fun stopBackgroundRecording(context: Context): String? {
        if (isBackgroundRecording) {
            val serviceIntent = Intent(context, AudioRecordService::class.java).apply {
                action = "STOP_RECORDING"
            }
            context.startService(serviceIntent)
            isBackgroundRecording = false
        }

        return stopRecording()
    }


    /**
     * 开始录音
     * @param context 上下文
     * @param outputDir 输出目录
     * @param config 录音配置
     * @return 录音文件路径，失败返回null
     */
    fun startRecording(
        context: Context,
        outputDir: File,
        config: RecordConfig = RecordConfig()
    ): String? {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            listener?.onStateChanged(RecordState.Error("Failed to create output directory"))
            return null
        }

        try {
            // 生成输出文件
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            currentFile = File(outputDir, "AUDIO_$timeStamp.${config.extension}")

            // 初始化MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(config.audioSource)
                setOutputFormat(config.outputFormat)
                setAudioEncoder(config.audioEncoder)
                setAudioChannels(config.channels)
                setAudioSamplingRate(config.sampleRate)
                setAudioEncodingBitRate(config.bitRate)
                setOutputFile(currentFile!!.absolutePath)
                setMaxDuration(config.maxDuration)
                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        stopRecording()
                    }
                }
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            listener?.onStateChanged(RecordState.Recording)

            // 开始音量监测
            startAmplitudeMonitor()

            return currentFile?.absolutePath
        } catch (e: Exception) {
            listener?.onStateChanged(RecordState.Error("Failed to start recording", e))
            release()
            return null
        }
    }

    /**
     * 停止录音
     * @return 录音文件路径，失败返回null
     */
    fun stopRecording(): String? {
        return try {
            stopAmplitudeMonitor()
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            listener?.onStateChanged(RecordState.Idle)
            currentFile?.absolutePath
        } catch (e: Exception) {
            listener?.onStateChanged(RecordState.Error("Failed to stop recording", e))
            release()
            null
        }
    }

    /**
     * 获取当前录音时长
     * @return 录音时长（毫秒）
     */
    fun getCurrentDuration(): Long {
        return if (startTime > 0) {
            System.currentTimeMillis() - startTime
        } else 0
    }

    /**
     * 开始音量监测
     */
    private fun startAmplitudeMonitor() {
        amplitudeTimer?.cancel()
        amplitudeTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    try {
                        mediaRecorder?.let { recorder ->
                            val amplitude = recorder.maxAmplitude
                            listener?.onAmplitude(amplitude)
                            listener?.onProgress(getCurrentDuration())
                        }
                    } catch (e: Exception) {
                        // 忽略异常，防止崩溃
                    }
                }
            }, 0, 100) // 每100ms更新一次
        }
    }

    /**
     * 停止音量监测
     */
    private fun stopAmplitudeMonitor() {
        amplitudeTimer?.cancel()
        amplitudeTimer = null
    }

    /**
     * 释放资源
     */
    private fun release() {
        stopAmplitudeMonitor()
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // 忽略异常
        }
        mediaRecorder = null
        startTime = 0
        currentFile = null
    }

    /**
     * 录音配置
     */
    data class RecordConfig(
        val audioSource: Int = MediaRecorder.AudioSource.MIC,
        val outputFormat: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaRecorder.OutputFormat.MPEG_4
        } else {
            MediaRecorder.OutputFormat.AAC_ADTS
        },
        val audioEncoder: Int = MediaRecorder.AudioEncoder.AAC,
        val channels: Int = 2,
        val sampleRate: Int = 44100,
        val bitRate: Int = 128000,
        val maxDuration: Int = 60000, // 默认最大录音时长60秒
        val extension: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "m4a" else "aac"
    )
} 