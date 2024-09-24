package com.dreaming.bluetooth.framework.connect.listener;

public interface RequestMtuListener extends GattResponseListener {
    void onMtuChanged(int mtu, int status);
}
