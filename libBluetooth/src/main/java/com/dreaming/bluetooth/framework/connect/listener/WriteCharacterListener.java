package com.dreaming.bluetooth.framework.connect.listener;

import android.bluetooth.BluetoothGattCharacteristic;

public interface WriteCharacterListener extends GattResponseListener {
    void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status, byte[] value);
}
