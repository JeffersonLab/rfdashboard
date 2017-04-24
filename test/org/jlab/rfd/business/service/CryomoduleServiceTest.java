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
import java.util.HashMap;
import org.jlab.rfd.model.CryomoduleType;
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
public class CryomoduleServiceTest {
    
    public CryomoduleServiceTest() {
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
    public void getCryoModuleTypes_basicUsage() throws ParseException, IOException {

        String expString = "0L02=QTR, 1L15=C25, 0L03=C25, 1L14=C25, 2L26=C100, 1L13=C25, 2L25=C100, 1L12=C50, "
                + "2L24=C100, 1L11=C50, 2L23=C100, 1L10=C25, 2L22=C100, 2L21=C25, 2L20=C25, 1L09=C25, 1L08=C25, "
                + "1L07=C25, 2L19=C25, 1L06=C50, 2L18=C25, 1L05=C50, 2L17=C25, 1L26=C100, 1L25=C100, 1L24=C100, "
                + "1L23=C100, 1L22=C100, 1L21=C25, 1L20=C25, 1L19=C25, 1L18=C25, 0L04=C100, 1L17=C25, 1L16=C25, "
                + "2L05=C25, 2L04=C50, 2L03=C25, 2L02=C25, 1L04=C50, 2L16=C50, 1L03=C25, 2L15=C50, 1L02=C25, "
                + "2L14=C25, 2L13=C25, 2L12=C25, 2L11=C25, 2L10=C50, 2L09=C50, 2L08=C25, 2L07=C50, 2L06=C25";
        
        HashMap<String, CryomoduleType> expTypes = new HashMap();
        String[] pairs = expString.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            expTypes.put(keyValue[0], CryomoduleType.valueOf(keyValue[1]));
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse("2017-03-29");
        CryomoduleService cs = new CryomoduleService();
        HashMap<String, CryomoduleType> cmTypes = cs.getCryoModuleTypes(date);
        
        assertEquals(cmTypes, expTypes);
    }
}
