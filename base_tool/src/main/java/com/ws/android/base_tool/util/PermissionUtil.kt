package com.ws.android.base_tool.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * 权限管理工具类
 */
object PermissionUtil {
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
     * 在Activity中注册权限请求
     * @param activity AppCompatActivity对象
     * @param onResult 权限请求结果回调
     * @return ActivityResultLauncher对象
     */
    fun registerForActivity(
        activity: AppCompatActivity,
        onResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onResult(permissions)
        }
    }

    /**
     * 在Fragment中注册权限请求
     * @param fragment Fragment对象
     * @param onResult 权限请求结果回调
     * @return ActivityResultLauncher对象
     */
    fun registerForFragment(
        fragment: Fragment,
        onResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onResult(permissions)
        }
    }

    /**
     * 请求权限
     * @param launcher ActivityResultLauncher对象
     * @param permissions 要请求的权限列表
     */
    fun request(launcher: ActivityResultLauncher<Array<String>>, vararg permissions: String) {
//        val permissionArray: Array<String> = Array(permissions.size) { i -> permissions[i] }
//        launcher.launch(permissionArray)
        launcher.launch(permissions.toList().toTypedArray())

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