package com.dreaming.bluetooth.framework;

import com.dreaming.bluetooth.framework.connect.listener.BleConnectStatusListener;
import com.dreaming.bluetooth.framework.connect.listener.BluetoothStateListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothBondListener;

public interface IBluetoothClientReceiver {

    void registerConnectStatusListener(String mac, BleConnectStatusListener listener);

    void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener);

    void registerBluetoothStateListener(BluetoothStateListener listener);

    void unregisterBluetoothStateListener(BluetoothStateListener listener);

    void registerBluetoothBondListener(BluetoothBondListener listener);

    void unregisterBluetoothBondListener(BluetoothBondListener listener);
}
