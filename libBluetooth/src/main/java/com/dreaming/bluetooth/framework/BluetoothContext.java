package com.dreaming.bluetooth.framework;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.lang.reflect.Method;

public class BluetoothContext {
    private static BluetoothLogger logger = new BluetoothLogger(BluetoothContext.class);

    private static Context mContext;
    private static Handler mHandler;
    private static Activity mActivity;

    private static BluetoothLogger.Tag tSet = new BluetoothLogger.Tag("set");
    //BluetoothReceiver.onReceive中会尝试初始化Context
    //最好还是在Application.attachContext里调用set
    //BluetoothService创建时也会初始化，但Service是跨进程的。
    public static void set(Context context) {
        if(mContext != null) return;
        synchronized (BluetoothContext.class){
            if(mContext == null){
                Context ctx = context.getApplicationContext();
                if(ctx == null) return;
                mContext = context.getApplicationContext();
                Application app = (Application) mContext;
                app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { mActivity = activity; }
                    @Override
                    public void onActivityStarted(Activity activity) { }
                    @Override
                    public void onActivityResumed(Activity activity) { mActivity = activity; }
                    @Override
                    public void onActivityPaused(Activity activity) { }
                    @Override
                    public void onActivityStopped(Activity activity) { }
                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
                    @Override
                    public void onActivityDestroyed(Activity activity) { }
                });
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logger.i(tSet,"initialize context success!");
                    }
                }, 1000);
            }
        }
    }

    private static BluetoothLogger.Tag tGet = new BluetoothLogger.Tag("get");
    public static Context get() {
        if(mContext == null){
            synchronized (BluetoothContext.class){
                if(mContext == null){
                    final long start = System.currentTimeMillis();
                    Log.i(tGet.toString(), "initialize context with reflect start!");
                    //最好是在Application.onCreate里调用set
                    //以下反射获取Application.context
                    //不推荐，影响性能。
                    //不能保证后续的函数是否变化，虽然变化的概率很低。
                    try {
                        @SuppressLint("PrivateApi")
                        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
                        Method mtdCurrentActivityThread = ActivityThread.getMethod("currentActivityThread");
                        Object currentActivityThread = mtdCurrentActivityThread.invoke(ActivityThread);
                        Method mtdGetApplication = currentActivityThread.getClass().getMethod("getApplication");
                        set((Context)mtdGetApplication.invoke(currentActivityThread));
                        final long end = System.currentTimeMillis();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                logger.i(tGet, "initialize context with reflect success cost %d ms!", end-start);
                            }
                        }, 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //这里初始化失败了不能用Logger
                        //否则会形成死循环
                        Log.i(tGet.toString(), "initialize context with reflect failure!");
                        Log.e(tGet.toString(),e.getMessage());
                    }
                }
            }
        }
        if(mContext == null){
            throw new NullPointerException("BluetoothContext not init, you must call [BluetoothContext.set(this);] in [Application.onCreate]!");
        }
        return mContext;
    }

    public static Activity getCurrentActivity(){
        return mActivity;
    }

    public static void post(Runnable runnable) {
        postDelayed(runnable, 0);
    }

    public static void postDelayed(Runnable runnable, long delayInMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.postDelayed(runnable, delayInMillis);
    }

    public static String getCurrentMethodName() {
        StackTraceElement e = Thread.currentThread().getStackTrace()[4];
        return e.getMethodName();
    }
}
