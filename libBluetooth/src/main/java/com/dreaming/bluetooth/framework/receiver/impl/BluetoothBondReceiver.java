package com.dreaming.bluetooth.framework.receiver.impl;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.dreaming.bluetooth.framework.connect.BluetoothDeviceBondState;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothBondStateChangeListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothReceiverListener;
import com.dreaming.bluetooth.framework.receiver.wrapper.AbsBluetoothReceiver;
import com.dreaming.bluetooth.framework.receiver.wrapper.IReceiverDispatcher;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.util.Arrays;
import java.util.List;

public class BluetoothBondReceiver extends AbsBluetoothReceiver {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothBondReceiver.class);

    private static final String[] ACTIONS = {
            BluetoothDevice.ACTION_BOND_STATE_CHANGED,
    };

    protected BluetoothBondReceiver(IReceiverDispatcher dispatcher) {
        super(dispatcher);
    }

    public static BluetoothBondReceiver newInstance(IReceiverDispatcher dispatcher) {
        return new BluetoothBondReceiver(dispatcher);
    }

    @Override
    protected List<String> getActions() {
        return Arrays.asList(ACTIONS);
    }

    @Override
    protected boolean onReceive(Context context, Intent intent) {
        if(!containsAction(intent.getAction())) return false;

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
        String mac = "null";
        if(device!=null) mac = device.getAddress();
        logger.v("onBondStateChanged for %s: state=%s", mac, BluetoothDeviceBondState.parse(state));
        if (device != null) {
            onBondStateChanged(device.getAddress(), state);
        }
        return true;
    }

    private void onBondStateChanged(String mac, int bondState) {
        List<BluetoothReceiverListener> listeners = getListeners(BluetoothBondStateChangeListener.class);
        for (BluetoothReceiverListener listener : listeners) {
            listener.invoke(mac, bondState);
        }
    }
}
