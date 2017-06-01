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
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/flot-barchart.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/tablesorter.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/cryo.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/errorbars/1.0.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/navigate/1.3.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/chart-widget.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-barchart.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cavity.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-error-bar-chart.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/lib/jquery.tablesorter.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/lib/jquery.tablesorter.pager.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cryo.js"></script>
        <script>

        </script>
    </jsp:attribute>
    <jsp:body>
        <h2 id="page-header-title"><c:out value="${title}"/></h2>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/cryo" method="get">
                <fieldset>
                    <div class="input-elem">
                        <div class="li-key">
                            <label class="required-field" for="start" title="Inclusive (Closed)">Start Date</label>
                            <div class="date-note">(Inclusive)</div>
                        </div>
                        <div class="li-value">
                            <input type="text" class="date-field" id="start" name="start" placeholder="YYYY-MM-DD" value="${requestScope.start}"/>
                        </div>
                    </div>
                    <div class="input-elem">
                        <div class="li-key">
                            <label class="required-field" for="end" title="Exclusive (Open)">End Date</label>
                            <div class="date-note">(Exclusive)</div>
                        </div>
                        <div class="li-value">
                            <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD" value="${requestScope.end}"/>
                        </div>
                    </div>
                    <div class="input-elem">
                        <div class="li-key">
                            <label class="required-field" for="diffStart" title="">Delta Start</label>
                        </div>
                        <div class="li-value">
                            <input type="text" class="date-field" id="diffStart" name="diffStart" placeholder="YYYY-MM-DD" value="${requestScope.diffStart}"/>
                        </div>
                    </div>
                    <div class="input-elem">
                        <div class="li-key">
                            <label class="required-field" for="diffEnd" title="">Delta End</label>
                        </div>
                        <div class="li-value">
                            <input type="text" class="date-field nowable-field" id="diffEnd" name="diffEnd" placeholder="YYYY-MM-DD" value="${requestScope.diffEnd}"/>
                        </div>
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
            <hr></hr><br></br>
            <button id="menu-toggle">Basic/Advanced</button>
        <t:tablesorter tableTitle="Cavity Set Point Deltas (${requestScope.diffStart} to ${requestScope.diffEnd})" tableId="diff-table-basic"></t:tablesorter>
        <t:tablesorter tableTitle="Cavity Set Point Deltas (${requestScope.diffStart} to ${requestScope.diffEnd})" tableId="diff-table-advanced"></t:tablesorter>

            <script>
                var jlab = jlab || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
            jlab.timeUnit = "${requestScope.timeUnit}";
            jlab.tableDate = "${requestScope.tableDate}";
        </script>
    </jsp:body>
</t:page>