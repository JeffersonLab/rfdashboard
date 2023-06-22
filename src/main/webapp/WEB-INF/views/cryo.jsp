<%-- 
    Document   : ModAnode
    Created on : Mar 24, 2017, 4:59:31 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Cryo" />
<t:page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/flot-barchart.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/cryo.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-charts.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cryo.js"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="page-title-bar">
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            (<a href="#" id="page-details-opener" title="Page Details">Details</a>)
        </div>
        <div id="page-details-dialog" class='dialog' title="Details">
            <h3> Linac Cryogen Pressure </h3>
            Linac cryogen pressure refers to helium pressure as measured at the "T" on each linac.  Specific PVs are listed for extra
            clarity.
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
                    Delta End - End date to use in the "Cavity Set Point Delta" tables.
                </li>
                <li>
                    TimeUnit - The size of the time interval represented between chart data points.
                </li>
            </ul>
            Note: Dates assume 12 AM at the start of the specified day.
            <br><br>
            Delta Start/End dates can also be controlled by clicking on chart elements in the Linac Cryogen Pressure chart.
            <h3> Charts and Tables </h3>
            The "Linac Cryogen Pressure" chart displays the helium pressure as measured at the "T" on each Linac.  The EPICS
            signal is split into bins whose size is based on the "TimeUnits" parameter and whose start is based on "Start Date"/"End Date".
            The point represents the average PV value during the bin and the error bars represent +/- one standard deviation for that bin.
            Energy reach data is also displayed on this chart where available.  Please note however, the LEM model does not directly include the
            effects of cryogen pressure.
            <br><br>
            This chart supports panning and zooming.  Clicking on points updates the "Delta Start"/"Delta End" values to match the
            date range of that interval.  Note: These statistics are calculated for each query and may require significant time to display
            based on the size of request.
            <br><br>
            The "Cavity Set Point Deltas" table shows changes cavity-specific set-point changes from "Delta Start" to "Delta End."
            These are the archived EPICS settings from midnight on the stated dates  This table can be sorted by clicking on the
            column hears.  Complex sorting can be achieved by "Shift+Click"ing the headers.  Data from this table is gathered from
            the MYA Archiver and the CED.  There are both "Basic" and "Advanced" versions of this table that can be toggled using
            the "Basic/Advanced" button.
            <br><br>
        </div>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/cryo" method="get">
                <fieldset>
                    <div class="input-elem">
                            <t:calendar-start-end id="main-calendar" start="${requestScope.start}" end="${requestScope.end}" startLabel="Start Date" endLabel="End Date"></t:calendar-start-end>
                        </div>
                        <div class="input-elem">
                            <t:calendar-start-end id="delta-calendar" start="${requestScope.diffStart}" end="${requestScope.diffEnd}" startLabel="Delta Start" endLabel="Delta End"></t:calendar-start-end>
                        </div>
                        <div class="input-elem">
                            <div class="li-key">
                                <label class="required-field" for="timeUnit" title="Time Interval">Time Units</label>
                            </div>
                            <div class="li-value">
                                <select id="timeUnit" name="timeUnit">
                                    <option value="day"${(param.timeUnit eq 'day') ? ' selected="selected"' : ''}>Day</option>
                                <option value="week"${(param.timeUnit eq 'week') ? ' selected="selected"' : ''}>Week</option>
                            </select>
                        </div>
                    </div>
                    <input class="controls-submit" type="submit" value="Submit" />
                </fieldset>
            </form>
        </div>
        <t:chart-widget placeholderId="cryo-linac-pressure"></t:chart-widget>
        <hr/><br/>
            <button id="menu-toggle">Basic/Advanced</button>
        <t:tablesorter tableTitle="Cavity Set Point Deltas<br/>(${requestScope.diffStart} to ${requestScope.diffEnd})" widgetId="diff-table-basic" filename="${requestScope.start}_${requestScope.end}_cavBasic.csv"></t:tablesorter>
        <t:tablesorter tableTitle="Cavity Set Point Deltas<br/>(${requestScope.diffStart} to ${requestScope.diffEnd})" widgetId="diff-table-advanced" filename="${requestScope.start}_${requestScope.end}_cavAdv.csv"></t:tablesorter>
        <t:tablesorter tableTitle="Cavity Set Point Totals<br/>(${requestScope.diffStart} to ${requestScope.diffEnd})" widgetId="summary-table" filename="${requestScope.start}_${requestScope.end}_cavSummary.csv"></t:tablesorter>

            <script>
                var jlab = jlab || {};
                jlab.start = "${requestScope.start}";
                jlab.end = "${requestScope.end}";
                jlab.timeUnit = "${requestScope.timeUnit}";
                jlab.diffStart = "${requestScope.diffStart}";
                jlab.diffEnd = "${requestScope.diffEnd}";
                jlab.cavityData = ${requestScope.cavityData};
                jlab.myaURL = "${requestScope.myaURL}";
        </script>
    </jsp:body>
</t:page>