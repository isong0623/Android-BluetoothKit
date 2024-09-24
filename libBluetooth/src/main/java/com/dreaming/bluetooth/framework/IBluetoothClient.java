package com.dreaming.bluetooth.framework;

import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleConnectResponse;
import com.dreaming.bluetooth.framework.connect.response.BleMtuResponse;
import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadRssiResponse;
import com.dreaming.bluetooth.framework.connect.response.BleUnnotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleWriteResponse;
import com.dreaming.bluetooth.framework.search.SearchRequest;
import com.dreaming.bluetooth.framework.search.response.SearchResponse;

import java.util.UUID;

public interface IBluetoothClient extends IBluetoothClientReceiver {

    void connect(String mac, BleConnectOptions options, BleConnectResponse response);

    void disconnect(String mac);

    void read(String mac, UUID service, UUID character, BleReadResponse response);

    void write(String mac, UUID service, UUID character, byte[] value, BleWriteResponse response);

    void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, BleReadResponse response);

    void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, BleWriteResponse response);

    void writeNoRsp(String mac, UUID service, UUID character, byte[] value, BleWriteResponse response);

    void notify(String mac, UUID service, UUID character, BleNotifyResponse response);

    void unnotify(String mac, UUID service, UUID character, BleUnnotifyResponse response);

    void indicate(String mac, UUID service, UUID character, BleNotifyResponse response);

    void unindicate(String mac, UUID service, UUID character, BleUnnotifyResponse response);

    void readRssi(String mac, BleReadRssiResponse response);

    void requestMtu(String mac, int mtu, BleMtuResponse response);

    void search(SearchRequest request, SearchResponse response);

    void stopSearch();

    void clearRequest(String mac, int type);

    void refreshCache(String mac);
}
