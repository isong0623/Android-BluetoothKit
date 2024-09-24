package com.dreaming.bluetooth.framework.utils.hook.compat;

import android.os.IBinder;

import com.dreaming.bluetooth.framework.utils.hook.utils.HookUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ServiceManagerCompat {

    private static Class<?> serviceManager = null;
    private static Field sCache = null;
    private static Method getService = null;

    public static Class<?> getServiceManager() {
        if(serviceManager == null){
            synchronized (ServiceManagerCompat.class){
                if(serviceManager == null){
                    serviceManager = HookUtils.getClass("android.os.ServiceManager");
                }
            }
        }
        return serviceManager;
    }

    public static Field getCacheField() {
        if(sCache == null){
            synchronized (ServiceManagerCompat.class){
                if(sCache == null){
                    sCache = HookUtils.getField(getServiceManager(), "sCache");
                    sCache.setAccessible(true);
                }
            }
        }
        return sCache;
    }

    public static HashMap<String, IBinder> getCacheValue() {
        return HookUtils.getValue(getCacheField());
    }

    public static Method getService() {
        if(getService == null){
            synchronized (ServiceManagerCompat.class){
                if(getService == null){
                    getService = HookUtils.getMethod(getServiceManager(), "getService", String.class);
                }
            }
        }
        return getService;
    }
}
