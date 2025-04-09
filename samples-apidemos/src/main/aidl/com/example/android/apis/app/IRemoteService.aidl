package com.example.android.apis.app;

import com.example.android.apis.app.IRemoteServiceCallback;

/**
 * Example of defining an interface for remote service communication.
 */
interface IRemoteService {
    /**
     * Register a callback interface with the service.
     */
    void registerCallback(IRemoteServiceCallback cb);
    
    /**
     * Unregister a callback interface with the service.
     */
    void unregisterCallback(IRemoteServiceCallback cb);
}