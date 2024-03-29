/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.util;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.TimeUnit;

/**
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
     * @param valid   Array of valid parameter values
     * @param def     Default value applied if out parameter is null
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
     * @param def     Default value applied if out parameter is null
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
     * @param request    The HttpServletRequest object of the request
     * @param unit       The time unit for calculating default start time
     * @param numBetween The number of "time units" before end we should set
     *                   start
     * @return A Map object containing "start"/"end" keys and values
     * @throws ParseException
     */
    public static Map<String, Date> processStartEnd(HttpServletRequest request, TimeUnit unit, int numBetween) throws ParseException {
        String errMsg = "Error parsing start/end parameters";
        Date start, end;
        Map<String, Date> output = new HashMap<>();

        String eString = request.getParameter("end");
        String sString = request.getParameter("start");

        if (eString != null && !eString.isEmpty()) {
            end = DateUtil.parseDateStringYMD(eString);
        } else {
            // Default to "now"
            end = DateUtil.truncateToDays(new Date());
        }

        if (sString != null && !sString.isEmpty()) {
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

        if (start.after(end)) {
            LOGGER.log(Level.SEVERE, "start is not before end");
            throw new IllegalArgumentException(errMsg);
        }

        if (end.after(new Date())) {
            LOGGER.log(Level.SEVERE, "end is a future date");
            throw new IllegalArgumentException(errMsg);
        }

        output.put("start", start);
        output.put("end", end);
        return output;
    }

    /**
     * Method for getting the values of the date request parameter.
     *
     * @param request
     * @return A List of Date objects representing the value of request "date"
     * parameters to day precision. Null if no date parameters were requested.
     * @throws java.text.ParseException
     */
    public static List<Date> processDate(HttpServletRequest request) throws ParseException {
        List<Date> dates = null;

        if (request.getParameter("date") != null) {
            dates = new ArrayList<>();
            for (String date : request.getParameterValues("date")) {
                if (date != null) {
                    dates.add(DateUtil.parseDateString(date));
                }
            }
        }
        return dates;
    }

    /**
     * Processes a multivalued request parameter into a List of it's values.
     * Note: A multivalued request parameter is a parameter that appears
     * multiple times in a single request.
     *
     * @param request
     * @param param   The parameter to process
     * @return A List of values associated with the specified request parameter
     */
    public static List<String> processMultiValuedParameter(HttpServletRequest request, String param) {
        List<String> props = null;

        if (request.getParameter(param) != null) {
            props = new ArrayList<>();
            for (String value : request.getParameterValues(param)) {
                if (value != null) {
                    props.add(value);
                }
            }
        }
        return props;
    }

    /**
     * This method supplies the common logic for turning multiple selections into a map of which of the available
     * options should be selected.  The idea is that all keys can be displayed as options, but only those with values
     * of TRUE should be treated as selected.
     * @param options The set of all options to be displayed.
     * @param selections Which options were selected by the user?  If null, use all options.
     * @param caseSensitive Does case matter when matching selections to options?
     * @return
     */
    public static Map<String, Boolean> generateMultiSelectionMap(Set<String> options, Set<String> selections,
                                                                 boolean caseSensitive) {
        Map<String, Boolean> out = new HashMap<>();

        if (selections == null) {
            for (String prop : options) {
                out.put(prop, Boolean.TRUE);
            }
        } else {
            // Create set containing lower case versions of the user choices if case insensitive.
            Set<String> selected;
            if (caseSensitive) {
                selected = selections;
            } else {
                selected = new HashSet<>();
                for (String prop : selections) {
                    selected.add(prop.toLowerCase());
                }
            }

            for (String prop : options) {
                if (caseSensitive) {
                    if (selected.contains(prop)) {
                        out.put(prop, Boolean.TRUE);
                    } else {
                        out.put(prop, Boolean.FALSE);
                    }
                } else {
                    // Selected has been mapped to lower case in this scenario
                    if (selected.contains(prop.toLowerCase())) {
                        out.put(prop, Boolean.TRUE);
                    } else {
                        out.put(prop, Boolean.FALSE);
                    }
                }
            }
        }
        return out;
    }
}