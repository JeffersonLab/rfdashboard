<%-- 
    Document   : energy-reach
    Created on : Apr 17, 2017, 2:06:06 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Energy Reach" />
<t:page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/flot-barchart.css"/>
        <!--<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/tablesorter.css"/>-->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/energy-reach.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-charts.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/energy-reach.js"></script>

    </jsp:attribute>
    <jsp:body>
        <style>
            .li-value button {
                display: inline-block
            }
        </style>
        <div class="page-title-bar">
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            (<a href="#" id="page-details-opener" title="Page Details">Details</a>)
        </div>
        <div id="page-details-dialog" class='dialog' title="Details">
            <h3> Energy Reach </h3>
            Energy reach is defined here as the linac energy with an expected total of eight C25 TrueArc trips per hour.
            Energy reach values are calculated by running the Linac Energy Manager (LEM) tool at linac energies ranging from 1000 MeV to 1190 MeV
            in 5 MeV steps, saving the trip rate at each step and interpolating the energy that produces eight C25 TrueArc trips per hour.
            LEM requires that an operation EPICS control system be up and responding.  This may result in a lack of data being produced or display during
            accelerator downs.  These LEM operations are done using a default value for linac beam current of 400 uA.
            <h3>Page Controls</h3>
            <ul>
                <li>
                    Start Date - The start date to be displayed in the energy reach chart
                </li>
                <li>
                    End Date - The end date to be displayed in the energy reach chart
                </li>
                <li>
                    Delta Start - Start date to use in the "Cavity Set Point Delta" tables.
                </li>
                <li>
                    Delta End - End date to use in the "Cavity Set Point Delta" tables.  Also specifies the C25 trip rate chart date.
                </li>
            </ul>
            Note: Dates assume 12 AM at the start of the specified day.
            <br><br>
            Delta Start/End dates can also be controlled by clicking on bars in the energy reach chart.
            <h3> Charts and Tables </h3>
            The "Linac Energy Reach" chart displays CEBAF energy reach over time.  Clicking on individual bars or manually selecting
            "Delta Start" and "Delta End" dates will update the rest of this page to display detailed information from those dates or date.
            Energy reach data is available for dates starting Dec 16, 2016.
            <br><br>
            The "C25 Trip Rates" chart displays C25 trips/hour vs. energy curves for the North and South Linacs as well as combined.
            <br><br>
            The "Cavity Set Point Deltas" table shows changes cavity-specific set-point changes from "Delta Start" to "Delta End."
            These are the archived EPICS settings and historical CED values from midnight on the stated dates  This
            table can be sorted by clicking on the column hears.  Complex sorting can be achieved by "Shift+Click"ing the headers.
            There are both "Basic" and "Advanced" versions of this table that can be toggled using the "Basic/Advanced" button.
        </div>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/energy-reach" method="get">
                <fieldset>
                    <t:calendar-start-end id="main-calendar" start="${requestScope.start}" end="${requestScope.end}" startLabel="Start Date" endLabel="End Date"></t:calendar-start-end>
                    <t:calendar-start-end id="delta-calendar" start="${requestScope.diffStart}" end="${requestScope.diffEnd}" startLabel="Delta Start" endLabel="Delta End"></t:calendar-start-end>
                    <input type="submit" value="Submit" />
                </fieldset>
            </form>
        </div>
        <t:chart-widget placeholderId="energy-reach"></t:chart-widget>
            <br><hr><br>
        <t:chart-widget placeholderId="lem-scan"></t:chart-widget>
            <hr><br>
            <button id="menu-toggle">Basic/Advanced</button>
        <t:tablesorter tableTitle="Cavity Set Point Deltas<br/>(${requestScope.diffStart} to ${requestScope.diffEnd})" widgetId="diff-table-basic" filename="${requestScope.start}_${requestScope.end}_cavBasic.csv"></t:tablesorter>
        <t:tablesorter tableTitle="Cavity Set Point Deltas<br/>(${requestScope.diffStart} to ${requestScope.diffEnd})" widgetId="diff-table-advanced" filename="${requestScope.start}_${requestScope.end}_cavAdv.csv"></t:tablesorter>
        <t:tablesorter tableTitle="Cavity Set Point Summary<br/>(${requestScope.diffStart} to ${requestScope.diffEnd})" widgetId="summary-table" filename="${requestScope.start}_${requestScope.end}_cavSummary.csv"></t:tablesorter>
            <script>
                var jlab = jlab || {};
                jlab.start = "${requestScope.start}";
                jlab.end = "${requestScope.end}";
                jlab.diffStart = "${requestScope.diffStart}";
                jlab.diffEnd = "${requestScope.diffEnd}";
                jlab.timeUnit = "${requestScope.timeUnit}";
                jlab.energyReachData = ${requestScope.energyReach};
                jlab.dayScanData = ${requestScope.dayScan};
                jlab.cavityData = ${requestScope.cavityData};
                jlab.myaURL = "${requestScope.myaURL}";

        </script>
    </jsp:body>
</t:page>