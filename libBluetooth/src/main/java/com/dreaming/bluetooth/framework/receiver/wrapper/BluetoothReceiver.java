package com.dreaming.bluetooth.framework.receiver.wrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.dreaming.bluetooth.framework.BluetoothContext;
import com.dreaming.bluetooth.framework.receiver.impl.BleCharacterChangeReceiver;
import com.dreaming.bluetooth.framework.receiver.impl.BleConnectStatusChangeReceiver;
import com.dreaming.bluetooth.framework.receiver.impl.BluetoothBondReceiver;
import com.dreaming.bluetooth.framework.receiver.impl.BluetoothStateReceiver;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothReceiverListener;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BluetoothReceiver extends BroadcastReceiver implements IBluetoothReceiver, Handler.Callback {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothReceiver.class);

    private static final int MSG_REGISTER = 1;

    private Map<String, List<BluetoothReceiverListener>> mListeners;

    private static IBluetoothReceiver mReceiver;

    private Handler mHandler;

    private IReceiverDispatcher mDispatcher = new IReceiverDispatcher() {
        @Override
        public List<BluetoothReceiverListener> getListeners(Class<?> clazz) {
            return mListeners.get(clazz.getSimpleName());
        }
    };

    private AbsBluetoothReceiver[] RECEIVERS = {
            BluetoothStateReceiver.newInstance(mDispatcher),
            BluetoothBondReceiver.newInstance(mDispatcher),
            BleConnectStatusChangeReceiver.newInstance(mDispatcher),
            BleCharacterChangeReceiver.newInstance(mDispatcher),
    };

    public static IBluetoothReceiver getInstance() {
        if (mReceiver == null) {
            synchronized (BluetoothReceiver.class) {
                if (mReceiver == null) {
                    mReceiver = new BluetoothReceiver();
                }
            }
        }
        return mReceiver;
    }

    private BluetoothReceiver() {
        mListeners = new HashMap<String, List<BluetoothReceiverListener>>();
        mHandler = new Handler(Looper.getMainLooper(), this);
        BluetoothUtils.registerReceiver(this, getIntentFilter());
    }

    private IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        for (AbsBluetoothReceiver receiver : RECEIVERS) {
            List<String> actions = receiver.getActions();
            for (String action : actions) {
                filter.addAction(action);
            }
        }
        return filter;
    }

    private boolean isInitContext = false;
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(context!=null && !isInitContext){
            isInitContext = true;
            BluetoothContext.set(context.getApplicationContext());
        }

        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        if (TextUtils.isEmpty(action)) {
            return;
        }

        logger.v("onReceive: %s", action);

        for (AbsBluetoothReceiver receiver : RECEIVERS) {
            if (receiver.onReceive(context, intent)) {
                break;
            }
        }
    }

    @Override
    public void register(BluetoothReceiverListener listener) {
        mHandler.obtainMessage(MSG_REGISTER, listener).sendToTarget();
    }

    private void registerInner(BluetoothReceiverListener listener) {
        if (listener != null) {
            List<BluetoothReceiverListener> listeners = mListeners.get(listener.getName());
            if (listeners == null) {
                listeners = new LinkedList<BluetoothReceiverListener>();
                mListeners.put(listener.getName(), listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REGISTER:
                registerInner((BluetoothReceiverListener) msg.obj);
                break;
        }
        return true;
    }
}
