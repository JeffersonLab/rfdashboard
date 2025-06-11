<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Cavity Performance"></c:set>
<t:reports-page title="${title}">
    <jsp:attribute name="stylesheets">
        <style>
            .accordion-header, .ui-accordion .ui-accordion-header {
                display: flex;
                padding-top: 0;
                padding-bottom: 0;
            }
            .accordion-label {
                flex-grow: 0;
            }
            .egain-meter {
                flex: 1;
            }
            .egain-meter-tick-container {
                display: flex;
                flex-direction: column;
                justify-content: center;
            }
            .egain-meter-tick {
                flex-grow: 0;
                font-size: 60%;
            }
            #accordion-scroll-box {
                min-height: 200px;
                max-height: 600px;
                overflow-y: auto;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script>
            $(document).ready(function () {
                // F100 and C50T are lumped in with C100 and C50, respectively.
                var labels = ["C100", "C25", "C50", "C75"];
                var jcc = jlab.colors;
                var colors = [jcc["C100"], jcc["C25"], jcc["C50"], jcc["C75"]];
                jlab.util.addLegend('cav-perf-legend', colors, labels, true);

                $(".date-field").datepicker({
                    dateFormat: "yy-mm-dd",
                    showButtonPanel: true
                });

                $(".egain-meter").each(function (i, elem) {
                    var perc = ($(this).data("e-percent") * 1).toFixed(2);
                    var cavityType = $(this).data("cav-type");

                    $("#accordion").accordion({
                        header: ".accordion-header",
                        collapsible: true,
                        active: false,
                        heightStyle: "content"
                    });

                    var color;
                    switch (cavityType) {
                        // J. Benesch requested this grouping for one-off CM types
                        case "C25":
                            color = jlab.colors["C25"];
                            break;
                        // J. Benesch requested this grouping for one-off CM types
                        case "C50T":
                        case "C50":
                            color = jlab.colors["C50"];
                            break;
                        case "C75":
                            color = jlab.colors["C75"];
                            break;
                        case "P1R":
                        case "C100":
                            color = jlab.colors["C100"];
                            break;
                        default:
                            color = "#A9A9A9"; //A different type of unknown. I don't want to include in the legend and this looks different enough to avoid confusion.
                    }

                    var data = [[perc, 0]];
                    var dataset = [{label: "", data: data, color: color}];
                    var ticks = [[0, ""]];

                    var options = {
                        series: {bars: {show: true, fillColor: color}},
                        bars: {barWidth: 0.25, horizontal: true},
                        xaxis: {min: 0, max: 200, ticks: ticks},
                        yaxis: {ticks: ticks},
                        legend: {show: false},
                        grid: {hoverable: true, borderWidth: 2, backgroundColor: {colors: ["#ffffff", "#EDF5FF"]}
                        }
                    };
                    $.plot($(this), dataset, options);
                });

            });
            
            jlab.tableSorter.initCommentDialogs(".table-panel");
        </script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div class="page-title-bar">
                <h2 id="page-header-title"><c:out value="${title}"/></h2>
            </div>
            <div id="control-form">
                <form action="${pageContext.request.contextPath}/reports/cavity-perf" method="get">
                    <fieldset>
                        <div class="li-key">
                            <label class="required-field" for="date">Report Date</label>
                        </div>
                        <div class="li-value">
                            <input type="text" class="date-field nowable-field" id="date" name="date" placeholder="YYYY-MM-DD" value="${requestScope.date}"/>
                        </div>
                        <div class="li-key">
                            <label class="required-field" for="select-sortBy">Sort By</label>
                        </div>
                        <div class="li-value">
                            <select id="select-sortBy" name="sortBy" required="true">
                                <option value="perf" <c:if test='${requestScope.sortBy == "perf"}'>selected</c:if>>Performance</option>
                                <option value="name" <c:if test='${requestScope.sortBy == "name"}'>selected</c:if>>Name</option>
                            </select>
                        </div>
                        <div class="li-key">
                            <label class="required-field" for="select-linac">Linac</label>
                        </div>
                        <div class="li-value">
                            <select id="select-linac" name="linac" required="true">
                                <option value="inj" <c:if test='${requestScope.linac == "inj"}'>selected</c:if>>Injector</option>
                                <option value="nl" <c:if test='${requestScope.linac == "nl"}'>selected</c:if>>North</option>
                                <option value="sl" <c:if test='${requestScope.linac == "sl"}'>selected</c:if>>South</option>
                            </select>
                        </div>

                        <div class="li-key">
                            <label class="required-field" for="select-cavtype">Cavity Type</label>
                        </div>
                        <div class="li-value">
                            <select id="select-cavtype" name="cavtype" required="true">
                                <option value="C25" <c:if test='${requestScope.cavType == "C25"}'>selected</c:if>>C25</option>
                                <option value="C50" <c:if test='${requestScope.cavType == "C50"}'>selected</c:if>>C50</option>
                                <option value="C75" <c:if test='${requestScope.cavType == "C75"}'>selected</c:if>>C75</option>
                                <option value="C100" <c:if test='${requestScope.cavType == "C100"}'>selected</c:if>>C100</option>
                                <option value="all" <c:if test='${requestScope.cavType == "all"}'>selected</c:if>>All</option>
                            </select>
                        </div>

                        <input type="submit" value="Submit" />
                        </fieldset>
                    </form>
                </div>
                <div class="accordion-title-wrap">
                    <div class="accordion-title">
                        RF Cavity %Nominal Energy Gain
                    </div>
                    <div id="cav-perf-legend" class="accordion-subtitle">
                    </div>
                </div>
                <div id="accordion-scroll-box">
                <div id="accordion" class="report-container">
                <c:forEach var="cav" items="${requestScope.cavList}">
                    <c:choose>
                        <%--The quarter cavity is non relativistic and only has two cavities.  Just leave it out of the list per J. Benesch--%>
                        <c:when test="${cav.cryomoduleType == 'QTR'}"></c:when>
                        <c:otherwise>
                            <div class="accordion-header">
                                <div class="accordion-label">${cav.cavityName}</div>
                                <div class="egain-meter" id="${cav.cavityName}-meter" data-cav-type="${cav.cavityType}" data-e-percent="${cav.EGainPerformance}"></div>
                                <div class="egain-meter-tick-container"><div class="egain-meter-tick">200%</div></div>
                            </div>
                            <div class="accordion-content">
                                <div class="table-panel">
                                    <div class="table-title">Cavity Data</div>
                                    <div class="table-wrap">
                                        <table class="comments-table">
                                            <thead><tr><th>Name</th><th>Cavity Type</th><th>GSET</th><th>ODVH</th><th>MaxGSET</th><th>ODVH/MaxGSET</th><th>GSET/ODVH</th><th>EGain %Nominal</th><tr></thead>
                                            <tbody>
                                                <tr>
                                                    <td>${cav.cavityName}</td>
                                                    <td>${cav.cavityType}</td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cav.gset}"></fmt:formatNumber> MV/m</td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cav.odvh}"></fmt:formatNumber> MV/m <span class='ui-icon ui-icon-comment comment-dialog' data-jlab-cavity='${cav.cavityName}' data-jlab-cav-property='OpsGsetMax'></span></td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cav.maxGset}"></fmt:formatNumber> MV/m <span class='ui-icon ui-icon-comment comment-dialog' data-jlab-cavity='${cav.cavityName}' data-jlab-cav-property='MaxGSET'></span></td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cav.odvh / cav.maxGset * 100.0}"></fmt:formatNumber>%</td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cav.gset / cav.odvh * 100.0}"></fmt:formatNumber>%</td>
                                                    <td><fmt:formatNumber maxFractionDigits="2" value="${cav.EGainPerformance}"></fmt:formatNumber>%</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                <br/>
                                <div class="table-panel">
                                    <div class="table-title">Latest Comments:
                                        <div class="table-links">
                                            <a href="${pageContext.request.contextPath}/comments/new-comment?topic=${cav.cavityName}">New Comment</a>&nbsp;
                                            <a href="${pageContext.request.contextPath}/comments/history?topic=${cav.cavityName}">Full History</a>
                                        </div>
                                    </div>
                                    <div class="table-wrap">
                                        <t:comments-table comments="${commentMap[cav.cavityName]}"></t:comments-table>
                                        </div>
                                    </div>
                                </div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
            </div>
        </section>
    </jsp:body>
</t:reports-page>