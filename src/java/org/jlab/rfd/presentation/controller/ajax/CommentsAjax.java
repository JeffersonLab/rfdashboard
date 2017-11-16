package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CommentService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.business.util.SessionUtil;
import org.jlab.rfd.model.Comment;

/**
 * Controller for inserting and querying comments from the rfd_comments database
 * table
 *
 * @author adamc
 */
@WebServlet(name = "CommentsAjax", urlPatterns = {"/ajax/comments"})
public class CommentsAjax extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CommentsAjax.class.getName());

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        // The SessionUtil method throws an exception if not authenticated
        String username = SessionUtil.checkAuthenticated(request);
        String topic = request.getParameter("topic");
        String comment = request.getParameter("comment");
        Date now = Calendar.getInstance().getTime();
        PrintWriter pw = response.getWriter();

        if (topic == null || comment == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String error = "{error: \"Required parameters are topic and comment\"}";
            pw = response.getWriter();
            pw.write(error);
            pw.flush();
            return;
        }

        CommentService cs = new CommentService();
        try {
            cs.makeComment(username, topic, now, comment);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            String error = "{error: \"Error submitting comment to database\"}";
            pw.write(error);
            pw.flush();
            return;
        }
        
        pw.write("{\"data\": \"success\"}");
        pw.flush();
    }

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

        // Only one content type support here
        response.setContentType("application/json");

        // First check if we are reading comments or making a new one
        String error = null;

        // If reading, then we can get everything or filter by username, topic, and timestamps (start/end).  Check for these
        // options and pass them on to the proper service
        List<String> users = null;
        String user = request.getParameter("user");
        if (user != null) {
            users = Arrays.asList(user.split(","));
        }
        List<String> topics = null;
        String topic = request.getParameter("topic");
        if (topic != null) {
            topics = Arrays.asList(topic.split(","));
        }

        String s = request.getParameter("start");
        String e = request.getParameter("end");
        Date start = null;
        Date end = null;
        try {
            if (s != null) {
                start = DateUtil.parseDateStringYMDHMS(s);
                System.out.println(start.toString());

            }
            if (e != null) {
                end = DateUtil.parseDateStringYMDHMS(e);
            }
        } catch (ParseException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            error = "{error: 'unable to process start or end parameters'}";
        }

        CommentService cs = new CommentService();
        List<Comment> comments = null;
        try {
            comments = cs.getComments(users, topics, start, end);
        } catch (SQLException | ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error querying database for comments");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            error = "{error: 'Error querying database for comments'}";
        }

        PrintWriter pw = response.getWriter();
        if (error != null) {
            pw.print(error);
            pw.flush();
        } else {
            if (comments != null) {
                JsonObjectBuilder job = Json.createObjectBuilder();
                JsonArrayBuilder jab = Json.createArrayBuilder();
                for (Comment com : comments) {
                    jab.add(com.toJsonObject());
                    // TODO: Consider using a SortedList for comments so that the results will always be in chronological order
                }
                JsonObject out = job.add("data", jab.build()).build();
                JsonWriter jw = Json.createWriter(pw);
                jw.writeObject(out);
            } else {
                pw.print("{data: []}");
                pw.flush();
            }
        }
    }
}
