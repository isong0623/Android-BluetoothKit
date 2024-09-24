package com.dreaming.bluetooth.framework.connect;

import com.dreaming.bluetooth.framework.connect.listener.GattResponseListener;
import com.dreaming.bluetooth.framework.model.BleGattProfile;

import java.util.UUID;

public interface IBleConnectWorker {

    boolean openGatt();

    void closeGatt();

    boolean discoverService();

    BluetoothDeviceState getCurrentStatus();

    void registerGattResponseListener(GattResponseListener listener);

    void clearGattResponseListener(GattResponseListener listener);

    boolean refreshDeviceCache();

    boolean readCharacteristic(UUID service, UUID characteristic);

    boolean writeCharacteristic(UUID service, UUID character, byte[] value);

    boolean readDescriptor(UUID service, UUID characteristic, UUID descriptor);

    boolean writeDescriptor(UUID service, UUID characteristic, UUID descriptor, byte[] value);

    boolean writeCharacteristicWithNoRsp(UUID service, UUID character, byte[] value);

    boolean setCharacteristicNotification(UUID service, UUID character, boolean enable);

    boolean setCharacteristicIndication(UUID service, UUID character, boolean enable);

    boolean readRemoteRssi();

    boolean requestMtu(int mtu);

    BleGattProfile getGattProfile();
}
