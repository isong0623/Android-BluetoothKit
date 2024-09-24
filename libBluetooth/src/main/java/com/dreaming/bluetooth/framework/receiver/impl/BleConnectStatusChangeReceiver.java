package com.dreaming.bluetooth.framework.receiver.impl;

import android.content.Context;
import android.content.Intent;

import com.dreaming.bluetooth.framework.connect.BluetoothAction;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.receiver.listener.BleConnectStatusChangeListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothReceiverListener;
import com.dreaming.bluetooth.framework.receiver.wrapper.AbsBluetoothReceiver;
import com.dreaming.bluetooth.framework.receiver.wrapper.IReceiverDispatcher;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.util.Arrays;
import java.util.List;

public class BleConnectStatusChangeReceiver extends AbsBluetoothReceiver {

    private static BluetoothLogger logger = new BluetoothLogger(BleConnectStatusChangeReceiver.class);

    private static final String[] ACTIONS = {
            BluetoothAction.ConnectStatusChanged.toString()
    };

    protected BleConnectStatusChangeReceiver(IReceiverDispatcher dispatcher) {
        super(dispatcher);
    }

    public static BleConnectStatusChangeReceiver newInstance(IReceiverDispatcher dispatcher) {
        return new BleConnectStatusChangeReceiver(dispatcher);
    }

    @Override
    protected List<String> getActions() {
        return Arrays.asList(ACTIONS);
    }

    @Override
    protected boolean onReceive(Context context, Intent intent) {
        if(!containsAction(intent.getAction())) return false;
        String mac = intent.getStringExtra(BluetoothExtra.Mac.toString());
        int status = intent.getIntExtra(BluetoothExtra.Status.toString(), 0);

        logger.v("onConnectStatusChanged for %s, status = %d", mac, status);
        onConnectStatusChanged(mac, status);
        return true;
    }

    private void onConnectStatusChanged(String mac, int status) {
        List<BluetoothReceiverListener> listeners = getListeners(BleConnectStatusChangeListener.class);
        for (BluetoothReceiverListener listener : listeners) {
            listener.invoke(mac, status);
        }
    }
}
