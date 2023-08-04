package org.jlab.rfd.presentatil.util;

import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.presentation.util.CMTypeMapper;
import org.junit.Assert;
import org.junit.Test;

public class CMTypeMapperTest {
    @Test
    public void testBasicUsage() {
        CMTypeMapper mapper = new CMTypeMapper();
        Assert.assertEquals("C50", mapper.get("C50T"));
        Assert.assertEquals("C100", mapper.get("F100"));

        // Checked the mapped values
        Assert.assertEquals(CryomoduleType.C50, mapper.get(CryomoduleType.C50T));
        Assert.assertEquals(CryomoduleType.C100, mapper.get(CryomoduleType.F100));
        Assert.assertEquals(CryomoduleType.QTR, mapper.get(CryomoduleType.Booster));

        // Check that unmapped values make it through
        Assert.assertEquals(CryomoduleType.C75, mapper.get(CryomoduleType.C75));
    }

    @Test(expected = IllegalArgumentException.class)
    public  void testPutError() {
        CMTypeMapper mapper = new CMTypeMapper();
        mapper.put("C50","asdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public  void testGetError() {
        CMTypeMapper mapper = new CMTypeMapper();
        mapper.get("asdf");
    }

    @Test
    public void testGetPutGood() {
        CMTypeMapper mapper = new CMTypeMapper();
        mapper.put("C50", "C75");
        mapper.put(CryomoduleType.C100, CryomoduleType.F100);

        Assert.assertEquals("C75", mapper.get("C50"));
        Assert.assertEquals("F100", mapper.get("C100"));
        Assert.assertEquals(CryomoduleType.F100, mapper.get(CryomoduleType.C100));
        Assert.assertEquals(CryomoduleType.C75, mapper.get(CryomoduleType.C50));
    }
}
