/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adamc
 */
public class MathUtil {
    private static final Logger LOGGER = Logger.getLogger(MathUtil.class.getName());

    // Check each value to the following value.  If the following value is greater, return false.  If no comparison returns false,
    // then the array must be sorted.
    public static boolean isSorted(Double[] nums) {
        for ( int i = 0; i < nums.length-1; i++ ) {
            if ( nums[i] > nums[i+1] ) {
                return false;
            }
        }
        return true;
    }
    
    // Shamelessly stolen from www.java2s.com/Code/Java/Collections-Data-Structure/LinearInterpolation.htm
    // Added a MathContext to the only divide operation in this method.  Can cause exception without specified rounding, scale.
    public static Double[] interpLinear(Double[] x, Double[] y, Double[] xi) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("X and Y must be the same length");
        }
        if (x.length == 1) {
            throw new IllegalArgumentException("X must contain more than one value");
        }
        Double[] dx = new Double[x.length - 1];
        Double[] dy = new Double[x.length - 1];
        Double[] slope = new Double[x.length - 1];
        Double[] intercept = new Double[x.length - 1];

        for (int i = 0; i < x.length - 1; i++) {
            //dx[i] = x[i + 1] - x[i];
            dx[i] = x[i + 1] - x[i];
            if (dx[i] > -1E-6 && dx[i] < 1E-6) {
                throw new IllegalArgumentException("X must be monotonic. A duplicate " + "x-value was found");
            }
            if (dx[i] < 0) {
                LOGGER.log(Level.WARNING, "X must be sorted.");
                LOGGER.log(Level.FINEST, "x = {0}", toLogString(x));
                LOGGER.log(Level.FINEST, "dx = {0}", toLogString(dx));
                throw new IllegalArgumentException("X must be sorted");
            }
            //dy[i] = y[i + 1] - y[i];
            dy[i] = y[i + 1] -y[i];
            //slope[i] = dy[i] / dx[i];
            slope[i] = dy[i] / dx[i];
            //intercept[i] = y[i] - x[i] * slope[i];
            intercept[i] = y[i] - (x[i] * slope[i]);
            //intercept[i] = y[i].subtract(x[i]).multiply(slope[i]);
        }

        // Perform the interpolation here
        Double[] yi = new Double[xi.length];
        for (int i = 0; i < xi.length; i++) {
            //if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
            if (xi[i] > x[x.length - 1] || xi[i] < x[0]) {
                yi[i] = null; // same as NaN
            } else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    //yi[i] = slope[loc] * xi[i] + intercept[loc];
                    yi[i] = slope[loc] * xi[i] + intercept[loc];
                } else {
                    yi[i] = y[loc];
                }
            }
        }

        return yi;
    }

    // Convenience function for printing out BigDecimalArrays
    private static String toLogString(Double[] nums) {
        StringBuilder msg = new StringBuilder("[");
        for (Double n : nums) {
            if (n != null) {
                msg.append(", ").append(n);
            } else {
                msg.append(", null");
            }
        }
        msg.append("]");
        return msg.toString();
    }
}
