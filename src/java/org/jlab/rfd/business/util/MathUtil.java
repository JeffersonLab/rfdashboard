/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
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
    public static final boolean isSorted(BigDecimal[] nums) {
        for ( int i = 0; i < nums.length-1; i++ ) {
            if ( nums[i].compareTo(nums[i+1]) == 1 ) {
                return false;
            }
        }
        return true;
    }
    
    // Shamelessly stolen from www.java2s.com/Code/Java/Collections-Data-Structure/LinearInterpolation.htm
    // Added a MathContext to the only divide operation in this method.  Can cause exception without specified rounding, scale.
    public static final BigDecimal[] interpLinear(BigDecimal[] x, BigDecimal[] y, BigDecimal[] xi) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("X and Y must be the same length");
        }
        if (x.length == 1) {
            throw new IllegalArgumentException("X must contain more than one value");
        }
        BigDecimal[] dx = new BigDecimal[x.length - 1];
        BigDecimal[] dy = new BigDecimal[x.length - 1];
        BigDecimal[] slope = new BigDecimal[x.length - 1];
        BigDecimal[] intercept = new BigDecimal[x.length - 1];

        // Calculate the line equation (i.e. slope and intercept) between each point
        BigInteger zero = new BigInteger("0");
        BigDecimal minusOne = new BigDecimal(-1);

        for (int i = 0; i < x.length - 1; i++) {
            //dx[i] = x[i + 1] - x[i];
            dx[i] = x[i + 1].subtract(x[i]);
            if (dx[i].equals(new BigDecimal(zero, dx[i].scale()))) {
                throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
            }
            if (dx[i].signum() < 0) {
                LOGGER.log(Level.WARNING, "X must be sorted.");
                LOGGER.log(Level.FINEST, "x = {0}", toLogString(x));
                LOGGER.log(Level.FINEST, "dx = {0}", toLogString(dx));
                throw new IllegalArgumentException("X must be sorted");
            }
            //dy[i] = y[i + 1] - y[i];
            dy[i] = y[i + 1].subtract(y[i]);
            //slope[i] = dy[i] / dx[i];
            MathContext mc = new MathContext(Math.max(dy[i].scale(), dx[i].scale()), RoundingMode.HALF_UP);
            slope[i] = dy[i].divide(dx[i], mc);
            //intercept[i] = y[i] - x[i] * slope[i];
            intercept[i] = x[i].multiply(slope[i]).subtract(y[i]).multiply(minusOne);
            //intercept[i] = y[i].subtract(x[i]).multiply(slope[i]);
        }

        // Perform the interpolation here
        BigDecimal[] yi = new BigDecimal[xi.length];
        for (int i = 0; i < xi.length; i++) {
            //if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
            if (xi[i].compareTo(x[x.length - 1]) > 0 || xi[i].compareTo(x[0]) < 0) {
                yi[i] = null; // same as NaN
            } else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    //yi[i] = slope[loc] * xi[i] + intercept[loc];
                    yi[i] = slope[loc].multiply(xi[i]).add(intercept[loc]);
                } else {
                    yi[i] = y[loc];
                }
            }
        }

        return yi;
    }

    // Convenience function for printing out BigDecimalArrays
    private static String toLogString(BigDecimal[] nums) {
        String msg = "[";
        for (BigDecimal n : nums) {
            if (n != null) {
                msg += ", " + n.toString();
            } else {
                msg += ", null";
            }
        }
        msg += "]";
        return msg;
    }
}
