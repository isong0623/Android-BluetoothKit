package com.dreaming.bluetooth.framework.connect.request;

import android.bluetooth.BluetoothGatt;
import android.os.Message;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.listener.ServiceDiscoverListener;
import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;
import com.dreaming.bluetooth.framework.model.BleGattProfile;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

public class BleConnectRequest extends BleRequest implements ServiceDiscoverListener {
    private static BluetoothLogger logger = new BluetoothLogger(BleConnectRequest.class);
    private static final int MSG_CONNECT = 1;
    private static final int MSG_DISCOVER_SERVICE = 2;
    private static final int MSG_CONNECT_TIMEOUT = 3;
    private static final int MSG_DISCOVER_SERVICE_TIMEOUT = 4;
    private static final int MSG_RETRY_DISCOVER_SERVICE = 5;

    private BleConnectOptions mConnectOptions;

    private int mConnectCount;

    private int mServiceDiscoverCount;

    public BleConnectRequest(BleConnectOptions options, BleGeneralResponse response) {
        super(response);
        this.mConnectOptions = options != null ? options : new BleConnectOptions.Builder().build();
    }

    @Override
    public void processRequest() {
        processConnect();
    }

    private void processConnect() {
        mHandler.removeCallbacksAndMessages(null);
        mServiceDiscoverCount = 0;

        switch (getCurrentStatus()) {
            case Connected:
                processDiscoverService();
                break;

            case Disconnected:
                if (!doOpenNewGatt()) {
                    closeGatt();
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, mConnectOptions.getConnectTimeout());
                }
                break;

            case ServiceReady:
                onConnectSuccess();
                break;
        }
    }

    private boolean doOpenNewGatt() {
        mConnectCount++;
        return openGatt();
    }

    private boolean doDiscoverService() {
        mServiceDiscoverCount++;
        return discoverService();
    }

    private void retryConnectIfNeeded() {
        if (mConnectCount < mConnectOptions.getConnectRetry() + 1) {
            retryConnectLater();
        } else {
            onRequestCompleted(BluetoothApiResponseCode.Failed.code);
        }
    }

    private void retryDiscoverServiceIfNeeded() {
        if (mServiceDiscoverCount < mConnectOptions.getServiceDiscoverRetry() + 1) {
            retryDiscoverServiceLater();
        } else {
            closeGatt();
        }
    }

    private void onServiceDiscoverFailed() {
        logger.v("onServiceDiscoverFailed");
        refreshDeviceCache();
        mHandler.sendEmptyMessage(MSG_RETRY_DISCOVER_SERVICE);
    }

    private void processDiscoverService() {
        logger.v("processDiscoverService, status = %s", getStatusText());

        switch (getCurrentStatus()) {
            case Connected:
                if (!doDiscoverService()) {
                    onServiceDiscoverFailed();
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_DISCOVER_SERVICE_TIMEOUT, mConnectOptions.getServiceDiscoverTimeout());
                }
                break;

            case Disconnected:
                retryConnectIfNeeded();
                break;

            case ServiceReady:
                onConnectSuccess();
                break;
        }
    }

    private void retryConnectLater() {
        logger.v("%s retry connect later", getAddress());
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT, 1000);
    }

    private void retryDiscoverServiceLater() {
        logger.v("%s retry discover service later", getAddress());
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_DISCOVER_SERVICE, 1000);
    }

    private void processConnectTimeout() {
        logger.v("%s connect timeout", getAddress());
        mHandler.removeCallbacksAndMessages(null);
        closeGatt();
    }

    private void processDiscoverServiceTimeout() {
        logger.v("%s service discover timeout", getAddress());
        mHandler.removeCallbacksAndMessages(null);
        closeGatt();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CONNECT:
                processConnect();
                break;

            case MSG_DISCOVER_SERVICE:
                processDiscoverService();
                break;

            case MSG_RETRY_DISCOVER_SERVICE:
                retryDiscoverServiceIfNeeded();
                break;

            case MSG_CONNECT_TIMEOUT:
                processConnectTimeout();
                break;

            case MSG_DISCOVER_SERVICE_TIMEOUT:
                processDiscoverServiceTimeout();
                break;
        }
        return super.handleMessage(msg);
    }

    @Override
    public String toString() {
        return "BleConnectRequest{" +
                "options=" + mConnectOptions +
                '}';
    }

    @Override
    public void onConnectStatusChanged(boolean connectedOrDisconnected) {
        checkRuntime();

        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);

        if (connectedOrDisconnected) {
            mHandler.sendEmptyMessageDelayed(MSG_DISCOVER_SERVICE, 300);
        } else {
            mHandler.removeCallbacksAndMessages(null);
            retryConnectIfNeeded();
        }
    }

    @Override
    public void onServicesDiscovered(int status, BleGattProfile profile) {
        checkRuntime();

        mHandler.removeMessages(MSG_DISCOVER_SERVICE_TIMEOUT);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            onConnectSuccess();
        } else {
            onServiceDiscoverFailed();
        }
    }

    private void onConnectSuccess() {
        BleGattProfile profile = getGattProfile();
        if (profile != null) {
            putParcelable(BluetoothExtra.GattProfile.toString(), profile);
        }
        onRequestCompleted(BluetoothApiResponseCode.Success.code);
    }
}
