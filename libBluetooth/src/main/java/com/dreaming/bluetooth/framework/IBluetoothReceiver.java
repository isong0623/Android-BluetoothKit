package com.dreaming.bluetooth.framework;

import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;

import java.util.UUID;

public interface IBluetoothReceiver extends IBluetoothClientReceiver{

    void saveNotifyListener(String mac, UUID service, UUID character, BleNotifyResponse response);

    void removeNotifyListener(String mac, UUID service, UUID character);

    void clearNotifyListener(String mac);
}
