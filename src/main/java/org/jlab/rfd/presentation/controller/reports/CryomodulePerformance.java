/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.reports;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.filter.CommentFilter;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.service.CommentService;
import org.jlab.rfd.business.service.CryomoduleService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.CavityResponse;
import org.jlab.rfd.model.Comment;
import org.jlab.rfd.model.CryomoduleDataPoint;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CryomodulePerformance", urlPatterns = {"/reports/cm-perf"})
public class CryomodulePerformance extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CryomodulePerformance.class.getName());

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

        Date date = null;
        try {
            // Only the first date supplied will be used
            List<Date> dates = RequestParamUtil.processDate(request);
            if (dates == null) {
                redirectNeeded = true;
                date = DateUtil.truncateToDate(new Date());
            } else {
                date = DateUtil.truncateToDate(dates.get(0));
            }
            request.setAttribute("date", DateUtil.formatDateYMD(date));
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error parsing date parameter");
            throw new ServletException("Error parsing date parameter");
        }

        String sortBy = (request.getParameter("sortBy") != null) ? request.getParameter("sortBy") : "";
        switch (sortBy) {
            case "name":
            case "perf":
                request.setAttribute("sortBy", sortBy);
                break;
            default:
                request.setAttribute("sortBy", "perf");
                redirectNeeded = true;
                break;
        }

        if (redirectNeeded) {
            String redirectUrl = request.getContextPath() + "/reports/cm-perf?date="
                    + URLEncoder.encode((String) request.getAttribute("date"), "UTF-8")
                    + "&sortBy=" + URLEncoder.encode((String) request.getAttribute("sortBy"), "UTF-8");
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        CryomoduleService cs = new CryomoduleService();
        List<CryomoduleDataPoint> cmList;
        try {
            cmList = cs.getCryomoduleDataPoints(date);
            switch ((String) request.getAttribute("sortBy")) {
                case "name":
                    Collections.sort(cmList, new Comparator<CryomoduleDataPoint>() {
                        @Override
                        public int compare(CryomoduleDataPoint c1, CryomoduleDataPoint c2) {
                            return c1.getName().compareTo(c2.getName());
                        }
                    });
                    break;
                case "perf":
                    Collections.sort(cmList, new Comparator<CryomoduleDataPoint>() {
                        @Override
                        public int compare(CryomoduleDataPoint c1, CryomoduleDataPoint c2) {
                            // How to handle NaN
                            if (Double.isNaN(c1.getEGainPerformance()) && !Double.isNaN(c2.getEGainPerformance())) {
                                return -1;
                            } else if (!Double.isNaN(c1.getEGainPerformance()) && Double.isNaN(c2.getEGainPerformance())) {
                                return 1;
                            } else if (Double.isNaN(c1.getEGainPerformance()) && Double.isNaN(c2.getEGainPerformance())) {
                                return 0;
                            }

                            // Regular numbers
                            if (c1.getEGainPerformance() > c2.getEGainPerformance()) {
                                return 1;
                            } else if (c1.getEGainPerformance() < c2.getEGainPerformance()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
            }
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying cryomodule data");
            throw new ServletException("Error querying cryomodule data");
        }
        request.setAttribute("cmList", cmList);

        CommentService comms = new CommentService();
        CommentFilter cf = new CommentFilter(null, null, null, null, null);
        Map<String, SortedSet<Comment>> comments;
        try {
            comments = comms.getCommentsByTopic(cf, 10, 0);
        } catch (SQLException | ParseException ex) {
            LOGGER.log(Level.WARNING, "Error querying comment database: {0}", ex.toString());
            throw new ServletException("Error querying comment database");
        }
        request.setAttribute("commentMap", comments);

        CavityService cavService = new CavityService();
        Map<String, CavityResponse> cavs;
        try {
            cavs = cavService.getCavityDataMap(date);
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying cavity datasources: {0}", ex.toString());
            throw new ServletException("Error querying comment database");
        }
        request.setAttribute("cavityMap", cavs);
        
        request.getRequestDispatcher("/WEB-INF/views/reports/cm-perf.jsp").forward(request, response);
    }
}
