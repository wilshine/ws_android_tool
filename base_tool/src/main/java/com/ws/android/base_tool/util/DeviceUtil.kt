package com.ws.android.base_tool.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.File
import java.net.NetworkInterface
import java.util.*

/**
 * 设备工具类，提供设备相关的实用方法
 */
object DeviceUtil {
    
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    }

    /**
     * 设备网络类型
     */
    enum class NetworkType {
        WIFI,
        MOBILE_5G,
        MOBILE_4G,
        MOBILE_3G,
        MOBILE_2G,
        ETHERNET,
        UNKNOWN
    }

    /**
     * 存储类型
     */
    enum class StorageType {
        INTERNAL,    // 内置存储
        EXTERNAL,    // 外置存储（SD卡）
        DATA,        // 应用数据存储
        CACHE       // 应用缓存存储
    }

    /**
     * 存储信息数据类
     */
    data class StorageInfo(
        val type: StorageType,
        val totalSpace: Long,
        val availableSpace: Long,
        val freeSpace: Long,
        val path: String
    )

    /**
     * 获取设备基本信息
     * @return 设备基本信息的Map
     */
    fun getDeviceInfo(): Map<String, String> = mapOf(
        "manufacturer" to Build.MANUFACTURER,
        "model" to Build.MODEL,
        "androidVersion" to Build.VERSION.RELEASE,
        "sdkVersion" to Build.VERSION.SDK_INT.toString(),
        "brand" to Build.BRAND,
        "device" to Build.DEVICE,
        "product" to Build.PRODUCT
    )

    /**
     * 获取设备唯一标识符
     * 注意：此标识符在设备恢复出厂设置后会改变
     * @param context Context
     * @return Result 包含设备ID或错误信息
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): Result<String> {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            if (androidId == "9774d56d682e549c" || androidId.isEmpty()) {
                Result.Error("Invalid Android ID")
            } else {
                Result.Success(androidId)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get device ID", e)
        }
    }

    /**
     * 获取设备MAC地址
     * 需要 ACCESS_NETWORK_STATE 权限
     * @return Result 包含MAC地址或错误信息
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun getMacAddress(): Result<String> {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces() ?: 
                return Result.Error("No network interfaces found")

            for (networkInterface in networkInterfaces) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
                    val macBytes = networkInterface.hardwareAddress ?: 
                        continue
                    
                    val macAddress = macBytes.joinToString(":") { 
                        String.format("%02X", it) 
                    }
                    
                    if (macAddress.isNotEmpty() && macAddress != "02:00:00:00:00:00") {
                        return Result.Success(macAddress)
                    }
                }
            }
            Result.Error("MAC address not found")
        } catch (e: Exception) {
            Result.Error("Failed to get MAC address", e)
        }
    }

    /**
     * 检查设备是否root
     * @return Result 包含root状态或错误信息
     */
    fun isDeviceRooted(): Result<Boolean> {
        return try {
            val rootPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            
            val testCommands = arrayOf("which su", "/system/xbin/which su", "type su")
            
            val hasRootPath = rootPaths.any { File(it).exists() }
            var hasRootCommand = false
            
            try {
                for (command in testCommands) {
                    if (Runtime.getRuntime().exec(command).waitFor() == 0) {
                        hasRootCommand = true
                        break
                    }
                }
            } catch (e: Exception) {
                // 忽略命令执行异常
            }
            
            Result.Success(hasRootPath || hasRootCommand)
        } catch (e: Exception) {
            Result.Error("Failed to check root status", e)
        }
    }

    /**
     * 检查是否为模拟器
     * @return Result 包含模拟器状态或错误信息
     */
    fun isEmulator(): Result<Boolean> {
        return try {
            val checkPoints = listOf(
                Build.FINGERPRINT.startsWith("generic"),
                Build.FINGERPRINT.startsWith("unknown"),
                Build.MODEL.contains("google_sdk"),
                Build.MODEL.contains("Emulator"),
                Build.MODEL.contains("Android SDK built for x86"),
                Build.MANUFACTURER.contains("Genymotion"),
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"),
                "google_sdk" == Build.PRODUCT,
                Build.HARDWARE.contains("goldfish"),
                Build.HARDWARE.contains("ranchu"),
                Build.BOARD.toLowerCase(Locale.ROOT).contains("nox"),
                Build.BOOTLOADER.toLowerCase(Locale.ROOT).contains("nox"),
                Build.HARDWARE.toLowerCase(Locale.ROOT).contains("nox"),
                Build.PRODUCT.toLowerCase(Locale.ROOT).contains("nox"),
                Build.SERIAL.toLowerCase(Locale.ROOT).contains("nox")
            )
            
            Result.Success(checkPoints.any { it })
        } catch (e: Exception) {
            Result.Error("Failed to check emulator status", e)
        }
    }

    /**
     * 获取内存信息
     * @param context Context
     * @return Result 包含内存信息或错误信息
     */
    fun getMemoryInfo(context: Context): Result<Map<String, Long>> {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            Result.Success(mapOf(
                "availableMemory" to memoryInfo.availMem,
                "totalMemory" to memoryInfo.totalMem,
                "threshold" to memoryInfo.threshold,
                "lowMemory" to if (memoryInfo.lowMemory) 1L else 0L
            ))
        } catch (e: Exception) {
            Result.Error("Failed to get memory info", e)
        }
    }

    /**
     * 获取存储信息
     * 需要 READ_EXTERNAL_STORAGE 权限来获取外置存储信息
     * @param context Context
     * @return Result 包含所有存储信息或错误信息
     */
//    @RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getStorageInfo(context: Context): Result<Map<StorageType, StorageInfo>> {
        return try {
            val storageMap = mutableMapOf<StorageType, StorageInfo>()

            // 获取内置存储信息
            getInternalStorageInfo()?.let { 
                storageMap[StorageType.INTERNAL] = it 
            }

            // 获取外置存储信息
            getExternalStorageInfo()?.let { 
                storageMap[StorageType.EXTERNAL] = it 
            }

            // 获取应用数据存储信息
            getAppDataStorageInfo(context)?.let { 
                storageMap[StorageType.DATA] = it 
            }

            // 获取应用缓存存储信息
            getAppCacheStorageInfo(context)?.let { 
                storageMap[StorageType.CACHE] = it 
            }

            if (storageMap.isEmpty()) {
                Result.Error("No storage information available")
            } else {
                Result.Success(storageMap)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get storage info", e)
        }
    }

    /**
     * 获取内置存储信息
     */
    private fun getInternalStorageInfo(): StorageInfo? {
        return try {
            val internalFile = Environment.getDataDirectory()
            val stat = StatFs(internalFile.path)
            StorageInfo(
                type = StorageType.INTERNAL,
                totalSpace = stat.totalBytes,
                availableSpace = stat.availableBytes,
                freeSpace = stat.freeBytes,
                path = internalFile.absolutePath
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取外置存储信息
     */
    private fun getExternalStorageInfo(): StorageInfo? {
        return try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return null
            }

            val externalFile = Environment.getExternalStorageDirectory()
            val stat = StatFs(externalFile.path)
            StorageInfo(
                type = StorageType.EXTERNAL,
                totalSpace = stat.totalBytes,
                availableSpace = stat.availableBytes,
                freeSpace = stat.freeBytes,
                path = externalFile.absolutePath
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取应用数据存储信息
     */
    private fun getAppDataStorageInfo(context: Context): StorageInfo? {
        return try {
            val dataFile = context.dataDir
            val stat = StatFs(dataFile.path)
            StorageInfo(
                type = StorageType.DATA,
                totalSpace = stat.totalBytes,
                availableSpace = stat.availableBytes,
                freeSpace = stat.freeBytes,
                path = dataFile.absolutePath
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取应用缓存存储信息
     */
    private fun getAppCacheStorageInfo(context: Context): StorageInfo? {
        return try {
            val cacheFile = context.cacheDir
            val stat = StatFs(cacheFile.path)
            StorageInfo(
                type = StorageType.CACHE,
                totalSpace = stat.totalBytes,
                availableSpace = stat.availableBytes,
                freeSpace = stat.freeBytes,
                path = cacheFile.absolutePath
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * StatFs 的扩展属性
     */
    private val StatFs.totalBytes: Long
        get() = blockCountLong * blockSizeLong

    private val StatFs.availableBytes: Long
        get() = availableBlocksLong * blockSizeLong

    private val StatFs.freeBytes: Long
        get() = freeBlocksLong * blockSizeLong

    /**
     * 获取网络状态
     * 需要 ACCESS_NETWORK_STATE 权限
     * @param context Context
     * @return Result 包含网络状态或错误信息
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkInfo(context: Context): Result<Map<String, Any>> {
        return try {
            val connectivityManager = 
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val capabilities = network?.let { 
                    connectivityManager.getNetworkCapabilities(it) 
                }
                
                if (capabilities == null) {
                    return Result.Success(mapOf(
                        "connected" to false,
                        "type" to NetworkType.UNKNOWN
                    ))
                }

                val networkType = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 
                        NetworkType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        val telephonyManager = 
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        when (telephonyManager.dataNetworkType) {
                            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.MOBILE_5G
                            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.MOBILE_4G
                            TelephonyManager.NETWORK_TYPE_UMTS,
                            TelephonyManager.NETWORK_TYPE_EVDO_0,
                            TelephonyManager.NETWORK_TYPE_EVDO_A,
                            TelephonyManager.NETWORK_TYPE_HSDPA,
                            TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA,
                            TelephonyManager.NETWORK_TYPE_EVDO_B,
                            TelephonyManager.NETWORK_TYPE_EHRPD,
                            TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.MOBILE_3G
                            TelephonyManager.NETWORK_TYPE_GPRS,
                            TelephonyManager.NETWORK_TYPE_EDGE,
                            TelephonyManager.NETWORK_TYPE_CDMA,
                            TelephonyManager.NETWORK_TYPE_1xRTT,
                            TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.MOBILE_2G
                            else -> NetworkType.UNKNOWN
                        }
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 
                        NetworkType.ETHERNET
                    else -> NetworkType.UNKNOWN
                }

                Result.Success(mapOf(
                    "connected" to true,
                    "type" to networkType,
                    "metered" to !capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                    ),
                    "validated" to capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
                ))
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                
                if (networkInfo == null || !networkInfo.isConnected) {
                    return Result.Success(mapOf(
                        "connected" to false,
                        "type" to NetworkType.UNKNOWN
                    ))
                }

                @Suppress("DEPRECATION")
                val networkType = when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> {
                        val telephonyManager = 
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        @Suppress("DEPRECATION")
                        when (telephonyManager.networkType) {
                            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.MOBILE_4G
                            TelephonyManager.NETWORK_TYPE_UMTS,
                            TelephonyManager.NETWORK_TYPE_EVDO_0,
                            TelephonyManager.NETWORK_TYPE_EVDO_A,
                            TelephonyManager.NETWORK_TYPE_HSDPA,
                            TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA,
                            TelephonyManager.NETWORK_TYPE_EVDO_B,
                            TelephonyManager.NETWORK_TYPE_EHRPD,
                            TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.MOBILE_3G
                            TelephonyManager.NETWORK_TYPE_GPRS,
                            TelephonyManager.NETWORK_TYPE_EDGE,
                            TelephonyManager.NETWORK_TYPE_CDMA,
                            TelephonyManager.NETWORK_TYPE_1xRTT,
                            TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.MOBILE_2G
                            else -> NetworkType.UNKNOWN
                        }
                    }
                    ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                    else -> NetworkType.UNKNOWN
                }

                Result.Success(mapOf(
                    "connected" to true,
                    "type" to networkType
                ))
            }
        } catch (e: Exception) {
            Result.Error("Failed to get network info", e)
        }
    }

    /**
     * 获取应用信息
     * @param context Context
     * @return Result 包含应用信息或错误信息
     */
    fun getAppInfo(context: Context): Result<Map<String, Any>> {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            
            Result.Success(mapOf(
                "versionName" to packageInfo.versionName,
                "versionCode" to versionCode,
                "packageName" to context.packageName,
                "firstInstallTime" to packageInfo.firstInstallTime,
                "lastUpdateTime" to packageInfo.lastUpdateTime
            )) as Result<Map<String, Any>>
        } catch (e: Exception) {
            Result.Error("Failed to get app info", e)
        }
    }

    /**
     * 获取系统语言和地区信息
     * @return Result 包含语言和地区信息或错误信息
     */
    fun getLocaleInfo(context: Context): Result<Map<String, String>> {
        return try {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            
            Result.Success(mapOf(
                "language" to locale.language,
                "country" to locale.country,
                "displayLanguage" to locale.displayLanguage,
                "displayCountry" to locale.displayCountry
            ))
        } catch (e: Exception) {
            Result.Error("Failed to get locale info", e)
        }
    }

    /**
     * 打开应用设置页面
     */
    fun openAppSettings(
        context: Context?,
        successCallback: (Boolean)->Unit,
        errorCallback: (String, String)->Unit
    ) {
        if (context == null) {
            Log.d("DeviceUtil", "Context cannot be null.")
            errorCallback("PermissionHandler.AppSettingsManager", "Android context cannot be null.")
            return
        }

        try {
            val settingsIntent = Intent()
            settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            settingsIntent.addCategory(Intent.CATEGORY_DEFAULT)
            settingsIntent.setData(Uri.parse("package:" + context.packageName))
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            context.startActivity(settingsIntent)

            successCallback(true)
        } catch (ex: Exception) {
            successCallback(false)
        }
    }
}