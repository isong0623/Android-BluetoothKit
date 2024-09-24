package com.dreaming.bluetooth.framework.connect;

import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;

import java.util.UUID;

public class BleConnectManager {

    public static void connect(String mac, BleConnectOptions options, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().connect(mac,options, response);
    }

    public static void disconnect(String mac) {
        BleConnectAuthorizer.getInstance().disconnect(mac);
    }

    public static void read(String mac, UUID service, UUID character, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().read(mac, service, character, response);
    }

    public static void write(String mac, UUID service, UUID character, byte[] value, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().write(mac, service, character, value, response);
    }

    public static void writeNoRsp(String mac, UUID service, UUID character, byte[] value, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().writeNoRsp(mac, service, character, value, response);
    }

    public static void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().readDescriptor(mac, service, character, descriptor, response);
    }

    public static void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().writeDescriptor(mac, service, character, descriptor, value, response);
    }

    public static void notify(String mac, UUID service, UUID character, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().notify(mac, service, character, response);
    }

    public static void unnotify(String mac, UUID service, UUID character, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().unnotify(mac, service, character, response);
    }

    public static void readRssi(String mac, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().readRssi(mac, response);
    }

    public static void indicate(String mac, UUID service, UUID character, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().indicate(mac, service, character, response);
    }

    public static void requestMtu(String mac, int mtu, BleGeneralResponse response) {
        BleConnectAuthorizer.getInstance().requestMtu(mac, mtu, response);
    }

    public static void clearRequest(String mac, int type) {
        BleConnectAuthorizer.getInstance().clearRequest(mac, type);
    }

    public static void refreshCache(String mac) {
        BleConnectAuthorizer.getInstance().refreshCache(mac);
    }
}
