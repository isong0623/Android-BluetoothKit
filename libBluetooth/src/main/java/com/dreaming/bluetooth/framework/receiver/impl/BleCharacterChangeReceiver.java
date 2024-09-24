package com.dreaming.bluetooth.framework.receiver.impl;

import android.content.Context;
import android.content.Intent;

import com.dreaming.bluetooth.framework.connect.BluetoothAction;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.receiver.listener.BleCharacterChangeListener;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothReceiverListener;
import com.dreaming.bluetooth.framework.receiver.wrapper.AbsBluetoothReceiver;
import com.dreaming.bluetooth.framework.receiver.wrapper.IReceiverDispatcher;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.ByteUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleCharacterChangeReceiver extends AbsBluetoothReceiver {
    private static BluetoothLogger logger = new BluetoothLogger(BleCharacterChangeReceiver.class);

    private static final String[] ACTIONS = {
        BluetoothAction.CharacterChanged.toString()
    };

    protected BleCharacterChangeReceiver(IReceiverDispatcher dispatcher) {
        super(dispatcher);
    }

    public static BleCharacterChangeReceiver newInstance(IReceiverDispatcher dispatcher) {
        return new BleCharacterChangeReceiver(dispatcher);
    }

    @Override
    protected List<String> getActions() {
        return Arrays.asList(ACTIONS);
    }

    @Override
    protected boolean onReceive(Context context, Intent intent) {
        if(!containsAction(intent.getAction())) return false;

        String mac     = intent.getStringExtra(BluetoothExtra.Mac.toString());
        UUID service   = (UUID) intent.getSerializableExtra(BluetoothExtra.ServiceUuid.toString());
        UUID character = (UUID) intent.getSerializableExtra(BluetoothExtra.CharacterUuid.toString());
        byte[] value   = intent.getByteArrayExtra(BluetoothExtra.ByteValue.toString());
        logger.v("onCharacterChanged for %s: service=%s, character=%s, value=%s", mac, service,character, ByteUtils.byteToString(value));
        onCharacterChanged(mac, service, character, value);
        return true;
    }

    private void onCharacterChanged(String mac, UUID service, UUID character, byte[] value) {
        List<BluetoothReceiverListener> listeners = getListeners(BleCharacterChangeListener.class);
        for (BluetoothReceiverListener listener : listeners) {
            listener.invoke(mac, service, character, value);
        }
    }
}
