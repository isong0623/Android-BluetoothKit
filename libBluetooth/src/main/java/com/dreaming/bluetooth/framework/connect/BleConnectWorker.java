package com.dreaming.bluetooth.framework.connect;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dreaming.bluetooth.framework.RuntimeChecker;
import com.dreaming.bluetooth.framework.connect.listener.GattResponseListener;
import com.dreaming.bluetooth.framework.connect.listener.IBluetoothGattResponse;
import com.dreaming.bluetooth.framework.connect.listener.ReadCharacterListener;
import com.dreaming.bluetooth.framework.connect.listener.ReadDescriptorListener;
import com.dreaming.bluetooth.framework.connect.listener.ReadRssiListener;
import com.dreaming.bluetooth.framework.connect.listener.RequestMtuListener;
import com.dreaming.bluetooth.framework.connect.listener.ServiceDiscoverListener;
import com.dreaming.bluetooth.framework.connect.listener.WriteCharacterListener;
import com.dreaming.bluetooth.framework.connect.listener.WriteDescriptorListener;
import com.dreaming.bluetooth.framework.connect.response.BluetoothGattResponse;
import com.dreaming.bluetooth.framework.model.BleGattProfile;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;
import com.dreaming.bluetooth.framework.utils.ByteUtils;
import com.dreaming.bluetooth.framework.utils.UUIDUtils;
import com.dreaming.bluetooth.framework.utils.Version;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyBulk;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyInterceptor;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleConnectWorker implements Handler.Callback, IBleConnectWorker, IBluetoothGattResponse, ProxyInterceptor, RuntimeChecker {
    private static BluetoothLogger logger = new BluetoothLogger(BleConnectWorker.class);
    private static final int MSG_GATT_RESPONSE = 0x120;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;

    private GattResponseListener mGattResponseListener;

    private Handler mWorkerHandler;

    private volatile BluetoothDeviceState mConnectStatus;

    private BleGattProfile mBleGattProfile;
    private Map<UUID, Map<UUID, BluetoothGattCharacteristic>> mDeviceProfile;

    private IBluetoothGattResponse mBluetoothGattResponse;

    private RuntimeChecker mRuntimeChecker;

    public BleConnectWorker(String mac, RuntimeChecker runtimeChecker) {
        BluetoothAdapter adapter = BluetoothUtils.getBluetoothAdapter();
        if (adapter != null) {
            mBluetoothDevice = adapter.getRemoteDevice(mac);
        } else {
            throw new IllegalStateException("ble adapter null");
        }

        mRuntimeChecker = runtimeChecker;
        mWorkerHandler = new Handler(Looper.myLooper(), this);
        mDeviceProfile = new HashMap<UUID, Map<UUID, BluetoothGattCharacteristic>>();
        mBluetoothGattResponse = ProxyUtils.getProxy(this, IBluetoothGattResponse.class, this);
    }

    private void refreshServiceProfile() {
        logger.v("refreshServiceProfile for %s", mBluetoothDevice.getAddress());

        List<BluetoothGattService> services = mBluetoothGatt.getServices();

        Map<UUID, Map<UUID, BluetoothGattCharacteristic>> newProfiles = new HashMap<UUID, Map<UUID, BluetoothGattCharacteristic>>();

        for (BluetoothGattService service : services) {
            UUID serviceUUID = service.getUuid();

            Map<UUID, BluetoothGattCharacteristic> map = newProfiles.get(serviceUUID);

            if (map == null) {
                logger.v("Service: " + serviceUUID);
                map = new HashMap<UUID, BluetoothGattCharacteristic>();
                newProfiles.put(service.getUuid(), map);
            }

            List<BluetoothGattCharacteristic> characters = service
                    .getCharacteristics();

            for (BluetoothGattCharacteristic character : characters) {
                UUID characterUUID = character.getUuid();
                logger.v("character: uuid = " + characterUUID);
                map.put(character.getUuid(), character);
            }
        }

        mDeviceProfile.clear();
        mDeviceProfile.putAll(newProfiles);
        mBleGattProfile = new BleGattProfile(mDeviceProfile);
    }

    private BluetoothGattCharacteristic getCharacter(UUID service, UUID character) {
        BluetoothGattCharacteristic characteristic = null;

        if (service != null && character != null) {
            Map<UUID, BluetoothGattCharacteristic> characters = mDeviceProfile.get(service);
            if (characters != null) {
                characteristic = characters.get(character);
            }
        }

        if (characteristic == null) {
            if (mBluetoothGatt != null) {
                BluetoothGattService gattService = mBluetoothGatt.getService(service);
                if (gattService != null) {
                    characteristic = gattService.getCharacteristic(character);
                }
            }
        }

        return characteristic;
    }

    private void setConnectStatus(BluetoothDeviceState status) {
        logger.v("setConnectStatus status = %s", status.toString());
        mConnectStatus = status;
    }

    @Override
    public void onConnectionStateChange(int status, int newState) {
        checkRuntime();

        logger.v("onConnectionStateChange for %s: status = %d, newState = %d",
                mBluetoothDevice.getAddress(), status, newState);

        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            setConnectStatus(BluetoothDeviceState.Connected);

            if (mGattResponseListener != null) {
                mGattResponseListener.onConnectStatusChanged(true);
            }
        } else {
            closeGatt();
        }
    }

    @Override
    public void onServicesDiscovered(int status) {
        checkRuntime();

        logger.v("onServicesDiscovered for %s: status = %d",
                mBluetoothDevice.getAddress(), status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            setConnectStatus(BluetoothDeviceState.ServiceReady);
            broadcastConnectStatus(BleConnectStatus.Connected.status);
            refreshServiceProfile();
        }

        if (mGattResponseListener != null && mGattResponseListener instanceof ServiceDiscoverListener) {
            ((ServiceDiscoverListener) mGattResponseListener).onServicesDiscovered(status, mBleGattProfile);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status, byte[] value) {
        checkRuntime();

        logger.v("onCharacteristicRead for %s: status = %d, service = 0x%s, character = 0x%s, value = %s",
                mBluetoothDevice.getAddress(),
                status,
                characteristic.getService().getUuid(),
                characteristic.getUuid(),
                ByteUtils.byteToString(value)
        );

        if (mGattResponseListener != null && mGattResponseListener instanceof ReadCharacterListener) {
            ((ReadCharacterListener) mGattResponseListener).onCharacteristicRead(characteristic, status, value);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status, byte[] value) {
        checkRuntime();

        logger.v("onCharacteristicWrite for %s: status = %d, service = 0x%s, character = 0x%s, value = %s",
                mBluetoothDevice.getAddress(),
                status,
                characteristic.getService().getUuid(),
                characteristic.getUuid(),
                ByteUtils.byteToString(value));

        if (mGattResponseListener != null && mGattResponseListener instanceof WriteCharacterListener) {
            ((WriteCharacterListener) mGattResponseListener).onCharacteristicWrite(characteristic, status, value);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        checkRuntime();

        logger.v("onCharacteristicChanged for %s: value = %s, service = 0x%s, character = 0x%s",
                mBluetoothDevice.getAddress(),
                ByteUtils.byteToString(value),
                characteristic.getService().getUuid(),
                characteristic.getUuid()
        );

        broadcastCharacterChanged(characteristic.getService().getUuid(), characteristic.getUuid(), value);
    }

    @Override
    public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status, byte[] value) {
        checkRuntime();

        logger.v("onDescriptorRead for %s: status = %d, service = 0x%s, character = 0x%s, descriptor = 0x%s",
                mBluetoothDevice.getAddress(),
                status,
                descriptor.getCharacteristic().getService().getUuid(),
                descriptor.getCharacteristic().getUuid(),
                descriptor.getUuid()
        );

        if (mGattResponseListener != null && mGattResponseListener instanceof ReadDescriptorListener) {
            ((ReadDescriptorListener) mGattResponseListener).onDescriptorRead(descriptor, status, value);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        checkRuntime();

        logger.v("onDescriptorWrite for %s: status = %d, service = 0x%s, character = 0x%s, descriptor = 0x%s",
                mBluetoothDevice.getAddress(),
                status,
                descriptor.getCharacteristic().getService().getUuid(),
                descriptor.getCharacteristic().getUuid(),
                descriptor.getUuid()
        );

        if (mGattResponseListener != null && mGattResponseListener instanceof WriteDescriptorListener) {
            ((WriteDescriptorListener) mGattResponseListener).onDescriptorWrite(descriptor, status);
        }
    }

    @Override
    public void onReadRemoteRssi(int rssi, int status) {
        checkRuntime();

        logger.v("onReadRemoteRssi for %s, rssi = %d, status = %d",
                mBluetoothDevice.getAddress(), rssi, status);

        if (mGattResponseListener != null && mGattResponseListener instanceof ReadRssiListener) {
            ((ReadRssiListener) mGattResponseListener).onReadRemoteRssi(rssi, status);
        }
    }

    @Override
    public void onMtuChanged(int mtu, int status) {
        checkRuntime();

        logger.v("onMtuChanged for %s, mtu = %d, status = %d",
                mBluetoothDevice.getAddress(), mtu, status);

        if (mGattResponseListener != null && mGattResponseListener instanceof RequestMtuListener) {
            ((RequestMtuListener) mGattResponseListener).onMtuChanged(mtu, status);
        }
    }

    private void broadcastConnectStatus(int status) {
        Intent intent = new Intent(BluetoothAction.ConnectStatusChanged.toString());
        intent.putExtra(BluetoothExtra.Mac.toString(), mBluetoothDevice.getAddress());
        intent.putExtra(BluetoothExtra.Status.toString(), status);
        BluetoothUtils.sendBroadcast(intent);
    }

    private void broadcastCharacterChanged(UUID service, UUID character,
    byte[] value) {
        Intent intent = new Intent(BluetoothAction.CharacterChanged.toString());
        intent.putExtra(BluetoothExtra.Mac.toString(), mBluetoothDevice.getAddress());
        intent.putExtra(BluetoothExtra.ServiceUuid.toString(), service);
        intent.putExtra(BluetoothExtra.CharacterUuid.toString(), character);
        intent.putExtra(BluetoothExtra.ByteValue.toString(), value);
        BluetoothUtils.sendBroadcast(intent);
    }

    @Override
    public boolean openGatt() {
        checkRuntime();

        logger.v("openGatt for %s", getAddress());

        if (mBluetoothGatt != null) {
            logger.e("Previous gatt not closed");
            return true;
        }

        Context context = BluetoothUtils.getContext();
        BluetoothGattCallback callback = new BluetoothGattResponse(mBluetoothGattResponse);

        if (Version.isMarshmallow()) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, callback);
        }

        if (mBluetoothGatt == null) {
            logger.e("openGatt failed: connectGatt return null!");
            return false;
        }

        return true;
    }

    private String getAddress() {
        return mBluetoothDevice.getAddress();
    }

    @Override
    public void closeGatt() {
        checkRuntime();

        logger.v("closeGatt for %s", getAddress());

        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        if (mGattResponseListener != null) {
            mGattResponseListener.onConnectStatusChanged(false);
        }

        setConnectStatus(BluetoothDeviceState.Disconnected);
        broadcastConnectStatus(BleConnectStatus.Disconnected.status);
    }

    @Override
    public boolean discoverService() {
        checkRuntime();

        logger.v("discoverService for %s", getAddress());

        if (mBluetoothGatt == null) {
            logger.e("discoverService but gatt is null!");
            return false;
        }

        if (!mBluetoothGatt.discoverServices()) {
            logger.e("discoverServices failed");
            return false;
        }

        return true;
    }

    @Override
    public BluetoothDeviceState getCurrentStatus() {
        checkRuntime();
        return mConnectStatus;
    }

    @Override
    public void registerGattResponseListener(GattResponseListener listener) {
        checkRuntime();
        mGattResponseListener = listener;
    }

    @Override
    public void clearGattResponseListener(GattResponseListener listener) {
        checkRuntime();

        if (mGattResponseListener == listener) {
            mGattResponseListener = null;
        }
    }

    @Override
    public boolean refreshDeviceCache() {
        logger.v("refreshDeviceCache for %s", getAddress());

        checkRuntime();

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!BluetoothUtils.refreshGattCache(mBluetoothGatt)) {
            logger.e("refreshDeviceCache failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean readCharacteristic(UUID service, UUID character) {
        logger.v("readCharacteristic for %s: service = 0x%s, character = 0x%s",
                mBluetoothDevice.getAddress(), service, character);

        checkRuntime();

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

//        if (!isCharacteristicReadable(characteristic)) {
//            logger.e("characteristic not readable!");
//            return false;
//        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!mBluetoothGatt.readCharacteristic(characteristic)) {
            logger.e("readCharacteristic failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean writeCharacteristic(UUID service, UUID character, byte[] value) {
        logger.v("writeCharacteristic for %s: service = 0x%s, character = 0x%s, value = 0x%s",
                mBluetoothDevice.getAddress(), service, character, ByteUtils.byteToString(value));

        checkRuntime();

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

//        if (!isCharacteristicWritable(characteristic)) {
//            logger.e("characteristic not writable!");
//            return false;
//        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        characteristic.setValue(value != null ? value : ByteUtils.EMPTY_BYTES);

        if (!mBluetoothGatt.writeCharacteristic(characteristic)) {
            logger.e("writeCharacteristic failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean readDescriptor(UUID service, UUID character, UUID descriptor) {
        logger.v("readDescriptor for %s: service = 0x%s, character = 0x%s, descriptor = 0x%s",
                mBluetoothDevice.getAddress(), service, character, descriptor);

        checkRuntime();

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

        BluetoothGattDescriptor gattDescriptor = characteristic.getDescriptor(descriptor);
        if (gattDescriptor == null) {
            logger.e("descriptor not exist");
            return false;
        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!mBluetoothGatt.readDescriptor(gattDescriptor)) {
            logger.e("readDescriptor failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean writeDescriptor(UUID service, UUID character, UUID descriptor, byte[] value) {
        logger.v("writeDescriptor for %s: service = 0x%s, character = 0x%s, descriptor = 0x%s, value = 0x%s",
                mBluetoothDevice.getAddress(), service, character, descriptor, ByteUtils.byteToString(value));

        checkRuntime();

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

        BluetoothGattDescriptor gattDescriptor = characteristic.getDescriptor(descriptor);
        if (gattDescriptor == null) {
            logger.e("descriptor not exist");
            return false;
        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        gattDescriptor.setValue(value != null ? value : ByteUtils.EMPTY_BYTES);

        if (!mBluetoothGatt.writeDescriptor(gattDescriptor)) {
            logger.e("writeDescriptor failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean writeCharacteristicWithNoRsp(UUID service, UUID character, byte[] value) {
        logger.v("writeCharacteristicWithNoRsp for %s: service = 0x%s, character = 0x%s, value = 0x%s",
                mBluetoothDevice.getAddress(), service, character, ByteUtils.byteToString(value));

        checkRuntime();

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

//        if (!isCharacteristicNoRspWritable(characteristic)) {
//            logger.e("characteristic not norsp writable!");
//            return false;
//        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        characteristic.setValue(value != null ? value : ByteUtils.EMPTY_BYTES);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

        if (!mBluetoothGatt.writeCharacteristic(characteristic)) {
            logger.e("writeCharacteristic failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean setCharacteristicNotification(UUID service, UUID character, boolean enable) {
        checkRuntime();

        logger.v("setCharacteristicNotification for %s, service = %s, character = %s, enable = %b",
                getAddress(), service, character, enable);

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

//        if (!isCharacteristicNotifyable(characteristic)) {
//            logger.e("characteristic not notifyable!");
//            return false;
//        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
            logger.e("setCharacteristicNotification failed");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);

        if (descriptor == null) {
            logger.e("getDescriptor for notify null!");
            return false;
        }

        byte[] value = (enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!descriptor.setValue(value)) {
            logger.e("setValue for notify descriptor failed!");
            return false;
        }

        if (!mBluetoothGatt.writeDescriptor(descriptor)) {
            logger.e("writeDescriptor for notify failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean setCharacteristicIndication(UUID service, UUID character, boolean enable) {
        checkRuntime();

        logger.v("setCharacteristicIndication for %s, service = %s, character = %s, enable = %b",
                    getAddress(), service, character, enable);

        BluetoothGattCharacteristic characteristic = getCharacter(service, character);

        if (characteristic == null) {
            logger.e("characteristic not exist!");
            return false;
        }

//        if (!isCharacteristicIndicatable(characteristic)) {
//            logger.e("characteristic not indicatable!");
//            return false;
//        }

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
            logger.e("setCharacteristicIndication failed");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);

        if (descriptor == null) {
            logger.e("getDescriptor for indicate null!");
            return false;
        }

        byte[] value = (enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!descriptor.setValue(value)) {
            logger.e("setValue for indicate descriptor failed!");
            return false;
        }

        if (!mBluetoothGatt.writeDescriptor(descriptor)) {
            logger.e("writeDescriptor for indicate failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean readRemoteRssi() {
        checkRuntime();

        logger.v("readRemoteRssi for %s", getAddress());

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!mBluetoothGatt.readRemoteRssi()) {
            logger.e("readRemoteRssi failed");
            return false;
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean requestMtu(int mtu) {
        checkRuntime();

        logger.v("requestMtu for %s, mtu = %d", getAddress(), mtu);

        if (mBluetoothGatt == null) {
            logger.e("ble gatt null");
            return false;
        }

        if (!mBluetoothGatt.requestMtu(mtu)) {
            logger.e("requestMtu failed");
            return false;
        }
        return true;
    }

    @Override
    public BleGattProfile getGattProfile() {
        return mBleGattProfile;
    }

    private boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    private boolean isCharacteristicWritable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    private boolean isCharacteristicNoRspWritable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }

    private boolean isCharacteristicNotifyable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    private boolean isCharacteristicIndicatable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_GATT_RESPONSE:
                ProxyBulk.safeInvoke(msg.obj);
                break;
        }
        return true;
    }

    @Override
    public boolean onIntercept(Object object, Method method, Object[] args) {
        mWorkerHandler.obtainMessage(MSG_GATT_RESPONSE,
                new ProxyBulk(object, method, args)).sendToTarget();
        return true;
    }

    @Override
    public void checkRuntime() {
        mRuntimeChecker.checkRuntime();
    }
}
