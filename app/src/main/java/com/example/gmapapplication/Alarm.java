package com.example.gmapapplication;

import android.app.Activity;
import android.content.Intent;
import android.provider.AlarmClock;
import android.view.View;

public class Alarm {

    public static void setAlarm(int targetHour, int targetMinute)
    {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);

        intent.putExtra(AlarmClock.EXTRA_HOUR, targetHour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, targetMinute);

        Activity activity = new Activity();
        activity.startActivity(intent);
    }

}
