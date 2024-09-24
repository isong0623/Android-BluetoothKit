package com.dreaming.bluetooth.framework.connect.listener;

public interface ReadRssiListener extends GattResponseListener {
    void onReadRemoteRssi(int rssi, int status);
}
