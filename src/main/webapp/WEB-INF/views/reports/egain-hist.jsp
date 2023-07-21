<%-- 
    Document   : egain-hist
    Created on : May 2, 2018, 2:59:57 PM
    Author     : adamc
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib  prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib  prefix="cf" uri="http://jlab.org/rfd/functions"%>

<c:set var="title" value="Energy Gain History" />
<t:reports-page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/flot-barchart.css"/>
        <style>
            .legend-panel {
                width: 0px;
                min-width:0px;
            }
            .select2-selection {
                max-height: 125px;
                overflow-y: auto;
            }
            #multi-chart-header {
                text-align: center;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-charts.js"></script>

        <script>
            var jlab = jlab || {};
            jlab.egainHistory = jlab.egainHistory || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
            jlab.by = "${requestScope.by}";
            jlab.timeUnit = "${requestScope.timeUnit}";
            jlab.egainData = ${requestScope.egainDataJson};
            jlab.cmTypes = ${requestScope.cmTypesByZoneJson};
            jlab.zonesByCMType = ${requestScope.zonesByCMTypeJson};

            // This script draws the energy gain chart for all of the zones given in the egainData JSON object.  egainData should
            // be formatted as a flot data object e.g.,
            // {
            //   labels:["s1","s2",...],
            //   data: [
            //     [ [x1,y1], [x2, y2], ... ],
            //     ...
            //   ],
            // }
            jlab.egainHistory.loadHistoryCharts = function (widgetId, start, end, timeUnit, egainData, cmTypes, by) {
                var allLabels = egainData.labels;
                var allData = egainData.data;
                var plotclick; // event handler for the plotclick event

                // Referenced by plotclick event handlers below
                var numDays = -1;
                if (timeUnit === "week") {
                    numDays = -7;
                }

                switch (by) {
                    case "zone" :
                        for (var i = 0; i < allLabels.length; i++) {
                            var zone = allLabels[i];
                            var data = allData[i];
                            var chartId = zone + "-chart";
                            // Put this back together in a way that flot will like
                            var chartData = {labels: [zone], data: [data]};
                            // Skip the quarter module in the injector
                            if (cmTypes[zone] !== "QTR") {

                                plotclick = function (event, pos, item) {
                                    if (item) {
                                        var zone = item.series.label; // otherwise it references the last value cavity takes in the outside loop.
                                        var cavities = [];
                                        for (var i = 0; i < 8; i++) {
                                            cavities[i] = zone + '-' + (i + 1);
                                        }
                                        var timestamp = item.series.data[item.dataIndex][0];
                                        var dateString = jlab.millisToDate(timestamp);
                                        var url = jlab.contextPath + "/reports/ced-prop-hist?start=" + jlab.addDays(dateString, numDays)
                                                + "&end=" + dateString + "&e=" + cavities.join("&e=") + "&props=OpsGsetMax&"
                                                + "props=MaxGSET&props=TunerBad&props=Bypassed";
                                        window.location.href = url;
                                    }
                                };

                                // Draw the chart for this series
                                jlab.egainHistory.loadHistoryChart(chartId, start, end, timeUnit, chartData, cmTypes[zone], by, plotclick);
                            }
                        }
                        break;
                    case "cavity":
                        for (var i = 0; i < allLabels.length; i++) {
                            var cavity = allLabels[i];
                            var zone = cavity.substring(0, 4);
                            var data = allData[i];
                            var chartId = cavity + "-chart";
                            // Put this back together in a way that flot will like
                            var chartData = {labels: [cavity], data: [data]};

                            // Skip the quarter module in the injector
                            if (cmTypes[zone] !== "QTR") {

                                // Launch the ced-prop-history page for this one cavity
                                plotclick = function (event, pos, item) {
                                    if (item) {
                                        var cavity = item.series.label; // otherwise it references the last value cavity takes in the outside loop.
                                        var timestamp = item.series.data[item.dataIndex][0];
                                        var dateString = jlab.millisToDate(timestamp);
                                        var url = jlab.contextPath + "/reports/ced-prop-hist?start=" + jlab.addDays(dateString, numDays)
                                                + "&end=" + dateString + "&e=" + cavity + "&props=OpsGsetMax&"
                                                + "props=MaxGSET&props=TunerBad&props=Bypassed";
                                        window.location.href = url;
                                    }
                                };

                                // Draw the chart for this series
                                jlab.egainHistory.loadHistoryChart(chartId, start, end, timeUnit, chartData, cmTypes[zone], by, plotclick);
                            }
                        }
                        break;
                    case "cmtype":
                        // Skip the quarter module in the injector
                        for (var i = 0; i < allLabels.length; i++) {
                            var cmType = allLabels[i];
                            if (cmType !== "QTR") {
                                var data = allData[i];
                                var chartId = cmType + "-chart";
                                // Put this back together in a way that flot will like
                                var chartData = {labels: [cmType], data: [data]};

                                // No plotclick function for by==cmtype since the IE11 can't handle a that long of a URL.

                                // Draw the chart for this series
                                jlab.egainHistory.loadHistoryChart(chartId, start, end, timeUnit, chartData, cmType, by, plotclick);
                            }
                        }
                        break;
                }

            };


            jlab.egainHistory.loadHistoryChart = function (chartId, start, end, timeUnit, chartData, cmType, by, plotclick) {
                var barColor, ymin, ymax, nominal;

                // By zone is usually used to compare across zones, so keep the ymax consistent across cmtypes
                // By cavity is usuually use to compare cavities within a zone, so change ymax according to cmtype
                // By cmtype isn't clear when they will look at this other than to get a general idea of where egain is coming from
                //   so let the yaxis auto adjust
                switch (cmType) {
                    case "C100":
                        barColor = jlab.colors["C100"];
                        switch (by) {
                            case "cavity":
                                ymax = 20;
                                ymin = 0;
                                nominal = 12.5;
                                break;
                            case "zone":
                                ymax = 125;
                                ymin = 0;
                                nominal = 100;
                                break;
                            case "cmtype":
                                ymax = null;
                                ymin = null;
                                nominal = -1;
                                break;
                        }
                        break;
                    case "C25":
                        barColor = jlab.colors["C25"];
                        switch (by) {
                            case "cavity":
                                ymax = 10;
                                ymin = 0;
                                nominal = 3.125;
                                break;
                            case "zone":
                                ymax = 50;
                                ymin = 0;
                                nominal = 25;
                                break;
                            case "cmtype":
                                ymax = null;
                                ymin = null;
                                nominal = -1;
                                break;
                        }
                        break;
                    case "C50":
                        barColor = jlab.colors["C50"];
                        switch (by) {
                            case "cavity":
                                ymax = 15;
                                ymin = 0;
                                nominal = 6.25;
                                break;
                            case "zone":
                                ymax = 75;
                                ymin = 0;
                                nominal = 50;
                                break;
                            case "cmtype":
                                ymax = null;
                                ymin = null;
                                nominal = -1;
                                break;
                        }
                        break;
                    case "C50T":
                        barColor = jlab.colors["C50T"];
                        switch (by) {
                            case "cavity":
                                ymax = 15;
                                ymin = 0;
                                nominal = 6.25;
                                break;
                            case "zone":
                                ymax = 75;
                                ymin = 0;
                                nominal = 50;
                                break;
                            case "cmtype":
                                ymax = null;
                                ymin = null;
                                nominal = -1;
                                break;
                        }
                        break;
                    case "C75":
                        barColor = jlab.colors["C75"];
                        switch (by) {
                            case "cavity":
                                ymax = 13;
                                ymin = 0;
                                nominal = 9.375;
                                break;
                            case "zone":
                                ymax = 90;
                                ymin = 0;
                                nominal = 75;
                                break;
                            case "cmtype":
                                ymax = null;
                                ymin = null;
                                nominal = -1;
                                break;
                        }
                        break;
                    case "F100":
                        barColor = jlab.colors["F100"];
                        switch (by) {
                            case "cavity":
                                ymax = 20;
                                ymin = 0;
                                nominal = 12.5;
                                break;
                            case "zone":
                                ymax = 125;
                                ymin = 0;
                                nominal = 100;
                                break;
                            case "cmtype":
                                ymax = null;
                                ymin = null;
                                nominal = -1;
                                break;
                        }
                        break;
                    default:
                        ymax = 125;
                        ymin = 0;
                        nominal = 0;
                        barColor = jlab.colors["Unknown"];
                }

                // Setup the title as a drilldown link if we're showing a cmtyp or zone chart.  Can't drilldown more than the cavity.
                var title = "<strong>" + chartData.labels[0] + " Energy Gain History</strong><br/><div style='font-size:smaller'>" + start + " to " + end + "</div>";
                if (by !== "cavity") {
                    // Assume we're looking at a by===zone chart
                    var drillDownBy = "cavity";
                    var zones = [chartData.labels[0]]; // should only be one series.  If more the zone should be the first label
                    if (by === "cmtype") {
                        drillDownBy = "zone";
                        zones = jlab.zonesByCMType[cmType];
                    }
                    title = "<strong>" + chartData.labels[0] + " Energy Gain History</strong><br/>"
                            + "<div style='font-size:smaller'>" + start + " to " + end + "&nbsp&nbsp"
                            + "(<a href='" + jlab.contextPath + "/reports/egain-hist?start=" + jlab.start
                            + "&end=" + jlab.end + "&by=" + drillDownBy + "&timeUnit=" + timeUnit
                            + "&zone=" + zones.join("&zone=") + "'>" + drillDownBy + " view</a>)" + "</div>";

                }

                if (typeof chartData.data[0].length === 0) {
                    $("#" + chartId).append("<br>" + title + "<br>No data available");
                    return;
                }

                var settings = {
                    colors: [barColor],
                    labels: chartData.labels,
                    timeUnit: timeUnit,
                    title: title,
                    tooltips: true,
                    tooltipX: "Date",
                    tooltipY: "EGain (MeV)",
                    legend: false,
                    chartType: "bar"
                };
                // Flot wants times in milliseconds from UTC
                var xmin = new Date(start).getTime();
                var xmax = new Date(end).getTime();
                var flotOptions = {
                    xaxis: {mode: "time", min: xmin, max: xmax},
                    yaxis: {axisLabel: "Energy Gain (MeV)", min: ymin, max: ymax},
                    grid: {clickable: true, markings: [{yaxis: {from: nominal, to: nominal}, color: "#000000"}]}
                };
                var flotData = [];
                for (i = 0; i < chartData.data.length; i++) {
                    flotData[i] = {data: chartData.data[i], points: {show: false}};
                }

                jlab.hideChartLoading(chartId);
                var plot = jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);
                if (typeof plotclick !== "undefined") {
                    $('#' + chartId).bind("plotclick", plotclick);
                }
            };


            $(document).ready(function () {
                $("#page-details-dialog").dialog(jlab.dialogProperties);
                $("#page-details-opener").click(function () {
                    $("#page-details-dialog").dialog("open");
                });

                // Init the two calendars
                $(".date-field").datepicker({
                    dateFormat: "yy-mm-dd",
                    showButtonPanel: true
                });
                jlab.util.initCalendarStartEnd("#main-calendar");

                // Init the selection widgets
                $(".multi-select").select2({width: "290px", closeOnSelect: false});
                $("#by-selector").select2({width: "290px"});

                var labels = ["C100", "C25", "C50", "C75"];
                var jcc = jlab.colors;
                var colors = [jcc["C100"], jcc["C25"], jcc["C50"], jcc["C75"]];
                jlab.util.addLegend('cm-legend', colors, labels, true);

                var massSelector = function (checkbox, optionText) {
                    if ($(checkbox).is(':checked')) {
                        $("#zone-selector > option:contains('" + optionText + "')").prop("selected", "selected");
                        $("#zone-selector").trigger("change");
                    } else {
                        $("#zone-selector > option:contains('" + optionText + "')").removeAttr("selected");
                        $("#zone-selector").trigger("change");
                    }
                };
                $("#nl-checkbox").click(function () {
                    massSelector(this, '1L');
                });
                $("#sl-checkbox").click(function () {
                    massSelector(this, '2L');
                });

                // Load the charts
                jlab.egainHistory.loadHistoryCharts("egain-chart", jlab.start, jlab.end, jlab.timeUnit, jlab.egainData, jlab.cmTypes, jlab.by);
            });
        </script>

    </jsp:attribute>
    <jsp:body>
        <div class="page-title-bar">
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            (<a href="#" id="page-details-opener" title="Page Details">Details</a>)
        </div>
        <div id="page-details-dialog" class='dialog' title="Details">
            <h3>Energy Gain History</h3>
            <p>
                Energy gain here is calculated for a cryocavity as the length of the cavity multiplied by the cavity gradient.  Aggregate views
                such as the "by zone" or "by cryomodule type" simply sum these cavity energy gains over the cavities within the stated
                category.  Bars represent the values taken from CED and the archiver for midnight (morning) of the specified date.
            </p>
            <p>
                This report features two types of drill down features.  Clicking on a bar within any of the graphs will load the RF Cavity History
                tool updates as captured in the CED for that chart's data set and date range.  Click on the "zone view" or "cavity view" links
                of any chart to see data further broken down by the specified sub-category.
            </p>
            <p>
                Note: In zone and cavity charts, a black line is drawn to indicate the "nominal" energy gain for a give cryomodule type.
            </p>
            <h3>Page Controls</h3>
            <ul>
                <li>Start Date - Earliest date for which to display data </li>
                <li>End Date - Latest date for which to display data</li>
                <li>Time Unit - Interval of time between data points</li>
                <li>Display By - The level for which data should be displayed.</li>
                <li>Zones (optional) - Specify the zones for which data is to be displayed.  An empty list causes data for all zones to be included</li>
            </ul>
        </div>
        <form id="filter-form" method="GET" action="${pageContext.request.contextPath}/reports/egain-hist">
            <fieldset>
                <t:calendar-start-end id="main-calendar" startLabel="<b>Start Date</b>" endLabel="<b>End Date</b>" end="${requestScope.end}" start="${requestScope.start}"></t:calendar-start-end>
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">Time Unit:</div>
                            <div class="li-value">
                                <select name="timeUnit">
                                    <option value="day" <c:if test="${requestScope.timeUnit == 'day'}"> selected="selected"</c:if>>Day</option>
                                <option value="week" <c:if test="${requestScope.timeUnit == 'week'}"> selected="selected"</c:if>>Week</option>
                                </select>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">Display By:</div>
                            <div class="li-value">
                                <select name="by">
                                    <option value="cmtype" <c:if test="${requestScope.by == 'cmtype'}"> selected="selected"</c:if>>CMType</option>
                                <option value="zone" <c:if test="${requestScope.by == 'zone'}"> selected="selected"</c:if>>Zone</option>
                                <option value="cavity" <c:if test="${requestScope.by == 'cavity'}"> selected="selected"</c:if>>Cavity</option>
                                </select>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">Zones (optional):</div>
                            <div class="li-value">
                                <select id="zone-selector" class="multi-select" name="zone" multiple="multiple">
                                <c:forEach var="zone" items="${requestScope.allZones}">
                                    <c:if test="${requestScope.cmTypesByZone[zone] != 'QTR'}">
                                        <option value="${zone}" <c:if  test="${cf:inList(zones, zone)}">selected="selected"</c:if>>${zone}</option>
                                        <%--<option value="${zone}" <c:if  test="${zones.contains(zone)}">selected="selected"</c:if>>${zone}</option>--%>
                                    </c:if>
                                </c:forEach>
                            </select>
                                <input type="checkbox" id="nl-checkbox"><label for="nl-checkbox">NL</label>
                                <input type="checkbox" id="sl-checkbox"><label for="sl-checkbox">SL</label>
                        </div>
                    </li>
                </ul>
                <input type="submit" value="Submit">
            </fieldset>
        </form>
        <div id="egain-charts">
            <div id="multi-chart-header">
                <div>Cryomodule Key</div>
                <div id="cm-legend" class="accordion-subtitle"></div>                
            </div>
            <c:choose>
                <c:when test="${requestScope.by == 'zone'}">
                    <c:forEach  items="${requestScope.cmTypesByZone}" var="cmType">
                        <c:if test="${cmType.value != 'QTR'}">
                            <hr>
                            <t:chart-widget placeholderId="${cmType.key}-chart"></t:chart-widget>
                                <hr>
                        </c:if>
                    </c:forEach>
                </c:when>
                <c:when test="${requestScope.by == 'cavity'}">
                    <c:forEach  items="${requestScope.cmTypesByZone}" var="cmType">
                        <c:if test="${cmType.value != 'QTR'}">
                            <hr>
                            <t:chart-widget placeholderId="${cmType.key}-1-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-2-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-3-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-4-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-5-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-6-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-7-chart"></t:chart-widget>
                                <hr>
                                <hr>
                            <t:chart-widget placeholderId="${cmType.key}-8-chart"></t:chart-widget>
                                <hr>
                        </c:if>
                    </c:forEach>
                </c:when>
                <c:when test="${requestScope.by == 'cmtype'}">
                    <c:forEach  items="${requestScope.cmTypeSet}" var="cmType">
                        <c:if test="${cmType != 'QTR'}">
                            <hr>
                            <t:chart-widget placeholderId="${cmType}-chart"></t:chart-widget>
                                <hr>
                        </c:if>
                    </c:forEach>
                </c:when>
            </c:choose>
        </div>
    </jsp:body>
</t:reports-page>