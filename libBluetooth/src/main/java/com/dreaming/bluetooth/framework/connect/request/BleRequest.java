package com.dreaming.bluetooth.framework.connect.request;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.RuntimeChecker;
import com.dreaming.bluetooth.framework.connect.BluetoothDeviceState;
import com.dreaming.bluetooth.framework.connect.IBleConnectDispatcher;
import com.dreaming.bluetooth.framework.connect.IBleConnectWorker;
import com.dreaming.bluetooth.framework.connect.listener.GattResponseListener;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;
import com.dreaming.bluetooth.framework.model.BleGattProfile;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;

import java.util.UUID;

public abstract class BleRequest implements IBleConnectWorker, IBleRequest, Handler.Callback, GattResponseListener, RuntimeChecker {
    private BluetoothLogger logger = new BluetoothLogger(getClass());

    protected static final int MSG_REQUEST_TIMEOUT = 0x20;

    protected BleGeneralResponse mResponse;

    protected Bundle mExtra;

    protected String mAddress;

    protected IBleConnectDispatcher mDispatcher;

    protected IBleConnectWorker mWorker;

    protected Handler mHandler, mResponseHandler;

    private RuntimeChecker mRuntimeChecker;

    private boolean mFinished;

    protected boolean mRequestTimeout;

    public BleRequest(BleGeneralResponse response) {
        mResponse = response;
        mExtra = new Bundle();
        mHandler = new Handler(Looper.myLooper(), this);
        mResponseHandler = new Handler(Looper.getMainLooper());
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public void setWorker(IBleConnectWorker worker) {
        mWorker = worker;
    }

    /**
     * 请求完成回调，要避免多次回调
     * @param code
     */
    public void onResponse(final int code) {
        if (mFinished) return;
        mFinished = true;

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mResponse != null) {
                        mResponse.onResponse(code, mExtra);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        return sb.toString();
    }

    public void putIntExtra(String key, int value) {
        mExtra.putInt(key, value);
    }

    public int getIntExtra(String key, int defaultValue) {
        return mExtra.getInt(key, defaultValue);
    }

    public void putByteArray(String key, byte[] bytes) {
        mExtra.putByteArray(key, bytes);
    }

    public void putParcelable(String key, Parcelable object) {
        mExtra.putParcelable(key, object);
    }

    public Bundle getExtra() {
        return mExtra;
    }

    protected String getStatusText() {
        return getCurrentStatus().toString();
    }

    @Override
    public boolean readDescriptor(UUID service, UUID characteristic, UUID descriptor) {
        return mWorker.readDescriptor(service, characteristic, descriptor);
    }

    @Override
    public boolean writeDescriptor(UUID service, UUID characteristic, UUID descriptor, byte[] value) {
        return mWorker.writeDescriptor(service, characteristic, descriptor, value);
    }

    public abstract void processRequest();

    @Override
    public boolean openGatt() {
        return mWorker.openGatt();
    }

    @Override
    public boolean discoverService() {
        return mWorker.discoverService();
    }

    @Override
    public BluetoothDeviceState getCurrentStatus() {
        return mWorker.getCurrentStatus();
    }

    @Override
    final public void process(IBleConnectDispatcher dispatcher) {
        checkRuntime();

        mDispatcher = dispatcher;

        logger.w("call process status = %s", getStatusText());

        if (!BluetoothUtils.isBleSupported()) {
            onRequestCompleted(BluetoothApiResponseCode.BleNotSupported.code);
        } else if (!BluetoothUtils.isBluetoothEnabled()) {
            onRequestCompleted(BluetoothApiResponseCode.BluetoothDisabled.code);
        } else {
            try {
                registerGattResponseListener(this);
                processRequest();
            } catch (Throwable e) {
                logger.e(e);
                onRequestCompleted(BluetoothApiResponseCode.Exception.code);
            }
        }
    }

    protected void onRequestCompleted(int code) {
        checkRuntime();

        logger.v("%s request complete: code = %d", getAddress(), code);

        mHandler.removeCallbacksAndMessages(null);
        clearGattResponseListener(this);

        onResponse(code);

        mDispatcher.onRequestCompleted(this);
    }

    @Override
    public void closeGatt() {
        logger.v("%s close gatt", getAddress());
        mWorker.closeGatt();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REQUEST_TIMEOUT:
                mRequestTimeout = true;
                closeGatt();
                break;
        }
        return true;
    }

    @Override
    public void registerGattResponseListener(GattResponseListener listener) {
        mWorker.registerGattResponseListener(listener);
    }

    @Override
    public void clearGattResponseListener(GattResponseListener listener) {
        mWorker.clearGattResponseListener(listener);
    }

    @Override
    public boolean refreshDeviceCache() {
        return mWorker.refreshDeviceCache();
    }

    @Override
    public boolean readCharacteristic(UUID service, UUID characteristic) {
        return mWorker.readCharacteristic(service, characteristic);
    }

    @Override
    public boolean writeCharacteristic(UUID service, UUID character, byte[] value) {
        return mWorker.writeCharacteristic(service, character, value);
    }

    @Override
    public boolean writeCharacteristicWithNoRsp(UUID service, UUID character, byte[] value) {
        return mWorker.writeCharacteristicWithNoRsp(service, character, value);
    }

    @Override
    public boolean setCharacteristicNotification(UUID service, UUID character, boolean enable) {
        return mWorker.setCharacteristicNotification(service, character, enable);
    }

    @Override
    public boolean setCharacteristicIndication(UUID service, UUID character, boolean enable) {
        return mWorker.setCharacteristicIndication(service, character, enable);
    }

    @Override
    public boolean readRemoteRssi() {
        return mWorker.readRemoteRssi();
    }

    @Override
    public boolean requestMtu(int mtu) {
        return mWorker.requestMtu(mtu);
    }

    public void setRuntimeChecker(RuntimeChecker checker) {
        mRuntimeChecker = checker;
    }

    @Override
    public void checkRuntime() {
        mRuntimeChecker.checkRuntime();
    }

    @Override
    public void cancel() {
        checkRuntime();

        logger.v("%s request canceled", getAddress());
        mHandler.removeCallbacksAndMessages(null);
        clearGattResponseListener(this);

        onResponse(BluetoothApiResponseCode.Canceled.code);
    }

    protected long getTimeoutInMillis() {
        return 30000;
    }

    @Override
    public void onConnectStatusChanged(boolean connectedOrDisconnected) {
        if (!connectedOrDisconnected) {
            onRequestCompleted(mRequestTimeout ? BluetoothApiResponseCode.Timedout.code : BluetoothApiResponseCode.Failed.code);
        }
    }

    protected void startRequestTiming() {
        mHandler.sendEmptyMessageDelayed(MSG_REQUEST_TIMEOUT, getTimeoutInMillis());
    }

    protected void stopRequestTiming() {
        mHandler.removeMessages(MSG_REQUEST_TIMEOUT);
    }

    @Override
    public BleGattProfile getGattProfile() {
        return mWorker.getGattProfile();
    }
}

