/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.ParamChecker;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "Cavity", urlPatterns = {"/cavity"})
public class Cavity extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(Cavity.class.getName());

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        boolean redirectNeeded = false;
        String[] requiredParameters = new String[]{"start", "end", "linacs", "cmtypes", "properties"};
        for ( String param : requiredParameters ) {
            if (request.getParameter(param) == null) {
                redirectNeeded = true;
            }
        }

        Map<String, Date> startEnd;
        Date start;
        Date end;
        try {
            startEnd = RequestParamUtil.processStartEnd(request, TimeUnit.WEEK, 4);
            start = startEnd.get("start");
            end = startEnd.get("end");
            request.setAttribute("start", sdf.format(start));
            request.setAttribute("end", sdf.format(end));
        } catch (ParseException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Error parsing start/end parameters", ex);
            throw new ServletException("Error parsing start/end parameters");
        }
        
        List<String> linacs = RequestParamUtil.processMultiValuedParameter(request, "linacs");
        if (linacs == null ) {
            String[] allLinacs = new String[] {"injector", "north", "south"};
            linacs = new ArrayList<>();
            linacs.addAll(Arrays.asList(allLinacs));
        }
        request.setAttribute("linacs", DataFormatter.listToMap(linacs));

        List<String> cmtypes = RequestParamUtil.processMultiValuedParameter(request, "cmtypes");
        if (cmtypes == null ) {
            String[] allTypes = new String[] {"QTR", "C25", "C50", "C100"};
            cmtypes = new ArrayList<>();
            cmtypes.addAll(Arrays.asList(allTypes));
        }
        request.setAttribute("cmtypes", DataFormatter.listToMap(cmtypes));

        List<String> properties = RequestParamUtil.processMultiValuedParameter(request, "properties");
        if (properties == null ) {
            String[] allProps = new String[] {"cmtype", "linac", "length", "odvh", "opsGsetMax", "maxGset", "q0", "qExternal", "tripOffset",
                "tripSlope", "modAnode", "comments"};
            properties = new ArrayList<>();
            properties.addAll(Arrays.asList(allProps));
        }
        request.setAttribute("properties", DataFormatter.listToMap(properties));
        
        if (redirectNeeded) {
            String redirectUrl;
            try {
                redirectUrl = request.getContextPath()
                        + "/cavity?start=" + URLEncoder.encode((String) request.getAttribute("start"), "UTF-8")
                        + "&end=" + URLEncoder.encode((String) request.getAttribute("end"), "UTF-8");
                for(String linac : linacs) {
                    redirectUrl = redirectUrl + "&linacs=" + URLEncoder.encode(linac, "UTF-8");
                }
                for(String cmtype : cmtypes) {
                    redirectUrl = redirectUrl + "&cmtypes=" + URLEncoder.encode(cmtype, "UTF-8");
                }
                for(String prop : properties) {
                    redirectUrl = redirectUrl + "&properties=" + URLEncoder.encode(prop, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("JVM doesn't support UTF-8");
            }
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/cavity.jsp").forward(request, response);
    }
}