package com.dreaming.bluetooth.framework.utils.hook;

import android.os.IBinder;
import android.os.IInterface;

import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.hook.utils.HookUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class BluetoothManagerProxyHandler implements InvocationHandler {
    
    private static BluetoothLogger logger = new BluetoothLogger(BluetoothManagerProxyHandler.class);
    private Object iBluetoothManager;

    private Class<?> bluetoothGattClaz;
    private Object bluetoothGatt;

    BluetoothManagerProxyHandler(Object iBluetoothManager) {
        this.iBluetoothManager = iBluetoothManager;

        this.bluetoothGattClaz = HookUtils.getClass("android.bluetooth.IBluetoothGatt");
        Class<?> stub = HookUtils.getClass("android.bluetooth.IBluetoothManager");
        Method method = HookUtils.getMethod(stub, "getBluetoothGatt");
        this.bluetoothGatt = HookUtils.invoke(method, iBluetoothManager);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.v("IBluetoothManager method: %s", method.getName());

        if ("getBluetoothGatt".equals(method.getName())) {
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                    new Class<?>[] {IBinder.class, IInterface.class, bluetoothGattClaz},
                    new BluetoothGattProxyHandler(bluetoothGatt));
        }
        return method.invoke(iBluetoothManager, args);
    }
}
