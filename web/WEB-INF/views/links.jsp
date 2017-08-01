<%-- 
    Document   : error
    Created on : May 1, 2017, 11:15:28 AM
    Author     : adamc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Links">  
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2>Links</h2>
            <h3>General</h3>
            <ul>
                <li>
                    <a href="http://opsweb.acc.jlab.org/abil/pro/">Accelerator Bypassed-Interlocks Log (ABIL)</a>
                </li>
                <li>
                    <a href="https://logbooks.jlab.org/">Jefferson Lab Electronic Logbook (eLog)</a>
                </li>
                <li>
                    <a href="http://opsweb.acc.jlab.org/CSUEApps/atlis/atlis.php">Accelerator Task List (ATLis)</a>
                </li>
                <li>
                    <a href="http://opsntsrv.acc.jlab.org/ops_docs/online_document_files/MCC_online_files/mainmachine_beamline_dwg.pdf">Beamline Map</a>
                </li>
                <li>
                    <a href="https://www.jlab.org/MEgroup/12GeVacc.html">Songsheets</a>
                </li>
            </ul>
            <h3>Tools</h3>
            <ul>
                <li>
                    <a href="https://accweb.acc.jlab.org/dtm/reports/fsd-summary">FSD Trip Summary</a>
                </li>
            </ul>
        </section>
    </jsp:body>         
</t:page>