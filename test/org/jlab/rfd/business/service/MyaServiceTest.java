/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import junit.framework.TestCase;

/**
 *
 * @author adamc
 */
public class MyaServiceTest extends TestCase {
    
    public MyaServiceTest(String testName) {
        super(testName);
    }

    /**
     * Test of mySampler method, of class MyaService.
     */
    public void testMySampler_5args() throws IOException {
        System.out.println("mySampler 5args");
        
        List<String> channels = new ArrayList<>();
        channels.add("R12XHTPLEM");
        channels.add("R13XHTPLEM");
        Calendar cal = Calendar.getInstance();
        cal.set(2018, 0, 1);
        Date date = cal.getTime();
        int stepSize = 1;
        int numSteps = 1;
        String deployment = "ops";

        MyaService instance = new MyaService();
        String expResult = "{\"data\":[{\"date\":\"2018-01-01T00:00:00\",\"values\":[{\"R12XHTPLEM\":\"73.4588\"},{\"R13XHTPLEM\":\"86.6749\"}]}]}";
        String result = instance.mySampler(channels, date, stepSize, numSteps, deployment).toString();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of mySampler method, of class MyaService.
     */
    public void testMySampler_2arg() throws Exception {
        System.out.println("mySampler 2args");

        List<String> channels = new ArrayList<>();
        channels.add("R12XHTPLEM");
        channels.add("R13XHTPLEM");
        Calendar cal = Calendar.getInstance();
        cal.set(2018, 0, 1);
        Date date = cal.getTime();

        MyaService instance = new MyaService();
        String expResult = "{R12XHTPLEM=73.4588, R13XHTPLEM=86.6749}";
        String result = instance.mySampler(channels, date).toString();
        assertEquals(expResult, result);
    }    
}
