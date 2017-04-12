/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import org.jlab.rfd.model.ModAnodeDataSpan;
import org.jlab.rfd.presentation.util.DataFormatter;
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
public class ModAnodeFormatterTest {
    
    public ModAnodeFormatterTest() {
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
    public void formatDataForFlot_BasicUsage() throws IOException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        Date s = sdf.parse("2017-03-25");
        Date e = sdf.parse("2017-03-29");
        
        final String expJson = "{"
                + "\"labels\":[\"Injector\",\"North\",\"South\",\"Total\"],"
                + "\"data\":["
                + "[[1490414400000,2],[1490500800000,2],[1490587200000,2],[1490673600000,2]],"
                + "[[1490414400000,29],[1490500800000,29],[1490587200000,29],[1490673600000,29]],"
                + "[[1490414400000,25],[1490500800000,25],[1490587200000,25],[1490673600000,25]],"
                + "[[1490414400000,56],[1490500800000,56],[1490587200000,56],[1490673600000,56]]"
                + "]}";
        
        JsonObject expected = Json.createReader(new StringReader(expJson)).readObject();

        System.out.println(expected.toString());

        ModAnodeService mas = new ModAnodeService();
        ModAnodeDataSpan maSpan = mas.getModAnodeDataSpan(s, e, "day");
        JsonObject json = DataFormatter.toJson(maSpan.getModAnodeCountByLinac());

        System.out.println(json.toString());

        assertEquals(expected.toString(), json.toString());
    }
}
