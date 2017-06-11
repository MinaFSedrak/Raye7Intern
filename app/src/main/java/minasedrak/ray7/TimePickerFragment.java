package minasedrak.ray7;


import android.app.Dialog;
import android.app.TimePickerDialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    int mHour;
    int mMinute;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final Calendar c = Calendar.getInstance();

        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog
        return new TimePickerDialog(getActivity(), this, mHour, mMinute, android.text.format.DateFormat.is24HourFormat(getActivity()));

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        boolean oldTime = false;

        if( hourOfDay < mHour){
            oldTime = true;}

        if ( minute < mMinute && hourOfDay == mHour){
            oldTime = true;}

        if ( oldTime == false){

            // Time has been set successfuly
            ((MainActivity)getActivity()).mDateTime.setTimeHasBeenSetSuccessfully(true);
            ((MainActivity)getActivity()).mDateTime.setHour(mHour);
            ((MainActivity)getActivity()).mDateTime.setMinute(mMinute);

            // Display Chosen Time And Date For user
            ((MainActivity)getActivity()).showDateAndTime();

        }else {
            Toast.makeText(getContext(), "Old time can't be set", Toast.LENGTH_SHORT).show();

        }

    }



    // Setters & Getters of Hour - Minute
    public int getmHour() {
        return mHour;
    }

    public void setmHour(int mHour) {
        this.mHour = mHour;
    }

    public int getmMinute() {
        return mMinute;
    }

    public void setmMinute(int mMinute) {
        this.mMinute = mMinute;
    }

}
