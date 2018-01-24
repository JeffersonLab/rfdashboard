<%-- 
    Document   : cm-perf
    Created on : Jan 19, 2018, 2:11:06 PM
    Author     : adamc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Cryomodule Performance"></c:set>
<t:reports-page title="${title}}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/cm-perf.css"/>
        <style>
            .comments-table{
                /*margin: 2px;*/
            }
            .accordion-header, .ui-accordion-header {
                align-content: center;
                padding-top: 1em;
                padding-bottom: 0em;
            }
            .accordion-label {
                display: inline-block;
                padding-top: 7px;
            }
            .egain-meter {
                display: inline-block;
                font-size: 75%;
                width:      90%;
                height:     45px;
                text-align: center;
                vertical-align: top;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cm-perf.js"></script>
        <script>
            $(document).ready(function () {

                $(".egain-meter").each(function (i, elem) {
                    var perc = ($(this).data("e-percent") * 1).toFixed(2);
                    var cmType = $(this).data("cmType");

                    var color;
                    switch (cmType) {
                        case "C100":
                            color = jlab.colors.cmtypes[0];
                            break;
                        case "C25":
                            color = jlab.colors.cmtypes[1];
                            break;
                        case "C50":
                            color = jlab.colors.cmtypes[2];
                            break;
                        default:
                            color = jlab.colors.cmtypes[4]; //Unknown                            
                    }

                    var data = [[perc, 0]];
                    var dataset = [{label: "", data: data, color: color}];
                    var ticks = [[0, ""]];

                    var options = {
                        series: {bars: {show: true, fillColor: color}},
                        bars: {barWidth: 0.5, horizontal: true},
                        xaxis: {min: 0, max: 150},
                        yaxis: {ticks: ticks},
                        legend: {show: false},
                        grid: {hoverable: true, borderWidth: 2, backgroundColor: {colors: ["#ffffff", "#EDF5FF"]}
                        }
                    };
                    $.plot($(this), dataset, options);
                });

                $("#accordion").accordion({
                    header: ".accordion-header",
                    collapsible: true,
                    active: false
                });

                var labels = ["C100", "C25", "C50", "Unknown"];
                var jcc = jlab.colors.cmtypes;
                var colors = [jcc[0], jcc[1], jcc[2], jcc[4]];
                jlab.util.addLegend('cm-perf-legend', colors, labels);

                $(".date-field").datepicker({
                    dateFormat: "yy-mm-dd",
                    showButtonPanel: true
                });
            });
        </script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div class="page-title-bar">
                <h2 id="page-header-title"><c:out value="${title}"/></h2>
            </div>
            <div id="control-form">
                <form action="${pageContext.request.contextPath}/reports/cm-perf" method="get">
                    <fieldset>
                        <div class="li-key">
                            <label class="required-field" for="date">Report Date</label>
                        </div>
                        <div class="li-value">
                            <input type="text" class="date-field nowable-field" id="date" name="date" placeholder="YYYY-MM-DD" value="${requestScope.date}"/>
                        </div>
                        <div class="li-key">
                            <label class="required-field" for="sortBy">Sort By</label>
                        </div>
                        <div class="li-value">
                            <select name="sortBy" required="true">
                                <option value="perf" <c:if test='${sortBy == "perf"}'>selected</c:if>>Performance</option>
                                <option value="name" <c:if test='${sortBy == "name"}'>selected</c:if>>Name</option>
                                </select>
                            </div>
                            <input type="submit" value="Submit" />
                        </fieldset>
                    </form>
                </div>
                <div class="accordion-title-wrap">
                    <div class="accordion-title">
                        Cryomodule %Nominal Energy Gain
                    </div>
                    <div id="cm-perf-legend" class="accordion-subtitle">
                    </div>
                </div>
                <div id="accordion" class="report-container">
                <c:forEach var="cm" items="${cmList}">
                    <c:choose>
                        <%--The quarter cavity is non relativistic and only has two cavities.  Just leave it out of the list--%>
                        <c:when test="${cm.cmType == 'QTR'}"></c:when>
                        <c:otherwise>
                            <div class="accordion-header">
                                <div class="accordion-label">${cm.name}</div>
                                <div class="egain-meter" id="${cm.name}-meter" data-cm-type="${cm.cmType}" data-e-percent="${cm.EGainPerformance}"></div>
                            </div>
                            <div class="accordion-content">
                                <div class="table-panel">
                                    <div class="table-title">Cryomodule Data</div>
                                    <div class="table-wrap">
                                        <table class="comments-table">
                                            <thead><tr><th>Name</th><th>Module Type</th><th>EGain (MeV)</th><th>%Nominal</th><th>LEM Heat</th><tr></thead>
                                            <tbody>
                                                <tr>
                                                    <td>${cm.name}</td>
                                                    <td>${cm.cmType}</td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cm.EGain}"></fmt:formatNumber></td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cm.EGainPerformance}"></fmt:formatNumber></td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cm.heat}"></fmt:formatNumber></td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <br/>
                                    <div class="table-panel ">
                                        <div class="table-title">Latest Comments:
                                            <div class="table-links">
                                                <a href="${pageContext.request.contextPath}/comments/new-comment?topic=${cm.name}">New Comment</a>&nbsp;
                                            <a href="${pageContext.request.contextPath}/comments/history?topic=${cm.name}">Full History</a>
                                        </div>
                                    </div>
                                    <div class="table-wrap">
                                        <t:comments-table comments="${commentMap[cm.name]}"></t:comments-table>
                                        </div>
                                    </div>
                                </div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </section>
    </jsp:body>
</t:reports-page>