/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.reports;

import org.jlab.rfd.business.filter.CommentFilter;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.service.CommentService;
import org.jlab.rfd.business.service.CryomoduleService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.*;
import org.jlab.rfd.presentation.util.RequestParamUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CavityPerformance", urlPatterns = {"/reports/cavity-perf"})
public class CavityPerformance extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CavityPerformance.class.getName());

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

        Date date;
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

        String linac = (request.getParameter("linac") != null) ? request.getParameter("linac") : "";
        LinacName linacName = null;
        request.setAttribute("linac", linac);
        switch (linac) {
            case "inj":
                linacName = LinacName.Injector;
                break;
            case "nl":
                linacName = LinacName.North;
                break;
            case "sl":
                linacName = LinacName.South;
                break;
            default:
                redirectNeeded = true;
                request.setAttribute("linac", "nl");
                linacName = LinacName.North;
                break;
        }

        String cavType = (request.getParameter("cavtype") != null) ? request.getParameter("cavtype") : "";
        CavityType cavityType = null;
        switch (cavType) {
            case "C25":
                cavityType = CavityType.C25;
                break;
            case "C50":
                cavityType = CavityType.C50;
                break;
            case "C75":
                cavityType = CavityType.C75;
                break;
            case "C100":
                cavityType = CavityType.C100;
                break;
            case "all":
                break;
            default:
                cavType = "all";
                redirectNeeded = true;
        }
        request.setAttribute("cavType", cavType);


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

        // Get the options that should be displayed.  CED may change over time so keep the static list as insurance for
        // deletions, but query CED to get any additions.
        String[] allTypes = new String[]{"QTR", "C25", "C50", "C50T", "C75", "C100", "F100", "Booster"};
        Set<String> cmtypeOptions = new HashSet<>(Arrays.asList(allTypes));
        try {
            CavityService cs1 = new CavityService();
            CryomoduleService cs = new CryomoduleService();
            Collection<CryomoduleType> cedCMTypeOptions = cs.getCryoModuleTypes(Date.from(Instant.now())).values();
            for (CryomoduleType cmt : cedCMTypeOptions) {
                cmtypeOptions.add(cmt.toString());
            }
        } catch (ParseException ex) {
            throw new ServletException("Error querying ModuleTypes from CED", ex);
        }

        // Make the map of what was selected and what was not.
        List<String> ctp = RequestParamUtil.processMultiValuedParameter(request, "cmtypes");
        Set<String> cmtypesParam = ctp == null ? null : new HashSet<>(ctp);

        Map <String, Boolean> cmtypes = RequestParamUtil.generateMultiSelectionMap(cmtypeOptions, cmtypesParam, false);
        request.setAttribute("cmtypes", cmtypes);

        if (redirectNeeded) {
            String redirectUrl = request.getContextPath() + "/reports/cavity-perf?date="
                    + URLEncoder.encode((String) request.getAttribute("date"), StandardCharsets.UTF_8)
                    + "&sortBy=" + URLEncoder.encode((String) request.getAttribute("sortBy"), StandardCharsets.UTF_8)
                    + "&linac=" + URLEncoder.encode((String) request.getAttribute("linac"), StandardCharsets.UTF_8)
                    + "&cavtype=" + URLEncoder.encode((String) request.getAttribute("cavType"), StandardCharsets.UTF_8);
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        CavityService cs = new CavityService();
        List<CavityDataPoint> cavList;
        try {
            cavList = new ArrayList<>(cs.getCavityData(date, linacName, cavityType));
            switch ((String) request.getAttribute("sortBy")) {
                case "name":
                    Collections.sort(cavList, new Comparator<>() {
                        @Override
                        public int compare(CavityDataPoint c1, CavityDataPoint c2) {
                            return c1.getCavityName().compareTo(c2.getCavityName());
                        }
                    });
                    break;
                case "perf":
                    Collections.sort(cavList, new Comparator<>() {
                        @Override
                        public int compare(CavityDataPoint c1, CavityDataPoint c2) {
                            boolean isNan = false;
                            // How to handle NaN
                            if (Double.isNaN(c1.getEGainPerformance()) && !Double.isNaN(c2.getEGainPerformance())) {
                                return -1;
                            } else if (!Double.isNaN(c1.getEGainPerformance()) && Double.isNaN(c2.getEGainPerformance())) {
                                return 1;
                            } else if (Double.isNaN(c1.getEGainPerformance()) && Double.isNaN(c2.getEGainPerformance())) {
                                isNan = true;
                            }

                            // Regular numbers
                            if (!isNan) {
                                if (c1.getEGainPerformance() > c2.getEGainPerformance()) {
                                    return 1;
                                } else if (c1.getEGainPerformance() < c2.getEGainPerformance()) {
                                    return -1;
                                }
                            }

                            // If we're still equal, sort by their names.
                            return c1.getCavityName().compareTo(c2.getCavityName());
                        }
                    });
                    break;
            }
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying cryomodule data");
            throw new ServletException("Error querying cryomodule data");
        }
        request.setAttribute("cavList", cavList);

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

        Map<String, CavityResponse> cavs;
        try {
            cavs = cs.getCavityDataMap(date, linacName);
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying cavity data sources: {0}", ex.toString());
            throw new ServletException("Error querying comment database");
        }
        request.setAttribute("cavityMap", cavs);

        request.getRequestDispatcher("/WEB-INF/views/reports/cavity-perf.jsp").forward(request, response);
    }
}
