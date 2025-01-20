package com.ws.android.base_tool.network

import okhttp3.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ws.android.base_tool.network.interceptor.LogInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import org.json.JSONObject

/**
 * 网络请求工具类
 */
object HttpUtil {

    private lateinit var client: OkHttpClient
    private var baseUrl: String = ""
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val interceptors = mutableListOf<Interceptor>()

    /**
     * 初始化配置
     * @param baseUrl 基础URL
     * @param logging 是否启用日志
     * @param interceptors 初始拦截器列表
     */
    fun init(
        baseUrl: String, 
        logging: Boolean = false,
        interceptors: List<Interceptor>? = null
    ) {
        this.baseUrl = baseUrl
        
        val builder = OkHttpClient.Builder()
        
        // 清除之前的拦截器
        this.interceptors.clear()
        
        // 添加新的拦截器
        interceptors?.let { 
            this.interceptors.addAll(it)
            it.forEach { interceptor ->
                builder.addInterceptor(interceptor)
            }
        }

        if (logging) {
            val loggingInterceptor = LogInterceptor()
            builder.addInterceptor(loggingInterceptor)
            this.interceptors.add(loggingInterceptor)
        }

        client = builder.build()
    }

    /**
     * 添加拦截器
     * @param interceptor 要添加的拦截器
     */
    fun addInterceptor(interceptor: Interceptor) {
        interceptors.add(interceptor)
        
        val builder = client.newBuilder()
        interceptors.forEach { 
            builder.addInterceptor(it)
        }
        client = builder.build()
    }

    /**
     * 移除拦截器
     * @param interceptor 要移除的拦截器
     */
    fun removeInterceptor(interceptor: Interceptor) {
        interceptors.remove(interceptor)
        
        val builder = OkHttpClient.Builder()
        interceptors.forEach { 
            builder.addInterceptor(it)
        }
        client = builder.build()
    }

    /**
     * 清除所有拦截器
     */
    fun clearInterceptors() {
        interceptors.clear()
        client = OkHttpClient.Builder().build()
    }

    /**
     * 发送GET请求并返回响应对象
     * @param url 请求URL
     * @param clazz 响应对象的类型
     */
    suspend fun <T> get(url: String, clazz: Class<T>): Result<T> {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        return@withContext Result.failure(IOException("Unexpected code $it"))
                    }

                    val json = it.body?.string()
                    val result = moshi.adapter(clazz).fromJson(json)
                    Result.success(result)
                } as Result<T>
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    /**
     * 发送POST请求并返回响应对象
     * @param url 请求URL
     * @param body 请求体
     * @param clazz 响应对象的类型
     */
    suspend fun <T> post(url: String, body: Any, clazz: Class<T>): Result<T> {
        val jsonBody = moshi.adapter(Any::class.java).toJson(body)
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody)

        val request = Request.Builder()
            .url("$baseUrl$url")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        return@withContext Result.failure(IOException("Unexpected code $it"))
                    }

                    val json = it.body?.string()
                    val result = moshi.adapter(clazz).fromJson(json)
                    Result.success(result)
                } as Result<T>
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    /**
     * 发送PUT请求并返回响应对象
     * @param url 请求URL
     * @param body 请求体
     * @param clazz 响应对象的类型
     */
    suspend fun <T> put(url: String, body: Any, clazz: Class<T>): Result<T> {
        val jsonBody = moshi.adapter(Any::class.java).toJson(body)
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody)

        val request = Request.Builder()
            .url("$baseUrl$url")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        return@withContext Result.failure(IOException("Unexpected code $it"))
                    }

                    val json = it.body?.string()
                    val result = moshi.adapter(clazz).fromJson(json)
                    Result.success(result)
                } as Result<T>
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    /**
     * 发送DELETE请求并返回响应对象
     * @param url 请求URL
     * @param clazz 响应对象的类型
     * @return Result<T> 包含响应对象的Result对象
     */
    suspend fun <T> delete(url: String, clazz: Class<T>): Result<T> {
        val request = Request.Builder()
            .url("$baseUrl$url")
            .delete()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        return@withContext Result.failure(IOException("Unexpected code $it"))
                    }

                    val json = it.body?.string()
                    val result = moshi.adapter(clazz).fromJson(json)
                    Result.success(result)
                } as Result<T>
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    /**
     * 发送GET请求并返回字符串响应
     * @param url 请求URL
     * @return Result<String> 包含响应文本的Result对象
     */
    suspend fun getPlain(url: String): Result<String> = try {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected response ${response.code}")
        }

        Result.success(response.body?.string() ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 发送GET请求并返回JSON对象
     * @param url 请求URL
     * @return Result<JSONObject> 包含JSON对象的Result
     */
    suspend fun getJson(url: String): Result<JSONObject> = try {
        getPlain(url).map { responseString ->
            JSONObject(responseString)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 发送POST请求并返回字符串响应
     * @param url 请求URL
     * @param body 请求体
     * @return Result<String> 包含响应文本的Result对象
     */
    suspend fun postPlain(url: String, body: Any): Result<String> = try {
        val jsonBody = moshi.adapter(Any::class.java).toJson(body)
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected response ${response.code}")
        }

        Result.success(response.body?.string() ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 发送POST请求并返回JSON对象
     * @param url 请求URL
     * @param body 请求体
     * @return Result<JSONObject> 包含JSON对象的Result
     */
    suspend fun postJson(url: String, body: Any): Result<JSONObject> = try {
        postPlain(url, body).map { responseString ->
            JSONObject(responseString)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 构建表单请求体
     * @param params 表单参数
     * @return FormBody
     */
    private fun buildFormBody(params: Map<String, String>): FormBody {
        return FormBody.Builder().apply {
            params.forEach { (key, value) ->
                add(key, value)
            }
        }.build()
    }

    /**
     * 发送表单POST请求并返回字符串响应
     * @param url 请求URL
     * @param params 表单参数
     * @return Result<String> 包含响应文本的Result对象
     */
    suspend fun postForm(url: String, params: Map<String, String>): Result<String> = try {
        val formBody = buildFormBody(params)
        
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Unexpected response ${response.code}")
        }

        Result.success(response.body?.string() ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 发送表单POST请求并返回JSON对象
     * @param url 请求URL
     * @param params 表单参数
     * @return Result<JSONObject> 包含JSON对象的Result
     */
    suspend fun postFormJson(url: String, params: Map<String, String>): Result<JSONObject> = try {
        postForm(url, params).map { responseString ->
            JSONObject(responseString)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}