package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.listener.ReadDescriptorListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

import java.util.UUID;

public class BleReadDescriptorRequest extends BleRequest implements ReadDescriptorListener {

    private UUID mServiceUUID;
    private UUID mCharacterUUID;
    private UUID mDescriptorUUID;

    public BleReadDescriptorRequest(UUID service, UUID character, UUID descriptor, BleGeneralResponse response) {
        super(response);
        mServiceUUID = service;
        mCharacterUUID = character;
        mDescriptorUUID = descriptor;
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
        if (!readDescriptor(mServiceUUID, mCharacterUUID, mDescriptorUUID)) {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status, byte[] value) {
        stopRequestTiming();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            putByteArray(BluetoothExtra.ByteValue.toString(), value);
            onRequestCompleted(BluetoothApiResponseCode.Success.code);
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }
}
