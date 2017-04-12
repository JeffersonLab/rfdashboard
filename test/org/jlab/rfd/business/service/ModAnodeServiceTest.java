/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import org.jlab.rfd.model.ModAnodeDataPoint;
import org.jlab.rfd.model.ModAnodeDataSpan;
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
public class ModAnodeServiceTest {
    
    public ModAnodeServiceTest() {
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
    public void getModAnodeData_basicUsage() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        Date date = sdf.parse("2017-03-29");
        
        ModAnodeService mas = new ModAnodeService();
        HashSet<ModAnodeDataPoint> maData = mas.getModAnodeData(date);

        assertTrue(418 == maData.size());
    }

    @Test
    public void getModAnodeDataSpan_basicUsage() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        Date start = sdf.parse("2017-03-22");
        Date end = sdf.parse("2017-03-29");
        
        ModAnodeService mas = new ModAnodeService();
        ModAnodeDataSpan maSpan = mas.getModAnodeDataSpan(start, end, "day");
        
        assertTrue(7 == maSpan.size());
    }
}
