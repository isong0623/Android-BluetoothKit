package com.dreaming.bluetooth.framework.connect.listener;

import com.dreaming.bluetooth.framework.model.BleGattProfile;

public interface ServiceDiscoverListener extends GattResponseListener {
    void onServicesDiscovered(int status, BleGattProfile profile);
}
