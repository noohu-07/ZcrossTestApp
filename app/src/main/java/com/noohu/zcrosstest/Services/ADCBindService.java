package com.noohu.zcrosstest.Services;

import android.os.IBinder;
import android.util.Log;

import vendor.zumi.adc.IAdcService;

public class ADCBindService {
    private static final String TAG = "NoohuTesting";
    private static final String SERVICE_NAME = "vendor.zumi.adc.IAdcService/default";

    public static IAdcService getBind() {
        try {
            // Load android.os.ServiceManager class
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");

            // Get the getService(String name) method
            java.lang.reflect.Method getServiceMethod = serviceManager.getMethod("getService", String.class);

            // Call getService(SERVICE_NAME)
            IBinder binder = (IBinder) getServiceMethod.invoke(null, SERVICE_NAME);
            Log.d(TAG, "Fetched binder: " + binder);

            if (binder == null) {
                Log.e(TAG, "Service binder is null");
                return null;
            }

            // Log interface descriptors
            String descriptor = binder.getInterfaceDescriptor();
            Log.d(TAG, "Remote descriptor: " + descriptor);
            Log.d(TAG, "Local AIDL descriptor: " + IAdcService.Stub.DESCRIPTOR);

            // Bind to the remote service
            return IAdcService.Stub.asInterface(binder);

        } catch (Exception e) {
            Log.e(TAG, "Error accessing ADCBindService: " + e.getMessage(), e);
            return null;
        }
    }
}