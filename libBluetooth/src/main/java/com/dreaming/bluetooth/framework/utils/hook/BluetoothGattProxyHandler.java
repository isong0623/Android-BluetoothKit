package com.dreaming.bluetooth.framework.utils.hook;

import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class BluetoothGattProxyHandler implements InvocationHandler {
    private static BluetoothLogger logger = new BluetoothLogger(BluetoothGattProxyHandler.class);
    private Object bluetoothGatt;

    BluetoothGattProxyHandler(Object bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.v("IBluetoothGatt method: %s", method.getName());
        return method.invoke(bluetoothGatt, args);
    }
}
