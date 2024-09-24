package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.listener.WriteCharacterListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

import java.util.UUID;

public class BleWriteRequest extends BleRequest implements WriteCharacterListener {

    private UUID mServiceUUID;
    private UUID mCharacterUUID;
    private byte[] mBytes;

    public BleWriteRequest(UUID service, UUID character, byte[] bytes, BleGeneralResponse response) {
        super(response);
        mServiceUUID = service;
        mCharacterUUID = character;
        mBytes = bytes;
    }

    @Override
    public void processRequest() {
        switch (getCurrentStatus()) {
            case Disconnected:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;

            case Connected:
                startWrite();
                break;

            case ServiceReady:
                startWrite();
                break;

            default:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;
        }
    }

    private void startWrite() {
        if (!writeCharacteristic(mServiceUUID, mCharacterUUID, mBytes)) {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status, byte[] value) {
        stopRequestTiming();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            onRequestCompleted(BluetoothApiResponseCode.Success.code);
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }
}
