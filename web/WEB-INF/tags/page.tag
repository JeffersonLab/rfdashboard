<%-- 
    Document   : page
    Created on : Apr 5, 2017, 9:00:09 AM
    Author     : adamc
--%>

<%@tag description="The basic RFDashboard page" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>

<%-- any content can be specified here e.g.: --%>
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${title}</title>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/smoothness.css">
        <jsp:invoke fragment="stylesheets"/>
    </head>
    <body>
        <div id="page">
            <c:if test="${initParam.NotificationBar ne null}">
                <div id="notification-bar"><c:out value="${initParam.NotificationBar}" /></div>
            </c:if>
            <header>
                <h1><span id="page-header-logo"></span><span id="page-header-text"><c:out value="${initParam.appName}" /></span></h1>
                <nav id="primary-nav">
                    <ul>
                        <li${'/overview' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/overview">Overview</a></li>
                        <li${'/Mod-Anode' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/mov-anode">Mod Anode</a></li>
                    </ul>
                </nav>
            </header>
            <div id="content">
                <div id="content-liner">
                    <jsp:doBody/>
                </div>
            </div>
        </div>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery/1.10.2.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-ui/1.10.3/jquery-ui.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3"></script>
        <jsp:invoke fragment="scripts"/>
    </body>
</html>
