package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.listener.ReadRssiListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

public class BleReadRssiRequest extends BleRequest implements ReadRssiListener {

    public BleReadRssiRequest(BleGeneralResponse response) {
        super(response);
    }

    @Override
    public void processRequest() {
        switch (getCurrentStatus()) {
            case Disconnected:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;

            case Connected:
                startReadRssi();
                break;

            case ServiceReady:
                startReadRssi();
                break;

            default:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;
        }
    }

    private void startReadRssi() {
        if (!readRemoteRssi()) {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onReadRemoteRssi(int rssi, int status) {
        stopRequestTiming();
        
        if (status == BluetoothGatt.GATT_SUCCESS) {
            putIntExtra(BluetoothExtra.Rssi.toString(), rssi);
            onRequestCompleted(BluetoothApiResponseCode.Success.code);
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }
}

