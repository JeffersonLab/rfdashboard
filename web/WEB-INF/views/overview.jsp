<%-- 
    Document   : overview
    Created on : Mar 16, 2017, 9:00:05 AM
    Author     : adamc
--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Overview" />
<t:page title="${title}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/flot-barchart.css"/>              
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-barchart.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/mod-anode.js"></script>
    </jsp:attribute>
    <jsp:body>
        <h2 id="page-header-title"><c:out value="${title}"/></h2>
        <h3>Welcome to the RF Gradient Team Dashboard.  This is just a test website, but there is more to come!</h3>
    </jsp:body>
</t:page>