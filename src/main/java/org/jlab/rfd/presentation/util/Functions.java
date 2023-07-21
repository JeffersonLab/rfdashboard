/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.util;

import java.util.List;

/**
 * @author adamc
 */
public class Functions {

    private Functions() {
        // cannot instantiate
    }

    // shamelessly stolen from Ryan's smoothness template
    public static boolean inArray(String[] haystack, String needle) {
        boolean inArray = false;

        if (needle != null && haystack != null) {
            for (String s : haystack) {
                if (needle.equals(s)) {
                    inArray = true;
                    break;
                }
            }
        }

        return inArray;
    }

    // shamelessly stolen from Ryan's smoothness template
    public static boolean inList(List<String> haystack, String needle) {
        boolean inList = false;
        if (haystack != null && needle != null) {
            inList = haystack.contains(needle);
        }
        return inList;
    }

    public static String capitalizeFirst(String s) {
        if (s == null) {
            return null;
        } else if (s.isEmpty()) {
            return s;
        } else if (s.length() == 1) {
            return s.toUpperCase();
        } else {
            String s1 = s.substring(0, 1).toUpperCase();
            return s1 + s.substring(1);
        }
    }
}
