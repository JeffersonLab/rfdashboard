/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jlab.rfd.model.LinacName;
import org.jlab.rfd.business.util.CebafNames;

/**
 *
 * @author adamc
 */
public class CebafNamesTest {
    
    public CebafNamesTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void cedZoneToEnglishLinac_BasicUsage() {
        assertEquals(CebafNames.cedZoneToEnglishLinac("0L02"), LinacName.Injector);
        assertEquals(CebafNames.cedZoneToEnglishLinac("1L17"), LinacName.North);
        assertEquals(CebafNames.cedZoneToEnglishLinac("2L25"), LinacName.South);
    }
    
    @Test (expected=IllegalArgumentException.class)
    public void cedZoneToEnglishLinac_BadInput() {
        CebafNames.cedZoneToEnglishLinac("1L27-1");
    }
}
