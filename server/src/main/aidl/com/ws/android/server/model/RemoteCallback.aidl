package com.ws.android.server.model;
import com.ws.android.server.model.Student;

interface RemoteCallback {

    oneway void onCallback(in Student student);
}