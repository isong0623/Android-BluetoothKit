package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.listener.WriteDescriptorListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

import java.util.UUID;

public class BleUnnotifyRequest extends BleRequest implements WriteDescriptorListener {

    private UUID mServiceUUID;
    private UUID mCharacterUUID;

    public BleUnnotifyRequest(UUID service, UUID character, BleGeneralResponse response) {
        super(response);
        mServiceUUID = service;
        mCharacterUUID = character;
    }

    @Override
    public void processRequest() {
        switch (getCurrentStatus()) {
            case Disconnected:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;

            case Connected:
                closeNotify();
                break;

            case ServiceReady:
                closeNotify();
                break;

            default:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;
        }
    }

    private void closeNotify() {
        if (!setCharacteristicNotification(mServiceUUID, mCharacterUUID, false)) {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        stopRequestTiming();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            onRequestCompleted(BluetoothApiResponseCode.Success.code);
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }
}
