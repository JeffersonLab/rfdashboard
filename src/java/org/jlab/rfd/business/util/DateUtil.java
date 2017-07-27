/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Note: This truncates the difference in the Date objects to the Day field
 * ignoring smaller fields
 *
 * @author adamc
 */
public class DateUtil {
    private static final SimpleDateFormat YMD_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    
    // Converts dates to day only.
    public static Date truncateToDays(Date d1) throws ParseException{
        return YMD_DATE_FORMATTER.parse(YMD_DATE_FORMATTER.format(d1));
    }
    
    // Returns a date object that represents 12AM of the very next day
    public static Date nextDay(Date d1) throws ParseException {
       Date curr = truncateToDays(d1);
       Calendar cal = Calendar.getInstance();
       cal.setTime(curr);
       cal.add(Calendar.DATE, 1);
       return cal.getTime();
    }
    
    // Returns the number of whole days between to points in time
    public static long getDifferenceInDays(Date d1, Date d2) {

        // Calculate the naive difference in days without consider DST, leap seconds, etc..  This gets you close without having
        // to loop a bunch for bigger differences
        long t1 = d1.getTime();
        long t2 = d2.getTime();
        long diffDays = (t2 - t1) / (1000L * 60 * 60 * 24);

        // Compensate for these by adding or subtracting a day while the Calendar systems says that there is still a difference
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        // Unlikely we're working with enough days for the lossy conversion to matter (> ~ 2 billion)
        c1.add(Calendar.DATE, (int)diffDays);
        c2.setTime(d2);
        
        // This increments diffDays only when the difference between the calendars is one or more days.  Less than one day is
        // incremented then decremented.
        while (c1.before(c2)) {
            c1.add(Calendar.DATE, 1);
            diffDays++;
        }
        while (c1.after(c2)) {
            c1.add(Calendar.DATE, -1);
            diffDays--;
        }
        return diffDays;
    }
}
