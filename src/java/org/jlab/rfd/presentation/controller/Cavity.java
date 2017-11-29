package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CavityResponse;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "Cavity", urlPatterns = {"/cavity"})
public class Cavity extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(Cavity.class.getName());

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

        List<String> linacs = RequestParamUtil.processMultiValuedParameter(request, "linacs");
        if (linacs == null) {
            String[] allLinacs = new String[]{"injector", "north", "south"};
            linacs = new ArrayList<>();
            linacs.addAll(Arrays.asList(allLinacs));
        }
        request.setAttribute("linacs", DataFormatter.listToMap(linacs));

        List<String> cmtypes = RequestParamUtil.processMultiValuedParameter(request, "cmtypes");
        if (cmtypes == null) {
            String[] allTypes = new String[]{"QTR", "C25", "C50", "C100"};
            cmtypes = new ArrayList<>();
            cmtypes.addAll(Arrays.asList(allTypes));
        }
        request.setAttribute("cmtypes", DataFormatter.listToMap(cmtypes));

        List<String> properties = RequestParamUtil.processMultiValuedParameter(request, "properties");
        if (properties == null) {
            String[] allProps = new String[]{"cmtype", "linac", "length", "odvh", "opsGsetMax", "maxGset", "q0", "qExternal", "tripOffset",
                "tripSlope", "modAnode", "comments", "bypassed", "tunerBad", "gset"};
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
                for (String linac : linacs) {
                    redirectUrl = redirectUrl + "&linacs=" + URLEncoder.encode(linac, "UTF-8");
                }
                for (String cmtype : cmtypes) {
                    redirectUrl = redirectUrl + "&cmtypes=" + URLEncoder.encode(cmtype, "UTF-8");
                }
                for (String prop : properties) {
                    redirectUrl = redirectUrl + "&properties=" + URLEncoder.encode(prop, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("JVM doesn't support UTF-8");
            }
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
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
            for (CavityResponse cr : cds.get(end) ) {
                names.add(cr.getCavityName());
            }
            request.setAttribute("cavityNames", names);
            
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error querying cavity data", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException("Error querying cavity data");            
        }
        System.out.println("HERE");
        request.getRequestDispatcher("/WEB-INF/views/cavity.jsp").forward(request, response);
    }
}
