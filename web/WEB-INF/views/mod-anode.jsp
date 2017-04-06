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
<t:page title="${title}"> 
    <jsp:attribute name="stylesheets">
        <<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/flot-barchart.css"/>              
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/lib/jquery-3.2.0.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/lib/flot/jquery.flot.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/lib/flot/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/lib/flot/jquery.flot.orderBars.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/lib/flot/jquery.flot.axislabels.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/flot-barchart.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/utils.js"></script>
    </jsp:attribute>
    <jsp:body>
        <h2 id="page-header-title"><c:out value="${title}"/></h2>
        <h3></h3>

        <t:chart-widget placeholderId="mav-count-by-linac"></t:chart-widget>

        <button type="button" onclick="jlab.barChart.updateChart('mav-count-by-linac')">Load Data</button>
    </jsp:body>
</t:page>
<!--
</body>
</html>
-->
