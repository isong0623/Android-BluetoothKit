package com.dreaming.bluetooth.framework.connect;

import android.Manifest;
import android.os.HandlerThread;
import android.os.Looper;

import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;
import com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyInterceptor;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BleConnectAuthorizer implements IBleConnectAuthorizer, ProxyInterceptor {
    private static final BluetoothLogger logger = new BluetoothLogger(BleConnectAuthorizer.class);

    private static final String TAG = BleConnectAuthorizer.class.getSimpleName();

    private static HashMap<String, IBleConnectMaster> mBleConnectMasters = new HashMap<String, IBleConnectMaster>();

    private static HandlerThread mWorkerThread;

    private static Looper getWorkerLooper() {
        if (mWorkerThread == null) {
            mWorkerThread = new HandlerThread(TAG);
            mWorkerThread.start();
        }
        return mWorkerThread.getLooper();
    }

    private static IBleConnectMaster getBleConnectMaster(String mac) {
        IBleConnectMaster master;

        master = mBleConnectMasters.get(mac);
        if (master == null) {
            master = BleConnectMaster.newInstance(mac, getWorkerLooper());
            mBleConnectMasters.put(mac, master);
        }

        return master;
    }

    private static IBleConnectAuthorizer mInstance = null;
    public static IBleConnectAuthorizer getInstance(){
        if(mInstance == null){
            synchronized (BleConnectAuthorizer.class){
                if(mInstance == null){
                    BleConnectAuthorizer authorizer = new BleConnectAuthorizer();
                    mInstance = ProxyUtils.getProxy(authorizer, IBleConnectAuthorizer.class, authorizer, false, true);
                }
            }
        }
        return mInstance;
    }

    public void connect(String mac, BleConnectOptions options, BleGeneralResponse response) {
        getBleConnectMaster(mac).connect(options, response);
    }

    public void disconnect(String mac) {
        getBleConnectMaster(mac).disconnect();
    }

    public void read(String mac, UUID service, UUID character, BleGeneralResponse response) {
        getBleConnectMaster(mac).read(service, character, response);
    }

    public void write(String mac, UUID service, UUID character, byte[] value, BleGeneralResponse response) {
        getBleConnectMaster(mac).write(service, character, value, response);
    }

    public void writeNoRsp(String mac, UUID service, UUID character, byte[] value, BleGeneralResponse response) {
        getBleConnectMaster(mac).writeNoRsp(service, character, value, response);
    }

    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, BleGeneralResponse response) {
        getBleConnectMaster(mac).readDescriptor(service, character, descriptor, response);
    }

    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, BleGeneralResponse response) {
        getBleConnectMaster(mac).writeDescriptor(service, character, descriptor, value, response);
    }

    public void notify(String mac, UUID service, UUID character, BleGeneralResponse response) {
        getBleConnectMaster(mac).notify(service, character, response);
    }

    public void unnotify(String mac, UUID service, UUID character, BleGeneralResponse response) {
        getBleConnectMaster(mac).unnotify(service, character, response);
    }

    public void readRssi(String mac, BleGeneralResponse response) {
        getBleConnectMaster(mac).readRssi(response);
    }

    public void indicate(String mac, UUID service, UUID character, BleGeneralResponse response) {
        getBleConnectMaster(mac).indicate(service, character, response);
    }

    public void requestMtu(String mac, int mtu, BleGeneralResponse response) {
        getBleConnectMaster(mac).requestMtu(mtu, response);
    }

    public void clearRequest(String mac, int type) {
        getBleConnectMaster(mac).clearRequest(type);
    }

    public void refreshCache(String mac) {
        getBleConnectMaster(mac).refreshCache();
    }

    public interface Action<T>{
        void onAction(T data);
    }
    public interface PermissionRequestor{
        void onRequestPermissions(Action<List<String>> onGranted, Action<List<String>> onDenied, String... permissions);
    }

    private static final PermissionRequestor defaultPermissionRequestor = new PermissionRequestor() {
        @Override
        public void onRequestPermissions(Action<List<String>> onGranted, Action<List<String>> onDenied, String... permissions) {
            onGranted.onAction(Arrays.asList(permissions));
        }
    };
    private static PermissionRequestor permissionRequestor = ProxyUtils.getProxy(defaultPermissionRequestor, PermissionRequestor.class, null, false, true);
    public static void setPermissionRequestor(PermissionRequestor requestor){
        permissionRequestor = ProxyUtils.getProxy(requestor==null? defaultPermissionRequestor :requestor, PermissionRequestor.class, null, false, true);
    }
    private static void call(String tag, Action<List<String>> onGranted, Action<List<String>> onDenied, String... permissions){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(String p:permissions){
            if(sb.length()>0) sb.append(",");
            sb.append(p);
        }
        sb.append("]");

        BluetoothLogger.Tag oTag = new BluetoothLogger.Tag(tag);
        if(permissionRequestor != defaultPermissionRequestor)
            logger.d(oTag, "call permissions: %s", sb.toString());
        permissionRequestor.onRequestPermissions(onGranted, onDenied, permissions);
    }


    @Override
    public boolean onIntercept(final Object object, final Method method, final Object[] args) {
        if(object == null) return false;
        if("onIntercept".equals(method.getName())) return false;
        logger.v("onIntercept: thread=%s method:%s",Thread.currentThread().getName(), method.getName());
        call(method.getName(), new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        try {
                            method.invoke(object, args);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        for(int i=args.length-1;i>-1;--i){
                            Object o = args[i];
                            if(o instanceof BleGeneralResponse){
                                ((BleGeneralResponse)o).onResponse(BluetoothApiResponseCode.Denied.code, null);
                            }
                        }
                    }
                },
                Manifest.permission.BLUETOOTH //TODO 查询各个api需要的权限
        );

        return true;
    }
}
