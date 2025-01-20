package com.ws.android.tool

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ws.android.base_tool.util.PermissionUtil
import com.ws.android.tool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 注册权限请求
        permissionLauncher = PermissionUtil.registerForActivity(this) { permissions ->
            updatePermissionStatus()
            permissions.forEach { (permission, isGranted) ->
                when {
                    isGranted -> {
                        showToast("$permission 权限已授予")
                    }
                    PermissionUtil.shouldShowRationale(this, permission) -> {
                        showPermissionRationale(permission)
                    }
                    else -> {
                        showSettingsDialog()
                    }
                }
            }
        }

        setupClickListeners()
        updatePermissionStatus()
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
        when {
            PermissionUtil.isGranted(this, permission) -> {
                showToast("已有 $permission 权限")
            }
            PermissionUtil.shouldShowRationale(this, permission) -> {
                showPermissionRationale(permission)
            }
            else -> {
                PermissionUtil.request(permissionLauncher, permission)
            }
        }
    }

    private fun requestMultiplePermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        when {
            PermissionUtil.areGranted(this, permissions) -> {
                showToast("已有所有权限")
            }
            PermissionUtil.shouldShowAnyRationale(this, permissions) -> {
                showPermissionRationale(permissions)
            }
            else -> {
                PermissionUtil.request(permissionLauncher, *permissions)
            }
        }
    }

    private fun showPermissionRationale(permission: String) {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("此功能需要 $permission 权限才能正常使用")
            .setPositiveButton("确定") { _, _ ->
                PermissionUtil.request(permissionLauncher, permission)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showPermissionRationale(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("此功能需要以下权限才能正常使用：\n${permissions.joinToString("\n")}")
            .setPositiveButton("确定") { _, _ ->
                PermissionUtil.request(permissionLauncher, *permissions)
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