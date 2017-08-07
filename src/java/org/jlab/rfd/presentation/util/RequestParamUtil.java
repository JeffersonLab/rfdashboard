/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.TimeUnit;

/**
 *
 * @author adamc
 */
public class RequestParamUtil {

    private static final Logger LOGGER = Logger.getLogger(RequestParamUtil.class.getName());

    private RequestParamUtil() {
        // not public
    }

    /**
     * Processes the standard "out" request parameter for specifying output
     * format
     *
     * @param request HTTP Request object
     * @param valid Array of valid parameter values
     * @param def Default value applied if out parameter is null
     * @return The string representing the parameter value or null if an invalid
     * request was made.
     */
    public static String processOut(HttpServletRequest request, String[] valid, String def) {
        String out = request.getParameter("out");
        if (out == null) {
            return def;
        }
        if (!Arrays.asList(valid).contains(out)) {
            LOGGER.log(Level.SEVERE, "Unsupported out format requested - {0}", out);
            return null;
        }

        return out;
    }

    /**
     * Processes the standard "timeUnit" request parameter for specifying output
     * format
     *
     * @param request HTTP Request object
     * @param def Default value applied if out parameter is null
     * @return The string representing the parameter value or null if an invalid
     * request was made.
     */
    public static TimeUnit processTimeUnit(HttpServletRequest request, TimeUnit def) {
        TimeUnit timeUnit;
        String unit = request.getParameter("timeUnit");
        if (unit == null) {
            return def;
        }
        try {
            timeUnit = TimeUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Unsupported timeUnit requested - {0}", unit);
            return null;
        }

        return timeUnit;
    }

    /**
     * The method processes the standard start/end parameter pairs. By default,
     * end is today, and start is determined as the "numBetween" "unit" before
     * end - e.g., 5, DAY means 5 days before end if nothing was specified.
     *
     * @param request The HttpServletRequest object of the request
     * @param unit The time unit for calculating default start time
     * @param numBetween The number of "time units" before end we should set
     * start
     * @return A Map object containing "start"/"end" keys and values
     * @throws ParseException
     */
    public static Map<String, Date> processStartEnd(HttpServletRequest request, TimeUnit unit, int numBetween) throws ParseException {
        String errMsg = "Error parsing start/end parameters";
        Date start, end;
        Map<String, Date> output = new HashMap<>();

        String eString = request.getParameter("end");
        String sString = request.getParameter("start");

        if (eString != null) {
            end = DateUtil.parseDateStringYMD(eString);
        } else {
            // Default to "now"
            end = DateUtil.truncateToDays(new Date());
        }

        if (sString != null) {
            start = DateUtil.parseDateStringYMD(sString);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            switch (unit) {
                case DAY:
                    cal.add(Calendar.DATE, -1 * numBetween);
                    break;
                case WEEK:
                    cal.add(Calendar.WEEK_OF_YEAR, -1 * numBetween);
                    break;
                default:
                    // Compiler should catch the invalid TimeUnit, but just in case modifications are made later
                    LOGGER.log(Level.SEVERE, "Received unrecognized time unit");
                    throw new IllegalArgumentException(errMsg);
            }
            start = cal.getTime();
        }

        if ( start.after(end) ) {
            LOGGER.log(Level.SEVERE, "start is not before end");
            throw new IllegalArgumentException(errMsg);
        }
        
        if ( end.after(new Date()) ) {
            LOGGER.log(Level.SEVERE, "end is a future date");
            throw new IllegalArgumentException(errMsg);
        }

        output.put("start", start);
        output.put("end", end);
        return output;
    }

    
    public static List<Date> processDate(HttpServletRequest request) {
        List<Date> dates = null;
        
        if ( request.getParameter("date") != null ) {
            dates = new ArrayList<>();
            for (String date : request.getParameterValues("date") ) {
                if ( date != null ) {
//                    try {
//                        dates.add()
//                    }
                }
            }
        }
        return dates;
    }
}
