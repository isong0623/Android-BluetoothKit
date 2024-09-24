package com.dreaming.bluetooth.framework;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.listener.BleConnectStatusListener;
import com.dreaming.bluetooth.framework.connect.listener.BluetoothStateListener;
import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode;
import com.dreaming.bluetooth.framework.connect.response.BleConnectResponse;
import com.dreaming.bluetooth.framework.connect.response.BleMtuResponse;
import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadRssiResponse;
import com.dreaming.bluetooth.framework.connect.response.BleUnnotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleWriteResponse;
import com.dreaming.bluetooth.framework.connect.response.BluetoothResponse;
import com.dreaming.bluetooth.framework.model.BleGattProfile;
import com.dreaming.bluetooth.framework.receiver.listener.BluetoothBondListener;
import com.dreaming.bluetooth.framework.search.SearchRequest;
import com.dreaming.bluetooth.framework.search.SearchResult;
import com.dreaming.bluetooth.framework.search.SearchState;
import com.dreaming.bluetooth.framework.search.response.SearchResponse;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;
import com.dreaming.bluetooth.framework.utils.ByteUtils;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyBulk;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyInterceptor;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.ClearRequest;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Connect;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Disconnect;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Indicate;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Notify;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Read;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.ReadDescriptor;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.ReadRssi;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.RefreshCache;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.RequestMtu;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Search;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.StopSearch;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Unnotify;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.Write;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.WriteDescriptor;
import static com.dreaming.bluetooth.framework.connect.request.BluetoothApiRequestCode.WriteNorsp;
import static com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode.Success;
import static com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode.ServiceUnready;
import static com.dreaming.bluetooth.framework.Constants.GATT_DEF_BLE_MTU_SIZE;

public class BluetoothClientImpl implements IBluetoothClient, ProxyInterceptor, Callback {

    private static BluetoothLogger logger = new BluetoothLogger(BluetoothClientImpl.class);
    
    private static final int MSG_INVOKE_PROXY = 1;

    private static final String TAG = BluetoothClientImpl.class.getSimpleName();

    private Context mContext;

    private volatile IBluetoothService mBluetoothService;

    private volatile static IBluetoothClient sInstance;

    private CountDownLatch mCountDownLatch;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;

    private IBluetoothReceiver mClientReceiver;

    private BluetoothClientImpl() {
        Context context = BluetoothContext.get();
        mContext = context.getApplicationContext();

        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper(), this);

        mClientReceiver = BluetoothClientReceiver.getInstance(mWorkerHandler.getLooper());

//        BluetoothHooker.hook();
    }

    @SuppressLint("ResourceType")
    public static IBluetoothClient getInstance() {
        if (sInstance == null) {
            synchronized (BluetoothClientImpl.class) {
                if (sInstance == null) {
                    logger.i("initialize!");
                    BluetoothUtils.printBanner();
                    BluetoothClientImpl client = new BluetoothClientImpl();
                    sInstance = ProxyUtils.getProxy(client, IBluetoothClient.class, client);
                }
            }
        }
        return sInstance;
    }

    private IBluetoothService getBluetoothService() {
        if (mBluetoothService == null) {
            bindServiceSync();
        }
        return mBluetoothService;
    }

    private void bindServiceSync() {
        checkRuntime(true);

        logger.v("call bindServiceSync()");

        mCountDownLatch = new CountDownLatch(1);

        Intent intent = new Intent();
        intent.setClass(mContext, BluetoothService.class);

        if (mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
            logger.v("registered success!");
            waitBluetoothManagerReady();
        } else {
            logger.v("registered failure!");
            mBluetoothService = BluetoothServiceImpl.getInstance();
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.v("onServiceConnected");
            mBluetoothService = IBluetoothService.Stub.asInterface(service);
            notifyBluetoothManagerReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.v("onServiceDisconnected");
            mBluetoothService = null;
        }
    };

    @Override
    public void connect(final String mac, final BleConnectOptions options, final BleConnectResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putParcelable(BluetoothExtra.Options.toString(), options);
        safeCallBluetoothApi(Connect, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                data.setClassLoader(getClass().getClassLoader());
                BleGattProfile profile = data.getParcelable(BluetoothExtra.GattProfile.toString());
                logger.v("connect to %s: << code=%d, profile=%s, >> options=%s", mac,code,profile,options);
                if (response != null) {
                    response.onResponse(code, profile);
                }
            }
        });
    }

    @Override
    public void disconnect(String mac) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        safeCallBluetoothApi(Disconnect, args, null);
        clearNotifyListener(mac);
    }

    @Override
    public void read(final String mac, final UUID service, final UUID character, final BleReadResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        safeCallBluetoothApi(Read, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                byte[] bytes = data.getByteArray(BluetoothExtra.ByteValue.toString());
                if(bytes == null) bytes = new byte[]{};
                logger.v("read for %s: << code=%d, bytes=%s, >> service=%s, character=%s", mac, code, ByteUtils.byteToString(bytes), service, character);
                if (response != null) {
                    response.onResponse(code, bytes);
                }
            }
        });
    }

    @Override
    public void write(final String mac, final UUID service, final UUID character, final byte[] value, final BleWriteResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        args.putByteArray(BluetoothExtra.ByteValue.toString(), value);
        safeCallBluetoothApi(Write, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                logger.v("write for %s: << code:%d, >> bytes=%s, service=%s, character=%s", mac, code, ByteUtils.byteToString(value), service, character);
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void readDescriptor(final String mac, final UUID service, final UUID character, final UUID descriptor, final BleReadResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        args.putSerializable(BluetoothExtra.DescriptorUuid.toString(), descriptor);
        safeCallBluetoothApi(ReadDescriptor, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                byte[] bytes = data.getByteArray(BluetoothExtra.ByteValue.toString());
                if(bytes == null) bytes = new byte[]{};
                logger.v("readDescriptor for %s: << code=%d, bytes=%s, >> service=%s, character=%s, descriptor=%s", mac, code, ByteUtils.byteToString(bytes), service, character, descriptor);
                if (response != null) {
                    response.onResponse(code, bytes);
                }
            }
        });
    }

    @Override
    public void writeDescriptor(final String mac, final UUID service, final UUID character, final UUID descriptor, final byte[] value, final BleWriteResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        args.putSerializable(BluetoothExtra.DescriptorUuid.toString(), descriptor);
        args.putByteArray(BluetoothExtra.ByteValue.toString(), value);
        safeCallBluetoothApi(WriteDescriptor, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                logger.v("writeDescriptor for %s: << code:%d, >> bytes=%s, service=%s, character=%s, descriptor=%s", mac, code, ByteUtils.byteToString(value), service, character, descriptor);
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void writeNoRsp(final String mac, final UUID service, final UUID character, final byte[] value, final BleWriteResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        args.putByteArray(BluetoothExtra.ByteValue.toString(), value);
        safeCallBluetoothApi(WriteNorsp, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                logger.v("writeNoRsp for %s: << code:%d, >> bytes=%s, service=%s, character=%s", mac, code, ByteUtils.byteToString(value), service, character);
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void notify(final String mac, final UUID service, final UUID character, final BleNotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        safeCallBluetoothApi(Notify, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                logger.v("notify for %s: << code:%d, >> service=%s, character=%s", mac, code, service, character);
                if (response != null) {
                    if (code == Success.code) {
                        saveNotifyListener(mac, service, character, response);
                    }
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void unnotify(final String mac, final UUID service, final UUID character, final BleUnnotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        safeCallBluetoothApi(Unnotify, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);

                logger.v("unnotify for %s: << code:%d, >> service=%s, character=%s", mac, code, service, character);
                removeNotifyListener(mac, service, character);

                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void indicate(final String mac, final UUID service, final UUID character, final BleNotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        safeCallBluetoothApi(Indicate, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                logger.v("indicate for %s: << code:%d, >> service=%s, character=%s", mac, code, service, character);
                if (response != null) {
                    if (code == Success.code) {
                        saveNotifyListener(mac, service, character, response);
                    }
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void unindicate(final String mac, final UUID service, final UUID character, final BleUnnotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putSerializable(BluetoothExtra.ServiceUuid.toString(), service);
        args.putSerializable(BluetoothExtra.CharacterUuid.toString(), character);
        safeCallBluetoothApi(Unnotify, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);

                logger.v("unindicate for %s: << code:%d, >> service=%s, character=%s", mac, code, service, character);
                removeNotifyListener(mac, service, character);

                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void readRssi(final String mac, final BleReadRssiResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        safeCallBluetoothApi(ReadRssi, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                logger.v("readRssi to %s: << code=%d", mac, code);
                if (response != null) {
                    response.onResponse(code, data.getInt(BluetoothExtra.Rssi.toString(), 0));
                }
            }
        });
    }

    @Override
    public void requestMtu(final String mac, final int mtu, final BleMtuResponse response) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putInt(BluetoothExtra.Mtu.toString(), mtu);
        safeCallBluetoothApi(RequestMtu, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);
                int responseMtu = data.getInt(BluetoothExtra.Mtu.toString(), GATT_DEF_BLE_MTU_SIZE);
                logger.v("requestMtu to %s: << code=%d, mtu=%d, >> mtu=%d", mac, code, responseMtu, mtu);
                if (response != null) {
                    response.onResponse(code, responseMtu);
                }
            }
        });
    }

    @Override
    public void search(SearchRequest request, final SearchResponse response) {
        Bundle args = new Bundle();
        args.putParcelable(BluetoothExtra.Request.toString(), request);
        safeCallBluetoothApi(Search, args, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                checkRuntime(true);

                SearchState state = SearchState.parse(code);
                SearchResult device = null;
                if(state == SearchState.Finished){
                    data.setClassLoader(getClass().getClassLoader());
                    device = data.getParcelable(BluetoothExtra.SearchResult.toString());
                }
                String sDevice = "null";
                if(device != null){
                    sDevice = "["+device.getName()+","+device.getAddress()+"]";
                }
                logger.v("search << code=%d, state=%s, device=%s", code, state, sDevice);

                if (response == null) {
                    return;
                }

                switch (state) {
                    case Start:
                        response.onSearchStarted();
                        break;
                    case Cancel:
                        response.onSearchCanceled();
                        break;
                    case Stop:
                        response.onSearchStopped();
                        break;
                    case Finished:
                        response.onDeviceFounded(device);
                        break;
                    default:
                        throw new IllegalStateException("unknown code");
                }
            }
        });
    }

    @Override
    public void stopSearch() {
        safeCallBluetoothApi(StopSearch, null, null);
    }

    @Override
    public void clearRequest(String mac, int type) {
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        args.putInt(BluetoothExtra.Type.toString(), type);
        safeCallBluetoothApi(ClearRequest, args, null);
    }

    @Override
    public void refreshCache(String mac) {
        checkRuntime(true);
        Bundle args = new Bundle();
        args.putString(BluetoothExtra.Mac.toString(), mac);
        safeCallBluetoothApi(RefreshCache, args, null);
    }

    private void safeCallBluetoothApi(BluetoothApiRequestCode code, Bundle args, final BluetoothResponse response) {
        checkRuntime(true);

        try {
            IBluetoothService service = getBluetoothService();

            logger.v("safeCallBluetoothApi service=@%s, code=%d, args=Bundle@%s", service, code.code, args.toString().split("@")[1]);

            if (service != null) {
                args = (args != null ? args : new Bundle());
                service.callBluetoothApi(code.code, args, response);
            } else {
                response.onResponse(ServiceUnready.code, null);
            }
        } catch (Throwable e) {
            logger.e(e);
        }
    }

    @Override
    public boolean onIntercept(final Object object, final Method method, final Object[] args) {
        mWorkerHandler.obtainMessage(MSG_INVOKE_PROXY, new ProxyBulk(object, method, args))
                .sendToTarget();
        return true;
    }

    private void notifyBluetoothManagerReady() {
        logger.v("notifyBluetoothManagerReady %s", mCountDownLatch);

        if (mCountDownLatch != null) {
            mCountDownLatch.countDown();
            mCountDownLatch = null;
        }
    }

    private void waitBluetoothManagerReady() {
        logger.v("waitBluetoothManagerReady %s", mCountDownLatch);
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void saveNotifyListener(String mac, UUID service, UUID character, BleNotifyResponse response) {
        mClientReceiver.saveNotifyListener(mac, service, character, response);
    }

    public void removeNotifyListener(String mac, UUID service, UUID character) {
        mClientReceiver.removeNotifyListener(mac, service, character);
    }

    public void clearNotifyListener(String mac) {
        mClientReceiver.clearNotifyListener(mac);
    }

    @Override
    public void registerConnectStatusListener(String mac, BleConnectStatusListener listener) {
        mClientReceiver.registerConnectStatusListener(mac, listener);
    }

    @Override
    public void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener) {
        mClientReceiver.unregisterConnectStatusListener(mac, listener);
    }

    @Override
    public void registerBluetoothStateListener(BluetoothStateListener listener) {
        mClientReceiver.registerBluetoothStateListener(listener);
    }

    @Override
    public void unregisterBluetoothStateListener(BluetoothStateListener listener) {
        mClientReceiver.unregisterBluetoothStateListener(listener);
    }

    @Override
    public void registerBluetoothBondListener(BluetoothBondListener listener) {
        mClientReceiver.registerBluetoothBondListener(listener);
    }

    @Override
    public void unregisterBluetoothBondListener(BluetoothBondListener listener) {
        mClientReceiver.unregisterBluetoothBondListener(listener);
    }

    private void checkRuntime(boolean async) {
        Looper targetLooper = async ? mWorkerHandler.getLooper() : Looper.getMainLooper();
        if (Looper.myLooper() != targetLooper) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INVOKE_PROXY:
                ProxyBulk.safeInvoke(msg.obj);
                return true;
        }
        return false;
    }
}
