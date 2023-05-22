/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        cal.set(2018, Calendar.JANUARY, 1);
        Date date = cal.getTime();
        int stepSize = 1;
        int numSteps = 1;
        String deployment = "history";

        MyaService instance = new MyaService();
        String expResult = "{\"channels\":{\"R12XHTPLEM\":{\"metadata\":{\"name\":\"R12XHTPLEM\",\"datatype\":\"DBR_DOUBLE\",\"datasize\":1,\"datahost\":\"opsmya8\",\"ioc\":\"iocnl1\",\"active\":true},\"data\":[{\"d\":\"2023-01-01T00:00:00\",\"v\":72.117897}],\"returnCount\":1},\"R13XHTPLEM\":{\"metadata\":{\"name\":\"R13XHTPLEM\",\"datatype\":\"DBR_DOUBLE\",\"datasize\":1,\"datahost\":\"opsmya13\",\"ioc\":\"iocnl1\",\"active\":true},\"data\":[{\"d\":\"2023-01-01T00:00:00\",\"v\":66.913399}],\"returnCount\":1}}}";
        String result = instance.mySampler(channels, date, stepSize, numSteps, deployment).toString();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of mySampler method, of class MyaService.
     */
    public void testMySampler_3arg() throws Exception {
        System.out.println("mySampler 3args");

        List<String> channels = new ArrayList<>();
        channels.add("R12XHTPLEM");
        channels.add("R13XHTPLEM");
        Calendar cal = Calendar.getInstance();
        cal.set(2018, Calendar.JANUARY, 1);
        Date date = cal.getTime();

        MyaService instance = new MyaService();
        String expResult = "{R12XHTPLEM=72.117897, R13XHTPLEM=66.913399}";
        String result = instance.mySampler(channels, date, "history").toString();
        assertEquals(expResult, result);
    }    
}
