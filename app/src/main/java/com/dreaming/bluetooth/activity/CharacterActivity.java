package com.dreaming.bluetooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dreaming.bluetooth.utils.CommonUtils;
import com.dreaming.bluetooth.R;
import com.dreaming.bluetooth.framework.BluetoothClient;
import com.dreaming.bluetooth.framework.connect.BleConnectStatus;
import com.dreaming.bluetooth.framework.connect.listener.BleConnectStatusListener;
import com.dreaming.bluetooth.framework.connect.response.BleMtuResponse;
import com.dreaming.bluetooth.framework.connect.response.BleNotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleReadResponse;
import com.dreaming.bluetooth.framework.connect.response.BleUnnotifyResponse;
import com.dreaming.bluetooth.framework.connect.response.BleWriteResponse;
import com.dreaming.bluetooth.framework.utils.ByteUtils;

import static com.dreaming.bluetooth.framework.Constants.*;
import static com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode.*;

import java.util.UUID;

public class CharacterActivity extends Activity implements View.OnClickListener {

    private String mMac;
    private UUID mService;
    private UUID mCharacter;

    private TextView mTvTitle;

    private Button mBtnRead;

    private Button mBtnWrite;
    private EditText mEtInput;

    private Button mBtnNotify;
    private Button mBtnUnnotify;
    private EditText mEtInputMtu;
    private Button mBtnRequestMtu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_activity);

        Intent intent = getIntent();
        mMac = intent.getStringExtra("mac");
        mService = (UUID) intent.getSerializableExtra("service");
        mCharacter = (UUID) intent.getSerializableExtra("character");
        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(String.format("%s", mMac));

        mBtnRead = (Button) findViewById(R.id.read);

        mBtnWrite = (Button) findViewById(R.id.write);
        mEtInput = (EditText) findViewById(R.id.input);

        mBtnNotify = (Button) findViewById(R.id.notify);
        mBtnUnnotify = (Button) findViewById(R.id.unnotify);

        mEtInputMtu = (EditText) findViewById(R.id.et_input_mtu);
        mBtnRequestMtu = (Button) findViewById(R.id.btn_request_mtu);

        mBtnRead.setOnClickListener(this);
        mBtnWrite.setOnClickListener(this);

        mBtnNotify.setOnClickListener(this);
        mBtnNotify.setEnabled(true);

        mBtnUnnotify.setOnClickListener(this);
        mBtnUnnotify.setEnabled(false);

        mBtnRequestMtu.setOnClickListener(this);
    }

    private final BleReadResponse mReadRsp = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
            if (code == Success.code) {
                mBtnRead.setText(String.format("read: %s", ByteUtils.byteToString(data)));
                CommonUtils.toast("success");
            } else {
                CommonUtils.toast("failed");
                mBtnRead.setText("read");
            }
        }
    };

    private final BleWriteResponse mWriteRsp = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {
            if (code == Success.code) {
                CommonUtils.toast("success");
            } else {
                CommonUtils.toast("failed");
            }
        }
    };

    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (service.equals(mService) && character.equals(mCharacter)) {
                mBtnNotify.setText(String.format("%s", ByteUtils.byteToString(value)));
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == Success.code) {
                mBtnNotify.setEnabled(false);
                mBtnUnnotify.setEnabled(true);
                CommonUtils.toast("success");
            } else {
                CommonUtils.toast("failed");
            }
        }
    };

    private final BleUnnotifyResponse mUnnotifyRsp = new BleUnnotifyResponse() {
        @Override
        public void onResponse(int code) {
            if (code == Success.code) {
                CommonUtils.toast("success");
                mBtnNotify.setEnabled(true);
                mBtnUnnotify.setEnabled(false);
            } else {
                CommonUtils.toast("failed");
            }
        }
    };

    private final BleMtuResponse mMtuResponse = new BleMtuResponse() {
        @Override
        public void onResponse(int code, Integer data) {
            if (code == Success.code) {
                CommonUtils.toast("request mtu success,mtu = " + data);
            } else {
                CommonUtils.toast("request mtu failed");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.read:
                BluetoothClient.getInstance().read(mMac, mService, mCharacter, mReadRsp);
                break;
            case R.id.write:
                BluetoothClient.getInstance().write(mMac, mService, mCharacter,
                        ByteUtils.stringToBytes(mEtInput.getText().toString()), mWriteRsp);
                break;
            case R.id.notify:
                BluetoothClient.getInstance().notify(mMac, mService, mCharacter, mNotifyRsp);
                break;
            case R.id.unnotify:
                BluetoothClient.getInstance().unnotify(mMac, mService, mCharacter, mUnnotifyRsp);
                break;
            case R.id.btn_request_mtu:
                String mtuStr = mEtInputMtu.getText().toString();
                if (TextUtils.isEmpty(mtuStr)) {
                    CommonUtils.toast("MTU不能为空");
                    return;
                }
                int mtu = Integer.parseInt(mtuStr);
                if (mtu < GATT_DEF_BLE_MTU_SIZE || mtu > GATT_MAX_MTU_SIZE) {
                    CommonUtils.toast("MTU不不在范围内");
                    return;
                }
                BluetoothClient.getInstance().requestMtu(mMac, mtu, mMtuResponse);
                break;
        }
    }

    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            Log.v("CharacterActivity", String.format("CharacterActivity.onConnectStatusChanged status = %d", status));

            if (status == BleConnectStatus.Disconnected.status) {
                CommonUtils.toast("disconnected");
                mBtnRead.setEnabled(false);
                mBtnWrite.setEnabled(false);
                mBtnNotify.setEnabled(false);
                mBtnUnnotify.setEnabled(false);

                mTvTitle.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        finish();
                    }
                }, 300);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothClient.getInstance().registerConnectStatusListener(mMac, mConnectStatusListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothClient.getInstance().unregisterConnectStatusListener(mMac, mConnectStatusListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
