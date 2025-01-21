package com.ws.android.base_tool.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.whenever
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic

@RunWith(MockitoJUnitRunner::class)
class PermissionUtilTest {

    @Mock
    private lateinit var mockActivity: AppCompatActivity
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockCallback: PermissionUtil.PermissionCallback

    private val testPermission = "android.permission.TEST_PERMISSION"
    private val testPermissions = arrayOf(
        "android.permission.PERMISSION_1",
        "android.permission.PERMISSION_2",
        "android.permission.PERMISSION_3"
    )

    private lateinit var contextCompatMock: MockedStatic<ContextCompat>
    private lateinit var activityCompatMock: MockedStatic<ActivityCompat>

    @Before
    fun setUp() {
        contextCompatMock = mockStatic(ContextCompat::class.java)
        activityCompatMock = mockStatic(ActivityCompat::class.java)
        PermissionUtil.init(mockActivity)
    }

    @After
    fun tearDown() {
        contextCompatMock.close()
        activityCompatMock.close()
    }

    @Test
    fun `test single permission granted`() {
        whenever(ContextCompat.checkSelfPermission(mockContext, testPermission))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        assertTrue(PermissionUtil.isGranted(mockContext, testPermission))
    }

    @Test
    fun `test single permission denied`() {
        whenever(ContextCompat.checkSelfPermission(mockContext, testPermission))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        assertFalse(PermissionUtil.isGranted(mockContext, testPermission))
    }

    @Test
    fun `test all permissions granted`() {
        testPermissions.forEach { permission ->
            whenever(ContextCompat.checkSelfPermission(mockContext, permission))
                .thenReturn(PackageManager.PERMISSION_GRANTED)
        }

        assertTrue(PermissionUtil.areGranted(mockContext, testPermissions))
    }

    @Test
    fun `test some permissions denied`() {
        contextCompatMock.`when`<Int> { 
            ContextCompat.checkSelfPermission(mockContext, testPermissions[0])
        }.thenReturn(PackageManager.PERMISSION_GRANTED)
        
        contextCompatMock.`when`<Int> { 
            ContextCompat.checkSelfPermission(mockContext, testPermissions[1])
        }.thenReturn(PackageManager.PERMISSION_DENIED)

        assertFalse(PermissionUtil.areGranted(mockContext, testPermissions))
    }

    @Test
    fun `test get denied permissions`() {
        whenever(ContextCompat.checkSelfPermission(mockContext, testPermissions[0]))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(ContextCompat.checkSelfPermission(mockContext, testPermissions[1]))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(ContextCompat.checkSelfPermission(mockContext, testPermissions[2]))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        val deniedPermissions = PermissionUtil.getDeniedPermissions(mockContext, testPermissions)
        assertEquals(2, deniedPermissions.size)
        assertTrue(deniedPermissions.contains(testPermissions[1]))
        assertTrue(deniedPermissions.contains(testPermissions[2]))
    }

    @Test
    fun `test permission permanently denied`() {
        whenever(ContextCompat.checkSelfPermission(mockActivity, testPermission))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, testPermission))
            .thenReturn(false)

        assertTrue(PermissionUtil.isPermanentlyDenied(mockActivity, testPermission))
    }

    @Test
    fun `test get permanently denied permissions`() {
        testPermissions.forEach { permission ->
            whenever(ContextCompat.checkSelfPermission(mockActivity, permission))
                .thenReturn(PackageManager.PERMISSION_DENIED)
            whenever(ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, permission))
                .thenReturn(false)
        }

        val permanentlyDenied = PermissionUtil.getPermanentlyDeniedPermissions(mockActivity, testPermissions)
        assertEquals(testPermissions.size, permanentlyDenied.size)
        assertTrue(permanentlyDenied.containsAll(testPermissions.toList()))
    }

    @Test
    fun `test should show rationale`() {
        whenever(ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, testPermission))
            .thenReturn(true)

        assertTrue(PermissionUtil.shouldShowRationale(mockActivity, testPermission))
    }

    @Test
    fun `test should show any rationale`() {
        whenever(ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, testPermissions[0]))
            .thenReturn(false)
        whenever(ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, testPermissions[1]))
            .thenReturn(true)

        assertTrue(PermissionUtil.shouldShowAnyRationale(mockActivity, testPermissions))
    }

    @Test
    fun `test request permissions when all granted`() {
        testPermissions.forEach { permission ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(mockActivity, permission)
            }.thenReturn(PackageManager.PERMISSION_GRANTED)
        }

        PermissionUtil.request(mockActivity, testPermissions, mockCallback)

        verify(mockCallback).onGranted(testPermissions.toList())
        verify(mockCallback, never()).onDenied(emptyList())
        verify(mockCallback, never()).onPermanentlyDenied(emptyList())
    }

    @Test
    fun `test request permissions when some denied and need rationale`() {
        testPermissions.forEach { permission ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(mockActivity, permission)
            }.thenReturn(PackageManager.PERMISSION_DENIED)
            
            activityCompatMock.`when`<Boolean> { 
                ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, permission)
            }.thenReturn(true)
        }

        PermissionUtil.request(mockActivity, testPermissions, mockCallback)

        verify(mockCallback).onDenied(testPermissions.toList())
        verify(mockCallback, never()).onGranted(emptyList())
        verify(mockCallback, never()).onPermanentlyDenied(emptyList())
    }

    @Test
    fun `test open settings success`() {
        doNothing().`when`(mockContext).startActivity(any())
        `when`(mockContext.packageName).thenReturn("com.test.app")

        assertTrue(PermissionUtil.openSettings(mockContext))
        
        val intentCaptor: ArgumentCaptor<Intent> = ArgumentCaptor.forClass(Intent::class.java)
        verify(mockContext).startActivity(intentCaptor.capture())
        
        val capturedIntent = intentCaptor.value
//        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, capturedIntent.action)
        assertEquals(
            Uri.fromParts("package", "com.test.app", null),
            capturedIntent.data
        )
    }

    @Test
    fun `test open settings failure`() {
        doThrow(RuntimeException()).`when`(mockContext).startActivity(any())
        
        assertFalse(PermissionUtil.openSettings(mockContext))
    }

//    @Test(expected = IllegalStateException::class)
//    fun `test request permissions without initialization throws exception`() {
//        // Reset PermissionUtil state
//        PermissionUtil.init(mockActivity)
//
//        // Request permissions without proper initialization
//        PermissionUtil.request(
//            mock(Activity::class.java),
//            arrayOf(testPermission),
//            mockCallback
//        )
//    }
} 