/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.reports;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.service.CryomoduleService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "EnergyGainHistory", urlPatterns = {"/reports/egain-hist"})
public class EnergyGainHistory extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EnergyGainHistory.class.getName());

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

        // Get the start and end dates of the report
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

        // Get the timeUnit parameter
        if (request.getParameter("timeUnit") == null) {
            redirectNeeded = true;
        }
        TimeUnit timeUnit = RequestParamUtil.processTimeUnit(request, TimeUnit.DAY);

        // Show the cmtype breakdown by default
        String by = request.getParameter("by");
        if (by == null) {
            by = ""; // Should get picked up default case statement
        }
        switch (by) {
            case "zone": break;
            case "cmtype" : break;
            case "cavity" : break;
            default:
                by = "cmtype";
                redirectNeeded = true;
                break;
        }
        
        // Get the list of zones that are to be displayed
        List<String> zones = new ArrayList<>();
        if (request.getParameter("zone") != null) {
            zones = Arrays.asList(request.getParameterValues("zone"));
            // Redirect will by give a non-null empty string.  We don't want this so reset it to an empty list.
            if (zones.size() == 1 && zones.get(0).isEmpty()) {
                zones = new ArrayList<>();
            }
        } else {
            redirectNeeded = true;
        }

        // Redirect back to yourself if needed to have all parameters in query string
        if (redirectNeeded) {
            List<String> encZones = new ArrayList<>();
            for (String zone : zones) {
                encZones.add(URLEncoder.encode(zone, "UTF-8"));
            }

            String redirectUrl = request.getContextPath() + "/reports/egain-hist"
                    + "?start=" + URLEncoder.encode(DateUtil.formatDateYMD(start), "UTF-8")
                    + "&end=" + URLEncoder.encode(DateUtil.formatDateYMD(end), "UTF-8")
                    + "&timeUnit=" + URLEncoder.encode(timeUnit.toString().toLowerCase(), "UTF-8")
                    + "&by=" + URLEncoder.encode(by, "UTF-8")
                    + "&zone=" + String.join("&zone=", encZones);
            response.sendRedirect(redirectUrl);
            return;
        }

        CavityService cs = new CavityService();
        CavityDataSpan cds;
        try {
            cds = cs.getCavityDataSpan(start, end, timeUnit);
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error querying cavity data", ex);
            throw new ServletException("Error querying cavity data");
        }

        CryomoduleService cms = new CryomoduleService();
        Map<String, CryomoduleType> cmTypesByZone;
        SortedMap<CryomoduleType, List<String>> zonesByCMType;
        List<String> allZones;
        try {
            cmTypesByZone = cms.getCryoModuleTypesByZone(end, zones);
            zonesByCMType = cms.getZonesByCryomoduleType(end, zones);
             allZones = new ArrayList(cms.getCryoModuleTypes(end).keySet());
             Collections.sort(allZones);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error querying cryomodule data", ex);
            throw new ServletException("Error querying cryomodule data");
        }
        
        SortedMap<Date, SortedMap<String, BigDecimal>> data;
        
        switch(by) {
            case "zone":
                data = cds.getEnergyGainByZone(zones);
                break;
            case "cmtype":
                data = cds.getEnergyGainByCMType(zones);
                break;
            case "cavity":
                data = cds.getEnergyGainByCavity(zones);
                break;
            default:
                throw new ServletException("Unrecognized value for 'by' parameter.  Supported options are zone, cmtype, cavity");
        }
        
        JsonObject egainDataJson;
        try {
            egainDataJson = DataFormatter.toFlotFromDateMap(data);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error processing cavity data", ex);
            throw new ServletException("Error processing cavity data");
        }
        
        // Convert Java collections to JsonObjects so they can be easily handled in client side javascript
       JsonObjectBuilder job = Json.createObjectBuilder();
        for (String key : cmTypesByZone.keySet()) {
            job.add(key, cmTypesByZone.get(key).toString());
        }
        JsonObject cmTypesByZoneJson = job.build();
        job = Json.createObjectBuilder();
        for (CryomoduleType cmType : zonesByCMType.keySet()) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (String zone : zonesByCMType.get(cmType)) {
                jab.add(zone);
            }
            job.add(cmType.toString(), jab.build());
        }
        JsonObject zonesByCMTypeJson = job.build();

        // The effective values of the parameters specified in the reqest
        request.setAttribute("start", DateUtil.formatDateYMD(start));
        request.setAttribute("end", DateUtil.formatDateYMD(end));
        request.setAttribute("timeUnit", timeUnit.toString().toLowerCase());
        request.setAttribute("by", by);
        request.setAttribute("zones", zones);
        
        // Data based on the parameters
        request.setAttribute("cmTypesByZone", cmTypesByZone);
        request.setAttribute("cmTypeSet", new TreeSet(cmTypesByZone.values()));
        request.setAttribute("allZones", allZones);
        request.setAttribute("cmTypesByZoneJson", cmTypesByZoneJson.toString());
        request.setAttribute("zonesByCMTypeJson", zonesByCMTypeJson.toString());
        request.setAttribute("egainDataJson", egainDataJson.toString());
        
        request.getRequestDispatcher("/WEB-INF/views/reports/egain-hist.jsp").forward(request, response);
    }
}
