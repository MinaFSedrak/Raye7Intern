package minasedrak.ray7;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MinaSedrak on 6/11/2017.
 */

public class DateTime {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;



    private boolean dateHasBeenSetSuccessfully;
    private boolean timeHasBeenSetSuccessfully;


    public DateTime(){

        dateHasBeenSetSuccessfully = false;
        timeHasBeenSetSuccessfully = false;

    }





    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isDateHasBeenSetSuccessfully() {
        return dateHasBeenSetSuccessfully;
    }

    public void setDateHasBeenSetSuccessfully(boolean dateHasBeenSetSuccessfully) {
        this.dateHasBeenSetSuccessfully = dateHasBeenSetSuccessfully;
    }

    public boolean isTimeHasBeenSetSuccessfully() {
        return timeHasBeenSetSuccessfully;
    }

    public void setTimeHasBeenSetSuccessfully(boolean timeHasBeenSetSuccessfully) {
        this.timeHasBeenSetSuccessfully = timeHasBeenSetSuccessfully;
    }



}
