package com.sodino.postdelay;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.Timer;

/**
 * Created by sodino on 2017/6/16.
 */

public class TestAlarmService extends Service {

    private static TestAlarmService DIRTY_INSTANCE;

    private PendingIntent intentXml;
    private PendingIntent intentXmlRepeating;
    private PendingIntent intentCode;

    private CodeAlarmReceiver codeReceiver;

    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DIRTY_INSTANCE = this;
        Log.d(getClass().getName(), "onCreate()");

        registerXmlAlarmRepeating();
        registerXmlAlarm();
        registerCode();

        scheduleTimer();
    }

    public void scheduleTimer() {
        if (timer == null) {
            timer = new Timer();
        }

        timer.schedule(new CountTask(), Constant.DURATION);
        Log.d(getClass().getName(),
                "schedule next CountTask at "
                        + Constant.DF.format(new Date(System.currentTimeMillis() + Constant.DURATION)));
    }

    private void registerCode() {
        if (codeReceiver == null) {
            codeReceiver = new CodeAlarmReceiver();
        }

        registerReceiver(codeReceiver, new IntentFilter(Constant.ACTION_RECEIVER_CODE));

        Intent intent = new Intent(Constant.ACTION_RECEIVER_CODE);
        int requestCode = 0;
        intentCode = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        schedule(intentCode, Constant.DURATION);
    }

    private void unregisterCode() {
        if (codeReceiver != null) {
            unregisterReceiver(codeReceiver);
        }

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(intentCode);
    }


    public void schedule(PendingIntent pending, long delayInMilliseconds) {
        String name = "unknown";
        if (pending == intentXml) {
            name = "intentXml";
        } else {
            name = "intentCode";
        }


        long nextAlarmInMilliseconds = System.currentTimeMillis() + delayInMilliseconds;
        Log.d(getClass().getName(), "Schedule " + name + " alarm at "
                + Constant.DF.format(new Date(nextAlarmInMilliseconds)));

        AlarmManager alarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= 23){
            // In SDK 23 and above, dosing will prevent setExact, setExactAndAllowWhileIdle will force
            // the device to run this task whilst dosing.
            Log.d(getClass().getName(), "Alarm scheule using setExactAndAllowWhileIdle");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    nextAlarmInMilliseconds, pending);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pending);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pending);
        }
    }

    private void registerXmlAlarm() {
        Log.d(getClass().getName(), "registerXmlAlarm()");
        if (intentXml == null) {
            Intent intent = new Intent(Constant.ACTION_RECEIVER_XML);
            int requestCode = 0;
            intentXml = PendingIntent.getBroadcast(this, requestCode,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        schedule(intentXml, Constant.DURATION);
    }

    private void unregisterXmlAlarm() {
        Log.d(getClass().getName(), "unregisterXmlAlarm()");

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(intentXml);
    }

    private void registerXmlAlarmRepeating() {
        Log.d(getClass().getName(), "registerXmlAlarmRepeating()");
        if (intentXmlRepeating == null) {
            Intent intent = new Intent(Constant.ACTION_RECEIVER_XML_REPEATING);
            int requestCode = 0;
            intentXmlRepeating = PendingIntent.getBroadcast(this, requestCode,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + Constant.DURATION
                , Constant.DURATION, intentXmlRepeating);
    }

    private void unregisterXmlAlarmRepeating() {
        Log.d(getClass().getName(), "unregisterXmlAlarmRepeating()");

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(intentXmlRepeating);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(getClass().getName(), "onDestroy()");

        unregisterXmlAlarmRepeating();
        unregisterXmlAlarm();
        unregisterCode();

        DIRTY_INSTANCE = null;
    }


    public static TestAlarmService getDirtyInstance() {
        return DIRTY_INSTANCE;
    }

    public void nextXmlAlarm() {
        Log.d(getClass().getName(), "nextXmlAlarm");
        schedule(intentXml, Constant.DURATION);
    }

    public void nextCodeAlarm() {
        Log.d(getClass().getName(), "nextCodeAlarm");
        schedule(intentCode, Constant.DURATION);
    }
}
