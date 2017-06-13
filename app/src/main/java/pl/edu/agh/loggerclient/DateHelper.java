package pl.edu.agh.loggerclient;

import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Magda on 2017-06-10.
 */

public class DateHelper {
    public static final String LAST_LOG_DATE_TAG = "LastLogDate";
    public static final String DATE_PATTERN = "MM-dd HH:mm:ss.S";

    public static String getCurrentStringDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(new Date());
    }

    public static void saveCurrentDate(SharedPreferences preferences) {
        String current = DateHelper.getCurrentStringDate();
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(DateHelper.LAST_LOG_DATE_TAG, current);
        edit.commit();
    }

    public static String getLastSavedDateOrDefault(SharedPreferences preferences) {
        String LastLogDate = preferences.getString(DateHelper.LAST_LOG_DATE_TAG, "");

        if(LastLogDate.isEmpty()) {
            LastLogDate = DateHelper.getCurrentStringDate();
        }

        return LastLogDate;
    }
}
