package com.dreaming.bluetooth;


import android.bluetooth.BluetoothDevice;

import com.dreaming.bluetooth.framework.BluetoothClient;
import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleWriteResponse;
import com.dreaming.bluetooth.framework.utils.ByteUtils;
import com.dreaming.bluetooth.framework.utils.UUIDUtils;

import static com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode.Success;

import java.util.UUID;


public class SecureConnector {

    private static BluetoothDevice mDevice;

    public static void processStep1(BluetoothDevice device) {
        mDevice = device;

        BluetoothClient.getInstance().write(device.getAddress(), UUIDUtils.makeUUID(0xFE95), UUIDUtils.makeUUID(0x10),
                ByteUtils.fromInt(0xDE85CA90), new BleWriteResponse() {
                    @Override
                    public void onResponse(int code) {
                        if (code == Success.code) {
                            processStep2();
                        }
                    }
                });
    }

    public static void processStep2() {
        BluetoothClient.getInstance().notify(mDevice.getAddress(), UUIDUtils.makeUUID(0xFE95),
                UUIDUtils.makeUUID(0x01), new BleNotifyResponse() {
                    @Override
                    public void onNotify(UUID service, UUID character, byte[] value) {

                    }

                    @Override
                    public void onResponse(int code) {

                    }
                });
    }
}
