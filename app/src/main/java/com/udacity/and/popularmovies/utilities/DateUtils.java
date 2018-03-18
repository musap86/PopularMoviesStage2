package com.udacity.and.popularmovies.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getYearFromDateString(String dateString) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.US);
        try {
            date = dateFormat.parse(dateString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateFormat.format(date);
    }
}
