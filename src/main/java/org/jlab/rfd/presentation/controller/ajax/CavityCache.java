package org.jlab.rfd.presentation.controller.ajax;

import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CavityResponse;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "CavityCache", value = "/ajax/cavity-cache")
public class CavityCache extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CavityCache.class.getName());
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String date = request.getParameter("date");  // The date to operate on
        String secret = request.getParameter("secret"); // A "secret" to keep prevent accidentally cache clears
        String action = request.getParameter("action"); // "read" or "clear".  clear requires the "secret"

        response.setContentType("application/json");

        String error = null;
        Date d = null;
        String reqSecret = "ayqs";
        try {
            d = DateUtil.parseDateStringYMD(date);
        } catch (ParseException e) {
            error = "Error parsing date.  Requires YYYY-mm-dd format.";
        }

        // Default to read action
        action = action == null ? "read" : action;
        switch (action){
            case "read":
                break;
            case "clear":
                if (secret == null || !secret.equals(reqSecret)) {
                    error = "Invalid secret";
                }
                break;
            default:
                error = "Invalid action selection '" + action + "'.  Options = (read, clear)";
        }

        if (error != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(response, error);
            return;
        }

        CavityService cs = new CavityService();
        if (action.equals("clear")) {
            int numCleared;
            try {
                numCleared = cs.clearCache(d);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeError(response, e.getMessage());
                return;
            }
            try (PrintWriter pw = response.getWriter()) {
                pw.write("{\"response\": \"Success\", \"rowsCleared\": " + numCleared + "}");
            }
        } else {
            try {
                Set<CavityResponse> data = cs.createCommentResponseSet(cs.readCache(d), new HashMap<>());
                CavityDataSpan span = new CavityDataSpan();
                span.put(d, data);
                try (PrintWriter pw = response.getWriter()) {
                    String json = span.toJson().toString();
                    pw.write("{\"response\": \"Success\", \"data\": " + json + "}");
                }
            } catch (SQLException|ParseException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeError(response, e.getMessage());
            }
        }
    }

    private void writeError(HttpServletResponse response, String error) throws IOException {
        try(PrintWriter pw = response.getWriter()) {
            pw.write("{\"error\": \"" + error + "\"}");
        }
    }
}
