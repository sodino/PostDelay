package com.sodino.postdelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

/**
 * Created by sodino on 2017/6/16.
 */

public class CodeAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getName(), "onReceive code " + Constant.DF.format(new Date(System.currentTimeMillis())));
        TestAlarmService.getDirtyInstance().nextCodeAlarm();
    }
}
