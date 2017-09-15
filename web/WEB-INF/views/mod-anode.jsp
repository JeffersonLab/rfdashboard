<%-- 
    Document   : ModAnode
    Created on : Mar 24, 2017, 4:59:31 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Mod Anode Summary" />
<t:page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/mod-anode.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/flot-barchart.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-charts.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/mod-anode.js"></script>
    </jsp:attribute>
    <jsp:body>

        <div class="page-title-bar">
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            (<a href="#" id="page-details-opener">Details</a>)
        </div>
        <div id="page-details-dialog" title="Details">
            <h3>Mod Anode Voltage</h3>
            Mod Anode Voltage is applied to klystrons to extend the life of aging klystron tubes.  This data is updated in the EPICS
            control system and is then manually ported to the CED.  LEM uses the CED ModAnode property of CryoCavity elements
            when calculating C25 trip rates and specifying individual cavity GSETs.
            <h3>Page Controls</h3>
            <ul>
                <li>
                    Start Date - The start date to be displayed in all of the mod anode charts
                </li>
                <li>
                    End Date - The end date to be displayed in all of the mod anode charts
                </li>
                <li>
                    Table Date - The date to use in all mod anode voltage related tables.
                </li>
                <li>
                    TimeUnit - The time interval between chart data points.  Day shows every day, Week shows every seventh day
                </li>
            </ul>
            Note: Dates assume 12 AM at the start of the specified day.
            <br><br>
            Table date can also be controlled by clicking on bars in the mod anode charts.
            <h3> Charts and Tables </h3>
            The "LEMSim Estimated Trip Impact of Mod Anode Voltage" chart displays the estimated number of C25 TrueArc trips
            per hour at two energies, 1050 MeV and 1090 MeV in both a standard configuration and in a hypothetical scenario where
            all mod anode voltages were set to zero.  This data is available for dates starting July 25, 2017.  These estimates
            are generated daily and are based on archived EPICS values from CEBAF's last runnable configuration as specified in
            the ModAnodeHarvester.cfg file at the time of the scan and on the CED values at the time of the scan.  These CED values should 
            be current at the time of the scan, but they will not reflect any recent, unmerged "Live Edits."
            <br><br>
            The "LEMSim Estimated Mod Anode Voltage Affects" table displays LEMSim suggested per-cavity GSET set points
            at 1050 MeV and 1090 MeV, both with and without mod anode voltages.  This table supports multiple column sorts by
            "Shift-Click"ing on the column headers.
            <br><br>
            The "Cavities with Mod Anode Voltage" charts show how the count of cavities with mod anode voltage split amongst 
            two categories, linac and cryomodule type.  This data pulled from the CED and the Archiver.  Clicking on the bars of this
            chart will update the Table Date accordingly.
            <br><br>
            The "Cavities with Non-Zero Mod Anode Voltage" display per-cavity Mod Anode Voltage and EPCIS setpoint for GSET at
            12AM of the table date.  This table supports multiple column sorts by "Shift-Click"ing on the column headers.
        </div>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/mod-anode" method="get">
                <fieldset>
                    <div class="li-key">
                        <label class="required-field" for="start" title="Inclusive (Closed)">Start Date</label>
                        <div class="date-note">(Inclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field" id="start" name="start" placeholder="YYYY-MM-DD" value="${requestScope.start}"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="end" title="Exclusive (Open)">End Date</label>
                        <div class="date-note">(Inclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD" value="${requestScope.end}"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="tableDate" title="Table Date">Table Date</label>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field nowable-field" id="tableDate" name="tableDate" placeholder="YYYY-MM-DD" value="${requestScope.tableDate}"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="timeUnit" title="Time Interval">Time Units</label>
                    </div>
                    <div class="li-value">
                        <select id="timeUnit" name="timeUnit">
                            <option value="day"${(param.timeUnit eq 'day') ? ' selected="selected"' : ''}>Day</option>
                            <option value="week"${(param.timeUnit eq 'week') ? ' selected="selected"' : ''}>Week</option>
                        </select>
                    </div>
                    <input type="submit" value="Submit" />
                </fieldset>
            </form>
        </div>
        <t:chart-widget placeholderId="mav-mah-trip-impact"></t:chart-widget>
        <hr></hr><br></br>
        <t:tablesorter tableTitle="LEMSim-Based Mod Anode Voltage Affects (${requestScope.tableDate})" widgetId="mav-mah-table" filename="${requestScope.tableDate}_LEMSim_MAV.csv"></t:tablesorter>
        <hr></hr><br></br>
        <t:chart-widget placeholderId="mav-count-by-linac"></t:chart-widget>
        <hr></hr><br></br>
        <t:chart-widget placeholderId="mav-count-by-cmtype"></t:chart-widget>
        <hr></hr><br></br>
        <t:tablesorter tableTitle="Mod Anode Voltage By Cavities (${requestScope.tableDate})" widgetId="mav-table" filename="${requestScope.tableDate}_MAV.csv"></t:tablesorter>
        <script>
            var jlab = jlab || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
            jlab.timeUnit = "${requestScope.timeUnit}";
            jlab.tableDate = "${requestScope.tableDate}";
        </script>
    </jsp:body>
</t:page>