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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Note: This truncates the difference in the Date objects to the Day field
 * ignoring smaller fields
 *
 * @author adamc
 */
public class DateUtil {

    private static final Logger LOGGER = Logger.getLogger(DateUtil.class.getName());

    private static final SimpleDateFormat YMD_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat YMD_HM_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String formatDateYMDHMS(Date d1) {
        return YMD_HM_DATE_FORMATTER.format(d1);
    }
    
    public static Date parseDateStringYMDHMS(String d1) throws ParseException {
        Date date;
        try {
            date = YMD_HM_DATE_FORMATTER.parse(d1);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error parsing date {0}", d1);
            throw ex;
        }
        return date;
    }

    /**
     * Converts a Date object to day precision.
     *
     * @param d1
     * @return A Date object corresponding to the start of the day in the
     * specified Date
     * @throws ParseException
     */
    public static Date truncateToDays(Date d1) throws ParseException {
        Date date;
        try {
            date = YMD_DATE_FORMATTER.parse(YMD_DATE_FORMATTER.format(d1));
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error parse date {0}", d1);
            throw ex;
        }
        return date;
    }

    /**
     * Converts strings to Date objects with day precision. Expects a date
     * format of yyyy-MM-dd.
     *
     * @param d1 The date string to be converted
     * @return
     * @throws ParseException
     */
    public static Date parseDateStringYMD(String d1) throws ParseException {
        Date date;
        try {
            date = YMD_DATE_FORMATTER.parse(d1);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error parse date {0}", d1);
            throw ex;
        }
        return date;
    }

    /**
     * Get the last second of the day (11:59:59)
     *
     * @param d1 The specified date
     * @return A Date object corresponding to the start of the last full second
     * of the day in the given date.
     * @throws ParseException
     */
    public static Date getEndOfDay(Date d1) throws ParseException {
        Date curr = truncateToDays(d1);
        Date next = getNextDay(curr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(next);
        cal.add(Calendar.SECOND, -1);
        return cal.getTime();
    }

    /**
     * Get a Date object that matches the start of the day after the supplied
     * date
     *
     * @param d1 The specified date
     * @return Returns a date object that represents 12AM of the very next day
     * @throws ParseException
     */
    public static Date getNextDay(Date d1) throws ParseException {
        Date curr = truncateToDays(d1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(curr);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    /**
     * Calculate the number of whole days between to points in time.
     *
     * @param d1 The first date
     * @param d2 The second date
     * @return The number of whole days between the two points in time
     */
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
        c1.add(Calendar.DATE, (int) diffDays);
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
