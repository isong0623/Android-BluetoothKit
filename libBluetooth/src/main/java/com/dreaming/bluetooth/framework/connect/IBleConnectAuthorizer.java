package com.dreaming.bluetooth.framework.connect;

import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

import java.util.UUID;

public interface IBleConnectAuthorizer {
    public void connect(String mac, BleConnectOptions options, BleGeneralResponse response);

    public void disconnect(String mac);

    public void read(String mac, UUID service, UUID character, BleGeneralResponse response);

    public void write(String mac, UUID service, UUID character, byte[] value, BleGeneralResponse response);

    public void writeNoRsp(String mac, UUID service, UUID character, byte[] value, BleGeneralResponse response);

    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, BleGeneralResponse response);

    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, BleGeneralResponse response);

    public void notify(String mac, UUID service, UUID character, BleGeneralResponse response);

    public void unnotify(String mac, UUID service, UUID character, BleGeneralResponse response);

    public void readRssi(String mac, BleGeneralResponse response);

    public void indicate(String mac, UUID service, UUID character, BleGeneralResponse response);

    public void requestMtu(String mac, int mtu, BleGeneralResponse response);

    public void clearRequest(String mac, int type);

    public void refreshCache(String mac);
}
