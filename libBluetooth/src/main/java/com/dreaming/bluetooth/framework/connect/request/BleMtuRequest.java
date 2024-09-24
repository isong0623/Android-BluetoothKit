package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.listener.RequestMtuListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

public class BleMtuRequest extends BleRequest implements RequestMtuListener {

    private int mMtu;

    public BleMtuRequest(int mtu, BleGeneralResponse response) {
        super(response);
        mMtu = mtu;
    }

    @Override
    public void processRequest() {
        switch (getCurrentStatus()) {
            case Disconnected:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;

            case Connected:
                requestMtu();
                break;

            case ServiceReady:
                requestMtu();
                break;

            default:
                onRequestCompleted(BluetoothApiResponseCode.Failed.code);
                break;
        }
    }

    private void requestMtu() {
        if (!requestMtu(mMtu)) {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onMtuChanged(int mtu, int status) {
        stopRequestTiming();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            putIntExtra(BluetoothExtra.Mtu.toString(), mtu);
            onRequestCompleted(BluetoothApiResponseCode.Success.code);
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }
}
