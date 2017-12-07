<%-- 
    Document   : ModAnode
    Created on : Mar 24, 2017, 4:59:31 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Bypassed Cavity Summary" />
<t:page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/flot-barchart.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/bypassed.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-charts.js"></script>
        <script>

        </script>
    </jsp:attribute>
    <jsp:body>
        <div class="page-title-bar">
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            (<a href="#" id="page-details-opener" title="Page Details">Details</a>)
        </div>
        <div id="page-details-dialog" class="dialog" title="Details">
            <h3> Bypassed Cavities </h3>
            Bypassed cavities are here defined as those cavities with an EPICS GSET value of zero.  This data is pulled from the
            archiver with supporting data from the CED.
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
            Table dates can also be controlled by clicking on bars in the energy reach chart.
            <h3> Charts and Tables </h3>
            The "Bypassed Cavities" charts display the number of bypassed cavities broken down by cryomodule type and linac.
            Clicking on the bars of this chart will update the Table Date accordingly.
            <br><br>
            The "Bypassed Cavities" table lists per-cavity information for the cavities that were bypassed at 12 AM on the specified
            table date.  This table supports multiple column sorts by "Shift-Click"ing on the column headers.
            <br><br>
        </div>

        <div id="control-form">
            <form action="${pageContext.request.contextPath}/bypassed" method="get">
                <fieldset>
                    <div class="li-key">
                        <label class="required-field" for="start">Start Date</label>
                        <div class="date-note">(Inclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field" id="start" name="start" placeholder="YYYY-MM-DD" value="${requestScope.start}"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="end">End Date</label>
                        <div class="date-note">(Inclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD" value="${requestScope.end}"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="tableDate">Table Date</label>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field nowable-field" id="tableDate" name="tableDate" placeholder="YYYY-MM-DD" value="${requestScope.tableDate}"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="timeUnit">Time Units</label>
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
        <t:chart-widget placeholderId="bypassed-count-by-linac"></t:chart-widget>
            <hr><br>
        <t:chart-widget placeholderId="bypassed-count-by-cmtype"></t:chart-widget>
            <hr><br>
        <t:tablesorter tableTitle="Bypassed Cavities (${requestScope.tableDate})" widgetId="bypassed-table" filename="${requestScope.tableDate}_bypassed.csv"></t:tablesorter>
            <script>
                var jlab = jlab || {};
                jlab.start = "${requestScope.start}";
                jlab.end = "${requestScope.end}";
                jlab.timeUnit = "${requestScope.timeUnit}";
                jlab.tableDate = "${requestScope.tableDate}";
                jlab.tableData = ${requestScope.tableData};
                jlab.bypassedCMType = ${requestScope.bypassedCMType};
                jlab.bypassedLinac = ${requestScope.bypassedLinac};
        </script>
    </jsp:body>
</t:page>