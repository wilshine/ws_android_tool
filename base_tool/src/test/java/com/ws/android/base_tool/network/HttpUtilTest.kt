package com.ws.android.base_tool.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ws.android.base_tool.mock.YourRequestBodyClass
import com.ws.android.base_tool.mock.YourResponseClass
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class HttpUtilTest {

    private lateinit var mockWebServer: MockWebServer
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        HttpUtil.init(mockWebServer.url("/").toString(), logging = false)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun test(): Unit = runBlocking {
        HttpUtil.getPlain("https://www.baidu.com").let {
            it.onSuccess { response ->
                println(response)
            }.onFailure { exception ->
                println(exception)
            }
        }
    }

    @Test
    fun testGetSuccess(): Unit = runBlocking {
        // Arrange
        val expectedResponse = YourResponseClass("Test Data")
        mockWebServer.enqueue(MockResponse().setBody(moshi.adapter(YourResponseClass::class.java).toJson(expectedResponse)))

        // Act
        val result = HttpUtil.get("/endpoint", YourResponseClass::class.java)

        // Assert
        result.onSuccess { response ->
            assertEquals(expectedResponse, response)
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }
    }

    @Test
    fun testGetFailure(): Unit = runBlocking {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // Act
        val result = HttpUtil.get("/endpoint", YourResponseClass::class.java)

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }

    @Test
    fun testPostSuccess(): Unit = runBlocking {
        // Arrange
        val requestBody = YourRequestBodyClass("Test Data")
        val expectedResponse = YourResponseClass("Test Data")
        mockWebServer.enqueue(MockResponse().setBody(moshi.adapter(YourResponseClass::class.java).toJson(expectedResponse)))

        // Act
        val result = HttpUtil.post("/endpoint", requestBody, YourResponseClass::class.java)

        // Assert
        result.onSuccess { response ->
            assertEquals(expectedResponse, response)
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }
    }

    @Test
    fun testPostFailure(): Unit = runBlocking {
        // Arrange
        val requestBody = YourRequestBodyClass("Test Data")
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // Act
        val result = HttpUtil.post("/endpoint", requestBody, YourResponseClass::class.java)

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }

    @Test
    fun testGetPlain(): Unit = runBlocking {
        // Arrange
        val expectedText = "Hello, World!"
        mockWebServer.enqueue(MockResponse().setBody(expectedText))

        // Act
        val result = HttpUtil.getPlain("/text")

        // Assert
        result.onSuccess { response ->
            assertEquals(expectedText, response)
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }
    }

    @Test
    fun testGetPlainFailure(): Unit = runBlocking {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // Act
        val result = HttpUtil.getPlain("/text")

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }

    @Test
    fun testGetJson(): Unit = runBlocking {
        // Arrange
        val jsonResponse = """{"name": "John", "age": 30}"""
        mockWebServer.enqueue(MockResponse().setBody(jsonResponse))

        // Act
        val result = HttpUtil.getJson("/json")

        // Assert
        result.onSuccess { jsonObject ->
            assertEquals("John", jsonObject.getString("name"))
            assertEquals(30, jsonObject.getInt("age"))
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }
    }

    @Test
    fun testGetJsonFailure(): Unit = runBlocking {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // Act
        val result = HttpUtil.getJson("/json")

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }

    @Test
    fun testPostPlain(): Unit = runBlocking {
        // Arrange
        val requestBody = YourRequestBodyClass("Test Data")
        val expectedText = "Response Text"
        mockWebServer.enqueue(MockResponse().setBody(expectedText))

        // Act
        val result = HttpUtil.postPlain("/text", requestBody)

        // Assert
        result.onSuccess { response ->
            assertEquals(expectedText, response)
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }

        // Verify request
        val request = mockWebServer.takeRequest()
        assertTrue(request.body.readUtf8().contains("Test Data"))
    }

    @Test
    fun testPostPlainFailure(): Unit = runBlocking {
        // Arrange
        val requestBody = YourRequestBodyClass("Test Data")
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // Act
        val result = HttpUtil.postPlain("/text", requestBody)

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }

    @Test
    fun testPostJson(): Unit = runBlocking {
        // Arrange
        val requestBody = YourRequestBodyClass("Test Data")
        val jsonResponse = """{"name": "John", "age": 30}"""
        mockWebServer.enqueue(MockResponse().setBody(jsonResponse))

        // Act
        val result = HttpUtil.postJson("/json", requestBody)

        // Assert
        result.onSuccess { jsonObject ->
            assertEquals("John", jsonObject.getString("name"))
            assertEquals(30, jsonObject.getInt("age"))
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }

        // Verify request
        val request = mockWebServer.takeRequest()
        assertTrue(request.body.readUtf8().contains("Test Data"))
    }

    @Test
    fun testPostJsonFailure(): Unit = runBlocking {
        // Arrange
        val requestBody = YourRequestBodyClass("Test Data")
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // Act
        val result = HttpUtil.postJson("/json", requestBody)

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }

    @Test
    fun testPostForm(): Unit = runBlocking {
        // Arrange
        val params = mapOf(
            "username" to "john_doe",
            "password" to "secret123"
        )
        val expectedResponse = "Login successful"
        mockWebServer.enqueue(MockResponse().setBody(expectedResponse))

        // Act
        val result = HttpUtil.postForm("/login", params)

        // Assert
        result.onSuccess { response ->
            assertEquals(expectedResponse, response)
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }

        // Verify request
        val request = mockWebServer.takeRequest()
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("username=john_doe"))
        assertTrue(requestBody.contains("password=secret123"))
        assertEquals("application/x-www-form-urlencoded", request.headers["Content-Type"])
    }

    @Test
    fun testPostFormJson(): Unit = runBlocking {
        // Arrange
        val params = mapOf(
            "username" to "john_doe",
            "password" to "secret123"
        )
        val jsonResponse = """{"status": "success", "userId": 123}"""
        mockWebServer.enqueue(MockResponse().setBody(jsonResponse))

        // Act
        val result = HttpUtil.postFormJson("/login", params)

        // Assert
        result.onSuccess { jsonObject ->
            assertEquals("success", jsonObject.getString("status"))
            assertEquals(123, jsonObject.getInt("userId"))
        }.onFailure { exception ->
            assertTrue("Expected success but got failure: ${exception.message}", false)
        }

        // Verify request
        val request = mockWebServer.takeRequest()
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("username=john_doe"))
        assertTrue(requestBody.contains("password=secret123"))
        assertEquals("application/x-www-form-urlencoded", request.headers["Content-Type"])
    }

    @Test
    fun testPostFormFailure(): Unit = runBlocking {
        // Arrange
        val params = mapOf(
            "username" to "john_doe",
            "password" to "wrong_password"
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // Act
        val result = HttpUtil.postForm("/login", params)

        // Assert
        result.onSuccess { response ->
            assertTrue("Expected failure but got success", false)
        }.onFailure { exception ->
            assertTrue("Expected failure", exception is IOException)
        }
    }
}