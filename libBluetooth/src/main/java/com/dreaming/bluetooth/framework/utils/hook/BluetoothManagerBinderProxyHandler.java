package com.dreaming.bluetooth.framework.utils.hook;

import android.os.IBinder;
import android.os.IInterface;

import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.hook.utils.HookUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class BluetoothManagerBinderProxyHandler implements InvocationHandler {
    
    private static BluetoothLogger logger = new BluetoothLogger(BluetoothManagerBinderProxyHandler.class);
    private IBinder iBinder;

    private Class<?> iBluetoothManagerClaz;
    private Object iBluetoothManager;

    BluetoothManagerBinderProxyHandler(IBinder iBinder) {
        this.iBinder = iBinder;

        this.iBluetoothManagerClaz = HookUtils.getClass("android.bluetooth.IBluetoothManager");
        Class<?> stub = HookUtils.getClass("android.bluetooth.IBluetoothManager$Stub");
        Method asInterface = HookUtils.getMethod(stub, "asInterface", IBinder.class);
        this.iBluetoothManager = HookUtils.invoke(asInterface, null, iBinder);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.v("IBinder method: %s", method.getName());

        if ("queryLocalInterface".equals(method.getName())) {
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                    new Class<?>[] {IBinder.class, IInterface.class, iBluetoothManagerClaz},
                    new BluetoothManagerProxyHandler(iBluetoothManager));
        }
        return method.invoke(iBinder, args);
    }
}
