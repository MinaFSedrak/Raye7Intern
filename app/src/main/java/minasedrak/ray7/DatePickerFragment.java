package minasedrak.ray7;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    int mYear;
    int mMonth;
    int mDay;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog
        return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);

    }


    // When Dialog Box is closed ,this method is called
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        boolean oldDate = false;

        if( year < mYear){
            oldDate = true;}

        if( month < mMonth && year == mYear){
            oldDate = true;}

        if ( dayOfMonth < mDay && month == mMonth && year == mYear){
            oldDate = true;}

        if ( oldDate == false){

            // Date has been set successfully
            ((MainActivity)getActivity()).mDateTime.setDateHasBeenSetSuccessfully(true);
            ((MainActivity)getActivity()).mDateTime.setYear(mYear);
            ((MainActivity)getActivity()).mDateTime.setMonth(mMonth);
            ((MainActivity)getActivity()).mDateTime.setDay(mDay);

            // Create TimePickerFragment
            DialogFragment timeFragment = new TimePickerFragment();
            timeFragment.show(getFragmentManager(), "Time Picker");

        } else {
            Toast.makeText(getContext(), "Old date can't be set", Toast.LENGTH_LONG).show();
        }


    }


}
