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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/chart-widget.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-barchart.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/bypassed.js"></script>
        <script>

        </script>
    </jsp:attribute>
    <jsp:body>
        <h2 id="page-header-title"><c:out value="${title}"/></h2>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/bypassed" method="get">
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
                        <div class="date-note">(Exclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD" value="${requestScope.end}"/>
                    </div>
                    <input type="submit" value="Submit" />
                </fieldset>
            </form>
        </div>
        <t:chart-widget placeholderId="bypassed-count-by-linac"></t:chart-widget>
        <hr></hr><br></br>
        <t:chart-widget placeholderId="bypassed-count-by-cmtype"></t:chart-widget>
        <script>
            var jlab = jlab || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
            jlab.timeUnit = "${requestScope.timeUnit}";
        </script>
    </jsp:body>
</t:page>