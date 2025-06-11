package org.jlab.rfd.presentation.controller.reports;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.service.CryomoduleService;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CavityResponse;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 * @author adamc
 */
@WebServlet(name = "Cavity", urlPatterns = {"/reports/cavity"})
public class Cavity extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(Cavity.class.getName());

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        boolean redirectNeeded = false;
        String[] requiredParameters = new String[]{"start", "end", "linacs", "cmtypes", "properties"};
        for (String param : requiredParameters) {
            if (request.getParameter(param) == null) {
                redirectNeeded = true;
            }
        }

        Map<String, Date> startEnd;
        List<Date> dates = new ArrayList<>();
        Date start;
        Date end;
        try {
            startEnd = RequestParamUtil.processStartEnd(request, TimeUnit.WEEK, 4);
            start = startEnd.get("start");
            end = startEnd.get("end");
            dates.add(start);
            dates.add(end);
            request.setAttribute("start", sdf.format(start));
            request.setAttribute("end", sdf.format(end));
        } catch (ParseException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Error parsing start/end parameters", ex);
            throw new ServletException("Error parsing start/end parameters");
        }

        List<String> lp = RequestParamUtil.processMultiValuedParameter(request, "linacs");
        Set<String> linacsParams;
        linacsParams = lp == null ? null : new HashSet<>(lp);
        Set<String> allLinacs = new HashSet<>(Arrays.asList("injector", "north", "south"));
        Map<String, Boolean> linacs = RequestParamUtil.generateMultiSelectionMap(allLinacs, linacsParams, false);
        request.setAttribute("linacs", linacs);

        // Get the options that should be displayed.  CED may change over time so keep the static list as insurance for
        // deletions, but query CED to get any additions.
        String[] allTypes = new String[]{"QTR", "C25", "C50", "C50T", "C75", "C100", "F100", "Booster"};
        Set<String> cmtypeOptions = new HashSet<>(Arrays.asList(allTypes));
        try {
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

        List<String> pp = RequestParamUtil.processMultiValuedParameter(request, "properties");
        Set<String> propertiesParam = pp == null ? null : new HashSet<>(pp);
        Set<String> allProps = new HashSet<>(Arrays.asList("cmtype", "cavityType", "linac", "length", "odvh", "opsGsetMax", "maxGset", "q0", "qExternal", "tripOffset",
                "tripSlope", "modAnode", "comments", "bypassed", "tunerBad", "gset"));
        Map<String, Boolean> properties = RequestParamUtil.generateMultiSelectionMap(allProps, propertiesParam, false);
        request.setAttribute("properties", properties);

        if (redirectNeeded) {
            StringBuilder redirectUrl;
            redirectUrl = new StringBuilder(request.getContextPath()
                    + "/reports/cavity?start=" + URLEncoder.encode((String) request.getAttribute("start"), StandardCharsets.UTF_8)
                    + "&end=" + URLEncoder.encode((String) request.getAttribute("end"), StandardCharsets.UTF_8));
            for (String linac : linacs.keySet()) {
                if (linacs.get(linac)) {
                    redirectUrl.append("&linacs=").append(URLEncoder.encode(linac, StandardCharsets.UTF_8));
                }
            }
            for (String cmtype : cmtypes.keySet()) {
                if (cmtypes.get(cmtype)) {
                    redirectUrl.append("&cmtypes=").append(URLEncoder.encode(cmtype, StandardCharsets.UTF_8));
                }
            }
            for (String prop : properties.keySet()) {
                if (properties.get(prop)) {
                    redirectUrl.append("&properties=").append(URLEncoder.encode(prop, StandardCharsets.UTF_8));
                }
            }
            response.sendRedirect(response.encodeRedirectURL(redirectUrl.toString()));
            return;
        }

        CavityService cs = new CavityService();
        try {
            CavityDataSpan cds = cs.getCavityDataSpan(dates);

            // Quick way to give the cavity data to the clientside javascript
            request.setAttribute("cavityData", cds.toJson().toString());

            // Get the set of cavity names for easy access in the JSP
            Set<String> names = new HashSet<>();
            for (CavityResponse cr : cds.get(start)) {
                names.add(cr.getCavityName());
            }
            // On the odd chance that they have different cavities
            for (CavityResponse cr : cds.get(end)) {
                names.add(cr.getCavityName());
            }
            request.setAttribute("cavityNames", names);

        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error querying cavity data", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException("Error querying cavity data");
        }

        request.getRequestDispatcher("/WEB-INF/views/reports/cavity.jsp").forward(request, response);
    }
}
