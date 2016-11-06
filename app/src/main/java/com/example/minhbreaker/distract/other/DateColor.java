package com.example.minhbreaker.distract.other;

import com.stacktips.view.DayDecorator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MinhBreaker on 6/11/16.
 */
public class DateColor {
    String date;
    int color;
    public DateColor (String date, int color) {
        this.date = date;
        this.color = color;
    }

    public String getDate() {
        return date;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String res = "";
        res += date+String.valueOf(color);
        return res;
    }
}
