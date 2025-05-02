package com.example.gmapapplication;

import java.util.Calendar;

public class Now {

    public final int HOUR_NOW = getCurrentHour();
    public final int MINUTE_NOW = getCurrentMinutes();


    public int getCurrentHour()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getCurrentMinutes(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MINUTE);
    }
}
