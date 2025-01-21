package com.ws.android.tool

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import com.ws.android.base_tool.util.DeviceUtil
import com.ws.android.base_tool.util.PermissionUtil
import com.ws.android.tool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化权限管理器
        PermissionUtil.init(this)

        setupClickListeners()
        updatePermissionStatus()

        testDeviceUtil()
    }

    fun testDeviceUtil() {

        if(!PermissionUtil.isGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionUtil.request(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), object : PermissionUtil.PermissionCallback {
                override fun onGranted(permissions: List<String>) {
                    testDeviceUtil()
                }

                override fun onDenied(permissions: List<String>) {
                    showToast("未授予存储权限")
                }

                override fun onPermanentlyDenied(permissions: List<String>) {
                    showToast("存储权限被永久拒绝")
                }
            })
            return
        }

        when (val result = DeviceUtil.getStorageInfo(baseContext)) {
            is DeviceUtil.Result.Success -> {
                // 获取内置存储信息
                result.data[DeviceUtil.StorageType.INTERNAL]?.let { internal ->
                    val totalGB = internal.totalSpace.bytesToGB()
                    val availableGB = internal.availableSpace.bytesToGB()
                    Log.d("Storage", "Internal Storage: Total ${totalGB}GB, Available ${availableGB}GB")
                }

                // 获取外置存储信息
                result.data[DeviceUtil.StorageType.EXTERNAL]?.let { external ->
                    val totalGB = external.totalSpace.bytesToGB()
                    val availableGB = external.availableSpace.bytesToGB()
                    Log.d("Storage", "External Storage: Total ${totalGB}GB, Available ${availableGB}GB")
                }

                // 获取应用数据存储信息
                result.data[DeviceUtil.StorageType.DATA]?.let { data ->
                    val totalMB = data.totalSpace.bytesToMB()
                    val availableMB = data.availableSpace.bytesToMB()
                    Log.d("Storage", "App Data Storage: Total ${totalMB}MB, Available ${availableMB}MB")
                }
            }
            is DeviceUtil.Result.Error -> {
                Log.e("Storage", "Error: ${result.message}", result.cause)
            }
        }

        // 字节转换扩展函数
        fun Long.bytesToMB() = this / (1024 * 1024)
        fun Long.bytesToGB() = this / (1024 * 1024 * 1024)
    }

    private fun setupClickListeners() {
        binding.apply {
            btnRequestCamera.setOnClickListener {
                requestPermission(Manifest.permission.CAMERA)
            }

            btnRequestStorage.setOnClickListener {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            btnRequestLocation.setOnClickListener {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            btnRequestMultiple.setOnClickListener {
                requestMultiplePermissions()
            }

            btnOpenSettings.setOnClickListener {
                PermissionUtil.openSettings(this@MainActivity)
            }
        }
    }

    private fun requestPermission(permission: String) {
        PermissionUtil.request(
            activity = this,
            permissions = arrayOf(permission),
            callback = object : PermissionUtil.PermissionCallback {
                override fun onGranted(permissions: List<String>) {
                    showToast("${permissions.first()} 权限已授予")
                    updatePermissionStatus()
                }

                override fun onDenied(permissions: List<String>) {
                    permissions.forEach { showPermissionRationale(it) }
                    updatePermissionStatus()
                }

                override fun onPermanentlyDenied(permissions: List<String>) {
                    showSettingsDialog()
                    updatePermissionStatus()
                }
            }
        )
    }

    private fun requestMultiplePermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        PermissionUtil.request(
            activity = this,
            permissions = permissions,
            callback = object : PermissionUtil.PermissionCallback {
                override fun onGranted(permissions: List<String>) {
                    showToast("已获得所有权限")
                    updatePermissionStatus()
                }

                override fun onDenied(permissions: List<String>) {
                    showPermissionRationale(permissions.toTypedArray())
                    updatePermissionStatus()
                }

                override fun onPermanentlyDenied(permissions: List<String>) {
                    showSettingsDialog()
                    updatePermissionStatus()
                }
            }
        )
    }

    private fun showPermissionRationale(permission: String) {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("此功能需要 $permission 权限才能正常使用")
            .setPositiveButton("确定") { _, _ ->
                requestPermission(permission)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showPermissionRationale(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("此功能需要以下权限才能正常使用：\n${permissions.joinToString("\n")}")
            .setPositiveButton("确定") { _, _ ->
                requestMultiplePermissions()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("请在设置中开启所需权限")
            .setPositiveButton("去设置") { _, _ ->
                PermissionUtil.openSettings(this)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updatePermissionStatus() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val status = buildString {
            append("权限状态：\n")
            permissions.forEach { permission ->
                append(permission.substringAfterLast("."))
                append(": ")
                append(
                    when {
                        PermissionUtil.isGranted(this@MainActivity, permission) -> "已授予"
                        PermissionUtil.isPermanentlyDenied(this@MainActivity, permission) -> "永久拒绝"
                        else -> "未授予"
                    }
                )
                append("\n")
            }
        }

        binding.tvPermissionStatus.text = status
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

private fun Long.bytesToMB(): Any {
    return this / (1024 * 1024)
}

private fun Long.bytesToGB(): Any {
    return this / (1024 * 1024 * 1024)
}
