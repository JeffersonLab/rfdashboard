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
<%@attribute name="pageStart"%>
<%@attribute name="pageEnd" %>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<c:set var="currentPath" scope="request" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${title}</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/img/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-ui/1.10.3/theme/smoothness/jquery-ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.css"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.css"/>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/rfd.css">
        <jsp:invoke fragment="stylesheets"/>
    </head>
    <body>
        <c:if test="${initParam.NotificationBar ne null}">
            <div id="notification-bar"><c:out value="${initParam.NotificationBar}" /></div>
        </c:if>
        <div id="page">
            <header>
                <h1><span id="page-header-logo"></span><span id="page-header-text"><c:out value="${initParam.appName}" /></span></h1>
                <nav id="primary-nav">
                    <ul>
                        <li ${'/overview' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/overview">Overview</a></li>
                        <li ${'/mod-anode' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/mod-anode?start=${pageStart}&end=${pageEnd}">Mod Anode</a></li>
                        <li ${'/bypassed' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/bypassed?start=${pageStart}&end=${pageEnd}"">Bypassed</a></li>
                        <li ${'/energy-reach' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/energy-reach?start=${pageStart}&end=${pageEnd}"">Energy Reach</a></li>
                    </ul>
                </nav>
            </header>
            <div id="content">
                <div id="content-liner">
                    <jsp:doBody/>
                </div>
                    <div id="version-info">Version: ${initParam.releaseNumber}, Released: ${initParam.releaseDate}</div>
            </div>
        </div>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery/1.10.2.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-ui/1.10.3/jquery-ui.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.min.js"></script>
        <jsp:invoke fragment="scripts"/>
    </body>
</html>
