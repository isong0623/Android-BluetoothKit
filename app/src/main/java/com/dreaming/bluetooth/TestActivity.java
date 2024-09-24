package com.dreaming.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        Log.v("TestActivity","");
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, TestActivity1.class);
                startActivity(intent);
                Log.v("TestActivity","finish");
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("TestActivity","onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("TestActivity","onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("TestActivity","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("TestActivity","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("TestActivity","onDestroy");
    }
}
