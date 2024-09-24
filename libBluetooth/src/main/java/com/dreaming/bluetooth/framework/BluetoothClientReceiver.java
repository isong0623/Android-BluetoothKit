package com.dreaming.bluetooth.framework;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dreaming.bluetooth.framework.connect.BleConnectStatus;
import com.dreaming.bluetooth.framework.connect.BluetoothState;
import com.dreaming.bluetooth.framework.connect.listener.BleConnectStatusListener;
import com.dreaming.bluetooth.framework.connect.listener.BluetoothStateListener;
import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;
import com.dreaming.bluetooth.framework.receiver.listener.BleCharacterChangeListener;
import com.dreaming.bluetooth.framework.receiver.listener.BleConnectStatusChangeListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothBondListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothBondStateChangeListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothStateChangeListener;
import com.dreaming.bluetooth.framework.receiver.wrapper.BluetoothReceiver;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.ListUtils;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyBulk;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyInterceptor;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BluetoothClientReceiver implements IBluetoothReceiver, ProxyInterceptor,Handler.Callback {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothClientReceiver.class);

    private static final int MSG_REG_RECEIVER = 2;
    private static final int MSG_INVOKE_PROXY = 3;

    private HashMap<String, HashMap<String, List<BleNotifyResponse>>> mNotifyResponses;
    private HashMap<String, List<BleConnectStatusListener>> mConnectStatusListeners;
    private List<BluetoothStateListener> mBluetoothStateListeners;
    private List<BluetoothBondListener> mBluetoothBondListeners;

    private Handler mWorkerHandler;

    private static IBluetoothReceiver sInstance;
    public static IBluetoothReceiver getInstance(Looper looper) {
        if (sInstance == null) {
            synchronized (BluetoothClientReceiver.class) {
                if (sInstance == null) {
                    logger.i("initialize!");
                    BluetoothClientReceiver receiver = new BluetoothClientReceiver(looper);
                    sInstance = ProxyUtils.getProxy(receiver, IBluetoothReceiver.class, receiver);
                }
            }
        }
        return sInstance;
    }

    private BluetoothClientReceiver(Looper looper){
        mWorkerHandler = new Handler(looper);
        mNotifyResponses = new HashMap<String, HashMap<String, List<BleNotifyResponse>>>();
        mConnectStatusListeners = new HashMap<String, List<BleConnectStatusListener>>();
        mBluetoothStateListeners = new LinkedList<BluetoothStateListener>();
        mBluetoothBondListeners = new LinkedList<BluetoothBondListener>();
        mWorkerHandler.obtainMessage(MSG_REG_RECEIVER).sendToTarget();
    }

    @Override
    public void saveNotifyListener(String mac, UUID service, UUID character, BleNotifyResponse response) {
        checkRuntime(true);
        HashMap<String, List<BleNotifyResponse>> listenerMap = mNotifyResponses.get(mac);
        if (listenerMap == null) {
            listenerMap = new HashMap<String, List<BleNotifyResponse>>();
            mNotifyResponses.put(mac, listenerMap);
        }

        String key = generateCharacterKey(service, character);
        List<BleNotifyResponse> responses = listenerMap.get(key);
        if (responses == null) {
            responses = new ArrayList<BleNotifyResponse>();
            listenerMap.put(key, responses);
        }

        responses.add(response);
    }

    @Override
    public void removeNotifyListener(String mac, UUID service, UUID character) {
        checkRuntime(true);
        HashMap<String, List<BleNotifyResponse>> listenerMap = mNotifyResponses.get(mac);
        if (listenerMap != null) {
            String key = generateCharacterKey(service, character);
            listenerMap.remove(key);
        }
    }

    @Override
    public void clearNotifyListener(String mac) {
        checkRuntime(true);
        mNotifyResponses.remove(mac);
    }

    private String generateCharacterKey(UUID service, UUID character) {
        return String.format("%s_%s", service, character);
    }

    @Override
    public void registerConnectStatusListener(String mac, BleConnectStatusListener listener) {
        checkRuntime(true);
        List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
        if (listeners == null) {
            listeners = new ArrayList<BleConnectStatusListener>();
            mConnectStatusListeners.put(mac, listeners);
        }
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener) {
        checkRuntime(true);
        List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
        if (listener != null && !ListUtils.isEmpty(listeners)) {
            listeners.remove(listener);
        }
    }

    @Override
    public void registerBluetoothStateListener(BluetoothStateListener listener) {
        checkRuntime(true);
        if (listener != null && !mBluetoothStateListeners.contains(listener)) {
            mBluetoothStateListeners.add(listener);
        }
    }

    @Override
    public void unregisterBluetoothStateListener(BluetoothStateListener listener) {
        checkRuntime(true);
        if (listener != null) {
            mBluetoothStateListeners.remove(listener);
        }
    }

    @Override
    public void registerBluetoothBondListener(BluetoothBondListener listener) {
        checkRuntime(true);
        if (listener != null && !mBluetoothBondListeners.contains(listener)) {
            mBluetoothBondListeners.add(listener);
        }
    }

    @Override
    public void unregisterBluetoothBondListener(BluetoothBondListener listener) {
        checkRuntime(true);
        if (listener != null) {
            mBluetoothBondListeners.remove(listener);
        }
    }

    private void registerBluetoothReceiver() {
        checkRuntime(true);
        BluetoothReceiver.getInstance().register(new BluetoothStateChangeListener() {
            @Override
            protected void onBluetoothStateChanged(int prevState, int curState) {
                checkRuntime(true);
                dispatchBluetoothStateChanged(curState);
            }
        });
        BluetoothReceiver.getInstance().register(new BluetoothBondStateChangeListener() {
            @Override
            protected void onBondStateChanged(String mac, int bondState) {
                checkRuntime(true);
                dispatchBondStateChanged(mac, bondState);
            }
        });
        BluetoothReceiver.getInstance().register(new BleConnectStatusChangeListener() {
            @Override
            protected void onConnectStatusChanged(String mac, int status) {
                checkRuntime(true);
                if (status == BleConnectStatus.Disconnected.status) {
                    clearNotifyListener(mac);
                }
                dispatchConnectionStatus(mac, status);
            }
        });
        BluetoothReceiver.getInstance().register(new BleCharacterChangeListener() {
            @Override
            public void onCharacterChanged(String mac, UUID service, UUID character, byte[] value) {
                checkRuntime(true);
                dispatchCharacterNotify(mac, service, character, value);
            }
        });
    }

    private void dispatchCharacterNotify(String mac, UUID service, UUID character, byte[] value) {
        checkRuntime(true);
        HashMap<String, List<BleNotifyResponse>> notifyMap = mNotifyResponses.get(mac);
        if (notifyMap != null) {
            String key = generateCharacterKey(service, character);
            List<BleNotifyResponse> responses = notifyMap.get(key);
            if (responses != null) {
                for (final BleNotifyResponse response : responses) {
                    response.onNotify(service, character, value);
                }
            }
        }
    }

    private void dispatchConnectionStatus(final String mac, final int status) {
        checkRuntime(true);
        List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
        if (!ListUtils.isEmpty(listeners)) {
            for (final BleConnectStatusListener listener : listeners) {
                listener.invokeSync(mac, status);
            }
        }
    }

    private void dispatchBluetoothStateChanged(final int currentState) {
        checkRuntime(true);
        if (currentState == BluetoothState.Off.state || currentState == BluetoothState.On.state) {
            for (final BluetoothStateListener listener : mBluetoothStateListeners) {
                listener.invokeSync(currentState == BluetoothState.On.state);
            }
        }
    }

    private void dispatchBondStateChanged(final String mac, final int bondState) {
        checkRuntime(true);
        for (final BluetoothBondListener listener : mBluetoothBondListeners) {
            listener.invokeSync(mac, bondState);
        }
    }

    private void checkRuntime(boolean async) {
        Looper targetLooper = async ? mWorkerHandler.getLooper() : Looper.getMainLooper();
        if (Looper.myLooper() != targetLooper) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean onIntercept(Object object, Method method, Object[] args) {
        mWorkerHandler.obtainMessage(MSG_INVOKE_PROXY, new ProxyBulk(object, method, args)).sendToTarget();
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case MSG_INVOKE_PROXY:
                ProxyBulk.safeInvoke(msg.obj);
                return true;
            case MSG_REG_RECEIVER:
                registerBluetoothReceiver();
                return true;
        }
        return false;
    }
}
