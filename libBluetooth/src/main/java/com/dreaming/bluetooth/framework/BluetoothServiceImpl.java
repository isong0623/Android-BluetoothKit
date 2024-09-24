package com.dreaming.bluetooth.framework;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.dreaming.bluetooth.framework.connect.BleConnectManager;
import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;
import com.dreaming.bluetooth.framework.search.BluetoothSearchManager;
import com.dreaming.bluetooth.framework.search.SearchRequest;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.util.UUID;

public class BluetoothServiceImpl extends IBluetoothService.Stub implements Handler.Callback {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothServiceImpl.class);
    
    private static BluetoothServiceImpl sInstance;

    private Handler mHandler;

    private BluetoothServiceImpl() {
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static BluetoothServiceImpl getInstance() {
        if (sInstance == null) {
            synchronized (BluetoothServiceImpl.class) {
                if (sInstance == null) {
                    sInstance = new BluetoothServiceImpl();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void callBluetoothApi(int code, Bundle args, final IResponse response) throws RemoteException {
        Message msg = mHandler.obtainMessage(code, new BleGeneralResponse() {

            @Override
            public void onResponse(int code, Bundle data) {
                if (response != null) {
                    if (data == null) {
                        data = new Bundle();
                    }
                    try {
                        response.onResponse(code, data);
                    } catch (Throwable e) {
                        logger.e(e);
                    }
                }
            }
        });

        args.setClassLoader(getClass().getClassLoader());
        msg.setData(args);
        msg.sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        Bundle args = msg.getData();
        String mac = args.getString(BluetoothExtra.Mac.toString());
        UUID service = (UUID) args.getSerializable(BluetoothExtra.ServiceUuid.toString());
        UUID character = (UUID) args.getSerializable(BluetoothExtra.CharacterUuid.toString());
        UUID descriptor = (UUID) args.getSerializable(BluetoothExtra.DescriptorUuid.toString());
        byte[] value = args.getByteArray(BluetoothExtra.ByteValue.toString());
        BleGeneralResponse response = (BleGeneralResponse) msg.obj;

        switch (BluetoothApiRequestCode.parse(msg.what)) {
            case Connect:
                BleConnectOptions options = args.getParcelable(BluetoothExtra.Options.toString());
                BleConnectManager.connect(mac, options, response);
                break;

            case Disconnect:
                BleConnectManager.disconnect(mac);
                break;

            case Read:
                BleConnectManager.read(mac, service, character, response);
                break;

            case Write:
                BleConnectManager.write(mac, service, character, value, response);
                break;

            case WriteNorsp:
                BleConnectManager.writeNoRsp(mac, service, character, value, response);
                break;

            case ReadDescriptor:
                BleConnectManager.readDescriptor(mac, service, character, descriptor, response);
                break;

            case WriteDescriptor:
                BleConnectManager.writeDescriptor(mac, service, character, descriptor, value, response);
                break;

            case Notify:
                BleConnectManager.notify(mac, service, character, response);
                break;

            case Unnotify:
                BleConnectManager.unnotify(mac, service, character, response);
                break;

            case ReadRssi:
                BleConnectManager.readRssi(mac, response);
                break;

            case Search:
                SearchRequest request = args.getParcelable(BluetoothExtra.Request.toString());
                BluetoothSearchManager.search(request, response);
                break;

            case StopSearch:
                BluetoothSearchManager.stopSearch();
                break;

            case Indicate:
                BleConnectManager.indicate(mac, service, character, response);
                break;

            case RequestMtu:
                int mtu = args.getInt(BluetoothExtra.Mtu.toString());
                BleConnectManager.requestMtu(mac, mtu, response);
                break;

            case ClearRequest:
                int clearType = args.getInt(BluetoothExtra.Type.toString(), 0);
                BleConnectManager.clearRequest(mac, clearType);
                break;

            case RefreshCache:
                BleConnectManager.refreshCache(mac);
                break;
        }
        return true;
    }
}
