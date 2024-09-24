package com.dreaming.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dreaming.bluetooth.framework.BluetoothClient;
import com.dreaming.bluetooth.framework.connect.BleConnectStatus;
import com.dreaming.bluetooth.framework.connect.listener.BleConnectStatusListener;
import com.dreaming.bluetooth.framework.connect.options.BleConnectOptions;
import com.dreaming.bluetooth.framework.connect.response.BleConnectResponse;
import com.dreaming.bluetooth.framework.model.BleGattProfile;
import com.dreaming.bluetooth.framework.search.SearchResult;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;

import static com.dreaming.bluetooth.framework.connect.response.BluetoothApiResponseCode.*;

import java.util.UUID;


public class DeviceDetailActivity extends Activity {

    private TextView mTvTitle;
    private ProgressBar mPbar;

    private ListView mListView;
    private DeviceDetailAdapter mAdapter;

    private SearchResult mResult;

    private BluetoothDevice mDevice;

    private boolean mConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail_activity);

        Intent intent = getIntent();
        String mac = intent.getStringExtra("mac");
        mResult = intent.getParcelableExtra("device");

        mDevice = BluetoothUtils.getRemoteDevice(mac);

        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(mDevice.getAddress());

        mPbar = (ProgressBar) findViewById(R.id.pbar);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new DeviceDetailAdapter(this, mDevice);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mConnected) {
                    return;
                }
                DetailItem item = (DetailItem) mAdapter.getItem(position);
                if (item.type == DetailItem.TYPE_CHARACTER) {
                    Log.v("DeviceDetailActivity", String.format("click service = %s, character = %s", item.service, item.uuid));
                    startCharacterActivity(item.service, item.uuid);
                }
            }
        });

        BluetoothClient.getInstance().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);

        connectDeviceIfNeeded();
    }

    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            Log.v("DeviceDetailActivity", String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == BleConnectStatus.Connected.status);
            connectDeviceIfNeeded();
        }
    };

    private void startCharacterActivity(UUID service, UUID character) {
        Intent intent = new Intent(this, CharacterActivity.class);
        intent.putExtra("mac", mDevice.getAddress());
        intent.putExtra("service", service);
        intent.putExtra("character", character);
        startActivity(intent);
    }

    private void connectDevice() {
        mTvTitle.setText(String.format("%s%s", getString(R.string.connecting), mDevice.getAddress()));
        mPbar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        BluetoothClient.getInstance().connect(mDevice.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                Log.v("DeviceDetailActivity", String.format("profile:\n%s", profile));
                mTvTitle.setText(String.format("%s", mDevice.getAddress()));
                mPbar.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);

                if (code == Success.code) {
                    mAdapter.setGattProfile(profile);
                }
            }
        });
    }

    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    @Override
    protected void onDestroy() {
        BluetoothClient.getInstance().disconnect(mDevice.getAddress());
        BluetoothClient.getInstance().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        super.onDestroy();
    }
}
