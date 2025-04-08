package com.ws.android.server;

interface IRemoteService {
    // 定义一个简单的加法运算接口
    int add(int a, int b);
    
    // 获取服务端的字符串
    String getMessage();
}