package com.example.android.apis.app;

/**
 * Example of a secondary interface associated with a service.
 */
interface ISecondary {
    /**
     * Request the process ID of this service.
     */
    int getPid();

    /**
     * Basic function that demonstrates passing primitive types.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean,
            float aFloat, double aDouble, String aString);
}