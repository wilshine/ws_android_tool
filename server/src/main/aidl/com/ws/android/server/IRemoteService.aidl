package com.ws.android.server;

import com.ws.android.server.model.Student;
import com.ws.android.server.model.RemoteCallback;

// 该文件定义了一个 AIDL 接口，用于客户端和服务端之间的通信
// ./gradlew :server:build 生成对应的 aidl 文件
interface IRemoteService {
    // 定义一个简单的加法运算接口
    int add(int a, int b);
    
    // 获取服务端的字符串
    String getMessage();

    Student getStudentInfo(int age, inout Student student);

    oneway void register(in RemoteCallback callback);

    void changeScore();
}


