package com.example.myapplication;

import java.io.Serializable;

public class TimeInfo implements Serializable {
    private int hour;
    private int minute;

    public TimeInfo(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}
