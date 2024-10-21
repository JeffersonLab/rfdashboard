/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.util.*;

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
        List<String> channels = new ArrayList<>();
        channels.add("R12XHTPLEM");
        channels.add("R13XHTPLEM");
        int stepSize = 1;
        int numSteps = 1;
        String deployment = "history";
        Calendar cal = Calendar.getInstance();
        cal.set(2018, Calendar.JANUARY, 1);
        Date date = cal.getTime();

        MyaService instance = new MyaService();
        String expResult = "{\"channels\":{\"R12XHTPLEM\":{\"metadata\":{\"name\":\"R12XHTPLEM\",\"datatype\":\"DBR_DOUBLE\",\"datasize\":1,\"datahost\":\"hstmya0\",\"ioc\":null,\"active\":true},\"data\":[{\"d\":\"2018-01-01T00:00:00\",\"v\":73.4588}],\"returnCount\":1},\"R13XHTPLEM\":{\"metadata\":{\"name\":\"R13XHTPLEM\",\"datatype\":\"DBR_DOUBLE\",\"datasize\":1,\"datahost\":\"hstmya3\",\"ioc\":null,\"active\":true},\"data\":[{\"d\":\"2018-01-01T00:00:00\",\"v\":86.6749}],\"returnCount\":1}}}";
        String result = instance.mySampler(channels, date, stepSize, numSteps, deployment).toString();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of mySampler method, of class MyaService.
     */
    public void testMySampler_3arg() throws Exception {
        List<String> channels = new ArrayList<>();
        channels.add("R12XHTPLEM");
        channels.add("R13XHTPLEM");
        Calendar cal = Calendar.getInstance();
        cal.set(2018, Calendar.JANUARY, 1);
        Date date = cal.getTime();

        MyaService instance = new MyaService();
        String expResult = "{R12XHTPLEM=73.4588, R13XHTPLEM=86.6749}";
        String result = instance.mySampler(channels, date, "history").toString();
        assertEquals(expResult, result);
    }    
}
