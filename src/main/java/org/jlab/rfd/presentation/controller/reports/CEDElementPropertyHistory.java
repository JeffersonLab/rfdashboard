/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.reports;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CEDUpdateHistoryService;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.CEDElementUpdate;
import org.jlab.rfd.model.CEDElementUpdateHistory;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CEDElementPropertyHistory", urlPatterns = {"/reports/ced-prop-hist"})
public class CEDElementPropertyHistory extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CEDElementPropertyHistory.class.getName());

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
        boolean redirectNeeded = false;

        Date start = null;
        Date end = null;
        try {
            if (request.getParameter("start") == null || request.getParameter("end") == null) {
                redirectNeeded = true;
            }
            Map<String, Date> dates = RequestParamUtil.processStartEnd(request, TimeUnit.WEEK, 4);
            start = dates.get("start");
            end = dates.get("end");
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error parsing start/end paramenters");
            throw new ServletException("Error parsing start/end paramenters");
        }

        List<String> elems = null;
        if (request.getParameter("e") != null) {
//            elems = Arrays.asList(request.getParameterValues("e"));
            elems = new ArrayList<>();
            for (String e : request.getParameterValues("e")) {
                elems.add(e);
            }
        }

        List<String> props = null;
        if (request.getParameter("props") != null) {
//            props = Arrays.asList(request.getParameterValues("props"));
            props = new ArrayList<>();
            for (String prop : request.getParameterValues("props")) {
                props.add(prop);
            }
        }

        if (redirectNeeded) {
            String redirectUrl;
            try {
                redirectUrl = request.getContextPath()
                        + "/reports/ced-prop-hist?start=" + URLEncoder.encode(DateUtil.formatDateYMDHMS(start), "UTF-8")
                        + "&end=" + URLEncoder.encode(DateUtil.formatDateYMDHMS(end), "UTF-8");
                if (request.getParameter("e") != null) {
                    redirectUrl += "&e=" + URLEncoder.encode(request.getParameter("e"), "UTF-8");
                }
                if (request.getParameter("props") != null) {
                    redirectUrl += "&props=" + URLEncoder.encode(request.getParameter("props"), "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("JVM doesn't support UTF-8");
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        CEDUpdateHistoryService cuhs = new CEDUpdateHistoryService();
        List<CEDElementUpdate> cedUpdates;
        try {
            cedUpdates = cuhs.getUpdateList(elems, props, start, end);
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.SEVERE, "Error querying CED data", ex);
            throw new ServletException("Error querying CED data");
        }

        // Sort the updates by timestamp in descending order - using a lambda for the comparator
//        Collections.sort(cedUpdates, (c1, c2) -> c2.getDateString().compareTo(c1.getDateString()));

        // Get the list of cavity names that can be selected
        CavityService cs = new CavityService();
        SortedSet<String> cavNames = cs.getCavityNames();
//        List<String> cavProps = Arrays.asList(new String[]{"OpsGsetMax", "Bypassed", "TunerBad", "MaxGSET"});
        List<String> cavProps = new ArrayList<>();
        cavProps.add("OpsGsetMax");
        cavProps.add("Bypassed");
        cavProps.add("TunerBad");
        cavProps.add("MaxGSET");

        request.setAttribute("elems", elems);
        request.setAttribute("props", props);
        request.setAttribute("cavNames", cavNames);
        request.setAttribute("cavProps", cavProps);
        request.setAttribute("start", DateUtil.formatDateYMDHMS(start));
        request.setAttribute("end", DateUtil.formatDateYMDHMS(end));
        request.setAttribute("cedUpdates", cedUpdates);

        request.getRequestDispatcher("/WEB-INF/views/reports/ced-prop-hist.jsp").forward(request, response);
    }
}
