package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.listener.ReadCharacterListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

import java.util.UUID;


public class BleReadRequest extends BleRequest implements ReadCharacterListener {

    private UUID mServiceUUID;
    private UUID mCharacterUUID;

    public BleReadRequest(UUID service, UUID character, BleGeneralResponse response) {
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
                startRead();
                break;

            case ServiceReady:
                startRead();
                break;

            default:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;
        }
    }

    private void startRead() {
        if (!readCharacteristic(mServiceUUID, mCharacterUUID)) {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status, byte[] value) {
        stopRequestTiming();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            putByteArray(BluetoothExtra.ByteValue.toString(), value);
            onRequestCompleted(BluetoothApiResponseCode.Success.code);
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }
}
