package com.sodino.postdelay;

import android.util.Log;

import java.util.Date;
import java.util.TimerTask;

/**
 * Created by sodino on 2017/6/18.
 */

public class CountTask extends TimerTask {
    @Override
    public void run() {
        Log.d(getClass().getName(), "call CountTask at "
                + Constant.DF.format(new Date(System.currentTimeMillis())));

        TestAlarmService.getDirtyInstance().scheduleTimer();
    }
}
