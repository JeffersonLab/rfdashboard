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
<c:url var="domainRelativeReturnUrl" scope="request" context="/" value="${requestScope['javax.servlet.forward.request_uri']}${requestScope['javax.servlet.forward.query_string'] ne null ? '?'.concat(requestScope['javax.servlet.forward.query_string']) : ''}"/>
<c:set var="currentPath" scope="request" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${initParam.appShortName} - ${title}</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/img/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-ui/1.10.3/theme/smoothness/jquery-ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.css"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/css/theme.default.min.css"/>
        <link rel="stylesheet" type="text/css" href="${initParam.cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/css/jquery.tablesorter.pager.min.css"/>
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
                <div id="auth">
                    <c:choose>
                        <c:when test="${fn:startsWith(currentPath, '/login')}">
                            <%-- Don't show login/logout when on login page itself! --%>
                        </c:when>
                        <c:when test="${pageContext.request.userPrincipal ne null}">
                            <div id="username-container">
                                <c:out value="${pageContext.request.userPrincipal}"/>
                            </div>
                            <form id="logout-form" action="${pageContext.request.contextPath}/logout" method="post">
                                <button type="submit" value="Logout">Logout</button>
                                <input type="hidden" name="returnUrl" value="${fn:escapeXml(domainRelativeReturnUrl)}"/>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <c:url value="/login" var="loginUrl">
                                <c:param name="returnUrl" value="${domainRelativeReturnUrl}"/>
                            </c:url>
                            <a id="login-link" href="${loginUrl}">Login</a>
                        </c:otherwise>
                    </c:choose>
                </div>

                <nav id="primary-nav">
                    <ul>
                        <li ${'/energy-reach' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/energy-reach?start=${pageStart}&end=${pageEnd}">Energy Reach</a></li>
                        <li ${'/mod-anode' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/mod-anode?start=${pageStart}&end=${pageEnd}">Mod Anode</a></li>
                        <li ${'/bypassed' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/bypassed?start=${pageStart}&end=${pageEnd}">Bypassed</a></li>
                        <li ${'/cavity' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/cavity?start=${pageStart}&end=${pageEnd}">Cavity</a></li>
                        <li ${'/cryo' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/cryo?start=${pageStart}&end=${pageEnd}">Cryo</a></li>
                        <li ${'/links' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/links">Links</a></li>
                        <li ${'/help' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/help">Help</a></li>
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/lib/jquery-migrate-1.4.1.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-ui/1.10.3/jquery-ui.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/jquery.tablesorter.combined.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/widgets/widget-output.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/parsers/parser-input-select.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/extras/jquery.tablesorter.pager.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/tablesorter-widget.js"></script>
        <jsp:invoke fragment="scripts"/>
    </body>
</html>
