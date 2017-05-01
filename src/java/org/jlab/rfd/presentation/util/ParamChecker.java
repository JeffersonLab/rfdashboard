/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.util;

import java.util.Date;
import org.jboss.logging.Logger;

/**
 *
 * @author adamc
 */
public final class ParamChecker {
    private static final Logger LOGGER = Logger.getLogger(ParamChecker.class.getName());
    
    private ParamChecker() {
        // not public
    }

    // Check start and end date for basic constraint violations
    // Mya and CED will both return data for request in the future (error?), so we need to filter that out.
    public static void validateStartEnd(Date start, Date end) {
        if ( start == null || end == null ) {
            throw new RuntimeException("start and end dates are required");
        } else if ( ! start.before(end) ) {
            throw new RuntimeException("start date must be before end date");
        } else if ( end.after(new Date())) {
            throw new RuntimeException("end date must be no later than today");
        }
    }
    
    // Mya and CED will both return data for request in the future (error?), so we need to filter that out.
    public static void validateDate(Date date) {
        if ( date.after(new Date()) ) {
            throw new RuntimeException("date must be no later than today");
        }
    }
}