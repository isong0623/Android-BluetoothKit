package com.dreaming.bluetooth.framework;

import com.dreaming.bluetooth.framework.connect.BluetoothDeviceState;
import com.dreaming.bluetooth.framework.connect.listener.BleConnectStatusListener;
import com.dreaming.bluetooth.framework.connect.listener.BluetoothStateListener;
import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleConnectResponse;
import com.dreaming.bluetooth.framework.connect.response.BleMtuResponse;
import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadRssiResponse;
import com.dreaming.bluetooth.framework.connect.response.BleUnnotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleWriteResponse;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothBondListener;
import com.dreaming.bluetooth.framework.search.SearchRequest;
import com.dreaming.bluetooth.framework.search.response.SearchResponse;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;
import com.dreaming.bluetooth.framework.utils.ByteUtils;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyUtils;

import java.util.UUID;

public class BluetoothClient implements IBluetoothClient {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothClient.class);
    
    private IBluetoothClient mClient;

    private static BluetoothClient mInstance;
    public static BluetoothClient getInstance() {
        if (mInstance == null) {
            synchronized (BluetoothClient.class) {
                if (mInstance == null) {
                    logger.i("initialize!");
                    mInstance = new BluetoothClient();
                }
            }
        }
        return mInstance;
    }

    private BluetoothClient() {
        mClient = BluetoothClientImpl.getInstance();
    }

    public void connect(String mac, BleConnectResponse response) {
        connect(mac, null, response);
    }

    @Override
    public void connect(String mac, BleConnectOptions options, BleConnectResponse response) {
        logger.i("connect to %s with options", mac, options);

        response = ProxyUtils.getUIProxy(response);
        mClient.connect(mac, options, response);
    }

    @Override
    public void disconnect(String mac) {
        logger.i("disconnect for %s", mac);

        mClient.disconnect(mac);
    }

    @Override
    public void read(String mac, UUID service, UUID character, BleReadResponse response) {
        logger.i("read character for %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.read(mac, service, character, response);
    }

    @Override
    public void write(String mac, UUID service, UUID character, byte[] value, BleWriteResponse response) {
        logger.i("write character for %s: service = %s, character = %s, value = %s", mac, service, character, ByteUtils.byteToString(value));

        response = ProxyUtils.getUIProxy(response);
        mClient.write(mac, service, character, value, response);
    }

    @Override
    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, BleReadResponse response) {
        logger.i("readDescriptor for %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.readDescriptor(mac, service, character, descriptor, response);
    }

    @Override
    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, BleWriteResponse response) {
        logger.i("writeDescriptor for %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.writeDescriptor(mac, service, character, descriptor, value, response);
    }

    @Override
    public void writeNoRsp(String mac, UUID service, UUID character, byte[] value, BleWriteResponse response) {
        logger.i("writeNoRsp %s: service = %s, character = %s, value = %s", mac, service, character, ByteUtils.byteToString(value));

        response = ProxyUtils.getUIProxy(response);
        mClient.writeNoRsp(mac, service, character, value, response);
    }

    @Override
    public void notify(String mac, UUID service, UUID character, BleNotifyResponse response) {
        logger.i("notify %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.notify(mac, service, character, response);
    }

    @Override
    public void unnotify(String mac, UUID service, UUID character, BleUnnotifyResponse response) {
        logger.i("unnotify %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.unnotify(mac, service, character, response);
    }

    @Override
    public void indicate(String mac, UUID service, UUID character, BleNotifyResponse response) {
        logger.i("indicate %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.indicate(mac, service, character, response);
    }

    @Override
    public void unindicate(String mac, UUID service, UUID character, BleUnnotifyResponse response) {
        logger.i("indicate %s: service = %s, character = %s", mac, service, character);

        response = ProxyUtils.getUIProxy(response);
        mClient.unindicate(mac, service, character, response);
    }

    @Override
    public void readRssi(String mac, BleReadRssiResponse response) {
        logger.i("readRssi %s", mac);

        response = ProxyUtils.getUIProxy(response);
        mClient.readRssi(mac, response);
    }

    @Override
    public void requestMtu(String mac, int mtu, BleMtuResponse response) {
        logger.i("requestMtu %s", mac);

        response = ProxyUtils.getUIProxy(response);
        mClient.requestMtu(mac, mtu, response);
    }

    @Override
    public void search(SearchRequest request, SearchResponse response) {
        logger.i("search %s", request);

        response = ProxyUtils.getUIProxy(response);
        mClient.search(request, response);
    }

    @Override
    public void stopSearch() {
        logger.i("stopSearch");
        mClient.stopSearch();
    }

    @Override
    public void registerConnectStatusListener(String mac, BleConnectStatusListener listener) {
        mClient.registerConnectStatusListener(mac, listener);
    }

    @Override
    public void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener) {
        mClient.unregisterConnectStatusListener(mac, listener);
    }

    @Override
    public void registerBluetoothStateListener(BluetoothStateListener listener) {
        mClient.registerBluetoothStateListener(listener);
    }

    @Override
    public void unregisterBluetoothStateListener(BluetoothStateListener listener) {
        mClient.unregisterBluetoothStateListener(listener);
    }

    @Override
    public void registerBluetoothBondListener(BluetoothBondListener listener) {
        mClient.registerBluetoothBondListener(listener);
    }

    @Override
    public void unregisterBluetoothBondListener(BluetoothBondListener listener) {
        mClient.unregisterBluetoothBondListener(listener);
    }

    public BluetoothDeviceState getConnectStatus(String mac) {
        return BluetoothUtils.getConnectStatus(mac);
    }

    public boolean isBluetoothOpened() {
        return BluetoothUtils.isBluetoothEnabled();
    }

    public boolean openBluetooth() {
        return BluetoothUtils.openBluetooth();
    }

    public boolean closeBluetooth() {
        return BluetoothUtils.closeBluetooth();
    }

    public boolean isBleSupported() {
        return BluetoothUtils.isBleSupported();
    }

    public int getBondState(String mac) {
        return BluetoothUtils.getBondState(mac);
    }

    @Override
    public void clearRequest(String mac, int type) {
        mClient.clearRequest(mac, type);
    }

    @Override
    public void refreshCache(String mac) {
        mClient.refreshCache(mac);
    }
}
