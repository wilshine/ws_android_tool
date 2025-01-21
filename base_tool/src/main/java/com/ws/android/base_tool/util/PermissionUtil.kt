package com.ws.android.base_tool.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity

/**
 * 权限管理工具类
 */
object PermissionUtil {
    private const val PERMISSION_REQUEST_CODE = 100
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var permissionCallback: PermissionCallback? = null

    /**
     * 权限请求回调接口
     */
    interface PermissionCallback {
        fun onGranted(permissions: List<String>)
        fun onDenied(permissions: List<String>)
        fun onPermanentlyDenied(permissions: List<String>)
    }

    /**
     * 初始化权限管理器（在Activity的onCreate中调用）
     * @param activity AppCompatActivity对象
     */
    fun init(activity: AppCompatActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            handlePermissionResult(activity, results)
        }
    }

    /**
     * 请求权限
     * @param activity Activity对象
     * @param permissions 要请求的权限列表
     * @param callback 权限请求回调
     */
    fun request(
        activity: Activity,
        permissions: Array<String>,
        callback: PermissionCallback
    ) {
        permissionCallback = callback
        when {
            areGranted(activity, permissions) -> {
                callback.onGranted(permissions.toList())
            }
            shouldShowAnyRationale(activity, permissions) -> {
                callback.onDenied(permissions.filter { shouldShowRationale(activity, it) })
            }
            else -> {
                permissionLauncher?.launch(permissions) ?: throw IllegalStateException(
                    "PermissionUtil not initialized. Call init() first in your Activity's onCreate."
                )
            }
        }
    }

    private fun handlePermissionResult(
        activity: Activity,
        results: Map<String, Boolean>
    ) {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        val permanentlyDenied = mutableListOf<String>()

        results.forEach { (permission, isGranted) ->
            when {
                isGranted -> granted.add(permission)
                isPermanentlyDenied(activity, permission) -> permanentlyDenied.add(permission)
                else -> denied.add(permission)
            }
        }

        permissionCallback?.let {
            if (granted.isNotEmpty()) it.onGranted(granted)
            if (denied.isNotEmpty()) it.onDenied(denied)
            if (permanentlyDenied.isNotEmpty()) it.onPermanentlyDenied(permanentlyDenied)
        }
    }

    /**
     * 检查是否已经授予权限
     * @param context Context对象
     * @param permission 权限名称
     * @return 是否已授予权限
     */
    fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查是否已经授予多个权限
     * @param context Context对象
     * @param permissions 权限列表
     * @return 是否所有权限都已授予
     */
    fun areGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { isGranted(context, it) }
    }

    /**
     * 检查权限是否被永久拒绝
     * @param activity Activity对象
     * @param permission 权限名称
     * @return 是否被永久拒绝
     */
    fun isPermanentlyDenied(activity: Activity, permission: String): Boolean {
        return !isGranted(activity, permission) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 打开应用设置页面
     * @param context Context对象
     * @return 是否成功打开设置页面
     */
    fun openSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查是否需要显示权限说明
     * @param activity Activity对象
     * @param permission 权限名称
     * @return 是否需要显示权限说明
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 检查是否需要显示任一权限的说明
     * @param activity Activity对象
     * @param permissions 权限列表
     * @return 是否需要显示权限说明
     */
    fun shouldShowAnyRationale(activity: Activity, permissions: Array<String>): Boolean {
        return permissions.any { shouldShowRationale(activity, it) }
    }

    /**
     * 获取未授予的权限列表
     * @param context Context对象
     * @param permissions 要检查的权限列表
     * @return 未授予的权限列表
     */
    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { !isGranted(context, it) }
    }

    /**
     * 获取被永久拒绝的权限列表
     * @param activity Activity对象
     * @param permissions 要检查的权限列表
     * @return 被永久拒绝的权限列表
     */
    fun getPermanentlyDeniedPermissions(activity: Activity, permissions: Array<String>): List<String> {
        return permissions.filter { isPermanentlyDenied(activity, it) }
    }
} 