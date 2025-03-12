
# ws_android_tool

----

基础工具库

市面上已经有一些成熟的工具库，对于已经有的轮子就不重复造了。

- [https://github.com/Blankj/AndroidUtilCode](https://github.com/Blankj/AndroidUtilCode)
- [https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/README-CN.md](https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/README-CN.md)
- [https://github.com/Blankj/AndroidUtilCode/blob/master/lib/subutil/README-CN.md](https://github.com/Blankj/AndroidUtilCode/blob/master/lib/subutil/README-CN.md)


## 权限管理

- [PermissionUtil]

```java

// 检查权限
fun isGranted(context: Context, permission: String): Boolean {}

// 请求权限
fun request(
        activity: Activity,
        permissions: Array<String>,
        callback: PermissionCallback) {}



```

可以参考：[https://github.com/getActivity/XXPermissions](https://github.com/getActivity/XXPermissions)

## MMKV

MMKV 是基于 mmap 内存映射的 key-value 组件，底层序列化/反序列化使用 protobuf 实现，性能高，稳定性强。如果项目对性能要求高，可以使用这个库。
如果是小项目直接使用SharedPreferences就可以了。

- [https://github.com/Tencent/MMKV](https://github.com/Tencent/MMKV)


## 网络请求

一般常见的功能：

- 基本的请求get、post
- 接口的响应数据直接转换成对应实体类或返回json对象
- 拦截器设置，配置自定义Headers



## 音频播放、录音

音视频库统一使用官方的media3

##