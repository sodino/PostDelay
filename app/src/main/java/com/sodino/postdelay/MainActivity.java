package com.sodino.postdelay;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {
    private static final int DO_BG_TASK = 1;
    private static final int DO_UI_TASK = 2;

    private static final int DURATION = 1000;

    HandlerThread bThread = new  HandlerThread("backendHandler_thread", Process.THREAD_PRIORITY_BACKGROUND);
    Handler backHandler;
    Handler uiHandler = new Handler(this);
    int bgCount = 0;
    int uiCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnStartCount).setOnClickListener(this);
        findViewById(R.id.btnStopCount).setOnClickListener(this);
        findViewById(R.id.btnIgnorePower).setOnClickListener(this);
        findViewById(R.id.btnLaunchPower).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnStartCount:{
                if (backHandler == null) {
                    bThread.start();
                    backHandler = new Handler(bThread.getLooper(), this);
                }
                backHandler.sendEmptyMessageDelayed(DO_BG_TASK, DURATION);
                uiHandler.sendEmptyMessageDelayed(DO_UI_TASK, DURATION);
                v.setVisibility(View.GONE);
                findViewById(R.id.btnStopCount).setVisibility(View.VISIBLE);

                Intent intent = new Intent(this, TestAlarmService.class);
                startService(intent);
            }break;
            case R.id.btnStopCount: {
                v.setVisibility(View.GONE);
                findViewById(R.id.btnStartCount).setVisibility(View.VISIBLE);
            }break;
            case R.id.btnIgnorePower:{
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }break;
            case R.id.btnLaunchPower:{
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            }break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        int count = 0;
        int diff = uiCount - bgCount;
        switch(msg.what) {
            case DO_UI_TASK:{
                count = uiCount;
                Log.d("BgTest", "uiCount " + count
                        + (diff > 0 ? ("  +" + diff) : "")
                );
                uiHandler.sendEmptyMessageDelayed(DO_UI_TASK, DURATION);

                uiCount ++;
            }break;
            case DO_BG_TASK:{
                count = bgCount;
                Log.d("BgTest", "bgCount " + count
                        + (diff < 0 ? ("  +" + Math.abs(diff)) : "")
                );
                backHandler.sendEmptyMessageDelayed(DO_BG_TASK, DURATION);

                bgCount ++;
            }break;
        }
        return false;
    }
}
