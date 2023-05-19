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
        String out = "{\"channels\":{\"R12XHTPLEM\":{\"metadata\":{\"name\":\"R12XHTPLEM\",\"datatype\":\"DBR_DOUBLE\",\"datasize\":1,\"datahost\":\"opsmya8\",\"ioc\":\"iocnl1\",\"active\":true},\"data\":[{\"d\":\"2023-01-01T00:00:00\",\"v\":72.117897}],\"returnCount\":1},\"R13XHTPLEM\":{\"metadata\":{\"name\":\"R13XHTPLEM\",\"datatype\":\"DBR_DOUBLE\",\"datasize\":1,\"datahost\":\"opsmya13\",\"ioc\":\"iocnl1\",\"active\":true},\"data\":[{\"d\":\"2023-01-01T00:00:00\",\"v\":66.913399}],\"returnCount\":1}}}";
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
