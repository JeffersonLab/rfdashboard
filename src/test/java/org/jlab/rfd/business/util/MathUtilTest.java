package org.jlab.rfd.business.util;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilTest {

    @Test
    public void testIsSorted() {
        Double[] sorted = new Double[]{-23.8103, 0.0, 11.5, 11.6, 11.600000001, 12.0, 12.0, 13.1};
        Double[] unsorted1 = new Double[]{100.0, 11.5, 11.6, 11.600000001, 12.0, 12.0, 13.1};
        Double[] unsorted2 = new Double[]{0.0, 11.5, 12.0, 11.600000001, 12.0, 12.0, 13.1};
        Double[] unsorted3 = new Double[]{0.0, -11.5, 12.0, 11.600000001, 12.0, 12.0, 13.1};
        Double[] unsorted4 = new Double[]{0.0, -11.5, 12.0, 11.600000001, 12.0, 12.0, 11.0};
        Assert.assertTrue(MathUtil.isSorted(sorted));
        Assert.assertFalse(MathUtil.isSorted(unsorted1));
        Assert.assertFalse(MathUtil.isSorted(unsorted2));
        Assert.assertFalse(MathUtil.isSorted(unsorted3));
        Assert.assertFalse(MathUtil.isSorted(unsorted4));
    }
}
