package com.dreaming.bluetooth.framework.connect.request;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

public class BleRefreshCacheRequest extends BleRequest {

    public BleRefreshCacheRequest(BleGeneralResponse response) {
        super(response);
    }

    @Override
    public void processRequest() {
        refreshDeviceCache();

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                onRequestCompleted(BluetoothApiResponseCode.Success.code);
            }
        }, 3000);
    }
}
