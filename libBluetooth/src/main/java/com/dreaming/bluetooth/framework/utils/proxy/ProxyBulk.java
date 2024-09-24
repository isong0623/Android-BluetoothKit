package com.dreaming.bluetooth.framework.utils.proxy;

import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.lang.reflect.Method;

public class ProxyBulk {

    private static BluetoothLogger logger = new BluetoothLogger(ProxyBulk.class);
    
    public Object object;
    public Method method;
    public Object[] args;

    public ProxyBulk(Object object, Method method, Object[] args) {
        this.object = object;
        this.method = method;
        this.args = args;
    }

    public Object safeInvoke() {
        Object result = null;
        try {
            logger.v("safeInvoke method = %s, object = %s", method, object);
            result = method.invoke(object, args);
        } catch (Throwable e) {
            logger.e(e);
        }
        return result;
    }

    public static Object safeInvoke(Object obj) {
        return ((ProxyBulk) obj).safeInvoke();
    }
}
