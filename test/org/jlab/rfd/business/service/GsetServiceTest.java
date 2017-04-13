package org.jlab.rfd.business.service;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
public class GsetServiceTest {
    
    public GsetServiceTest() {
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

    /*
    * This just checks that the method doesn't blow up.  "Difficult" to check since order isn't guarenteed.
    */
//    @Test
//    public void getCavityEpicsNames_basicUsage() throws ParseException, IOException {
//        
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
//        Date timestamp = sdf.parse("2017-02-05");
//                
//        GsetService gs = new GsetService();
//        HashMap<String, String> name2Epics = gs.getCavityEpicsNames(timestamp);
//    }
//    
//    @Test
//    public void getCavityGsetData_basicUsage() throws ParseException, IOException {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
//        Date timestamp = sdf.parse("2017-02-05");
//                
//        GsetService gs = new GsetService();
//        HashMap<String, String> name2Epics = gs.getCavityEpicsNames(timestamp);
//
//        HashMap<String, Double> gsetData = gs.getCavityGsetData(timestamp, name2Epics);
//        System.out.print(gsetData.toString());
//    }
}
