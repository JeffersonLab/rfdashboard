package org.jlab.rfd.test;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "MySampler", value = "/mySampler/data")
public class MySampler extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String out = "{\"data\":[{\"date\":\"2018-01-01T00:00:00\",\"values\":[{\"R12XHTPLEM\":\"73.4588\"},{\"R13XHTPLEM\":\"86.6749\"}]}]}";
        response.setContentType("application/json");
        try(PrintWriter pw = response.getWriter()) {
            pw.println(out);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
