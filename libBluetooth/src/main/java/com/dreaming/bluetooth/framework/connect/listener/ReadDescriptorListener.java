package com.dreaming.bluetooth.framework.connect.listener;

import android.bluetooth.BluetoothGattDescriptor;

public interface ReadDescriptorListener extends GattResponseListener {
    void onDescriptorRead(BluetoothGattDescriptor descriptor, int status, byte[] value);
}
