/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author adamc
 */
public class SessionUtil {
    private SessionUtil () {
        // not public
    }
    
    public static String checkAuthenticated(HttpServletRequest request) {
        Principal princ = request.getUserPrincipal();
        
        // Not sure if the ANONYMOUS portion is required, but saw this included in other apps ... can't hurt I guess
        if ( princ == null || princ.getName().isEmpty() || princ.getName().equalsIgnoreCase("ANONYMOUS")) {
            throw new SecurityException("You must be authenticated to perform the requested operation");
        }
        
        String username = princ.getName();
        return username;
    }
}
