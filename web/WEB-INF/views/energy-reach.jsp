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
<t:page title="${title}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/flot-barchart.css"/>
<!--        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/energy-reach.css"/>-->
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/everpolate/everpolate.browserified.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/flot-barchart.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/chart-widget.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/energy-reach.js"></script>
    </jsp:attribute>
    <jsp:body>
        <h2 id="page-header-title"><c:out value="${title}"/></h2>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/energy-reach" method="get">
                <fieldset>
                    <div class="li-key">
                        <label class="required-field" for="start" title="Inclusive (Closed)">Start Date</label>
                        <div class="date-note">(Inclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field" id="start" name="start" placeholder="YYYY-MM-DD"/>
                    </div>
                    <div class="li-key">
                        <label class="required-field" for="end" title="Exclusive (Open)">End Date</label>
                        <div class="date-note">(Exclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD"/>
                    </div>
                    <input type="submit" value="Submit" />
                </fieldset>
            </form>
        </div>
            <t:chart-widget placeholderId="lem-scan"></t:chart-widget>
            <hr></hr><br></br>
            <t:chart-widget placeholderId="energy-reach"></t:chart-widget>
        <script>
            var jlab = jlab || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
        </script>
    </jsp:body>
</t:page>