package com.dreaming.bluetooth.framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

public class BluetoothService extends Service {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothService.class);
    
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.v("onCreate");
        mContext = getApplicationContext();
        BluetoothContext.set(mContext);
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.v("onBind");
        return BluetoothServiceImpl.getInstance();
    }
}
