/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author adamc
 */
public class DateUtilTest {
    
    public DateUtilTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getDifferenceInDays_basicUsage() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        // Regular date range
        assertEquals(31, DateUtil.getDifferenceInDays(sdf.parse("2017-01-01"), sdf.parse("2017-02-01")));
        // Date range containing DST spring forward
        assertEquals(31, DateUtil.getDifferenceInDays(sdf.parse("2017-03-01"), sdf.parse("2017-04-01")));
        // Date range containing DST fall back
        assertEquals(30, DateUtil.getDifferenceInDays(sdf.parse("2017-11-01"), sdf.parse("2017-12-01")));

        // Single day range containing DST spring forward
        assertEquals(1, DateUtil.getDifferenceInDays(sdf.parse("2017-03-12"), sdf.parse("2017-03-013")));
        // Single day range containing DST fall back
        assertEquals(1, DateUtil.getDifferenceInDays(sdf.parse("2017-11-05"), sdf.parse("2017-11-06")));
    }
}
