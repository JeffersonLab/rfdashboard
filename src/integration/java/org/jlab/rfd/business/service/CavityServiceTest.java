package org.jlab.rfd.business.service;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class CavityServiceTest extends TestCase {

    public CavityServiceTest(String testName) {
        super(testName);
    }

    public void testGetCavityNames() throws IOException {
        CavityService cs = new CavityService();

        Set<String> exp = new TreeSet<>();
        exp.add("0L02-7");
        exp.add("0L02-8");
        String[] injZones = {"0L03", "0L04"};
        for (String zone : injZones) {
            for(int i = 1; i < 9; i++) {
                exp.add(zone + "-" + i);
            }
        }
        String Z;
        for(int l=1; l<3; l++) {
            for(int z=2; z < 27; z++) {
                for (int c = 1; c < 9; c++) {
                    if (z < 10) {
                        Z = "0" + z;
                    } else {
                        Z = "" + z;
                    }
                    exp.add(l + "L" + Z + "-" + c);
                }
            }
        }

        Set<String> result = cs.getCavityNames();
        assertEquals(exp, result);
    }
}
