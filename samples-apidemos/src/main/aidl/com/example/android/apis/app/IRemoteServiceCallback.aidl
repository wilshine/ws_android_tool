package com.example.android.apis.app;

/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.
 */
interface IRemoteServiceCallback {
    /**
     * Called when the service has a new value for you.
     */
    void valueChanged(int value);
}