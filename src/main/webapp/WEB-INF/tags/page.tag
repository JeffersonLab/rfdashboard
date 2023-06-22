<%@tag description="The basic RFDashboard page" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title"%>
<%@attribute name="pageStart"%>
<%@attribute name="pageEnd" %>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<%@attribute name="secondaryNavigation" fragment="true" %>
<s:tabbed-page title="${title}" category="${category}">
    <jsp:attribute name="stylesheets">
        <c:choose>
        <c:when test="${'NONE' eq resourceLocation}"></c:when>
        <c:when test="${'CDN' eq resourceLocation}">
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-ui/1.10.3/theme/smoothness/jquery-ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.css"/>
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/css/theme.default.min.css"/>
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/css/jquery.tablesorter.pager.min.css"/>
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-plugins/select2/4.0.5/dist/css/select2.min.css"/>
        </c:when>
        <c:otherwise> <!-- LOCAL -->
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/cdn/jquery-ui/1.10.3/theme/smoothness/jquery-ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/tablesorter-mottie/2.28.15/css/theme.default.min.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/tablesorter-mottie/2.28.15/css/jquery.tablesorter.pager.min.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/select2/4.0.5/dist/css/select2.min.css"/>
        </c:otherwise>
        </c:choose>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/img/favicon.ico"/>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/rfd.css">
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
        <c:when test="${'NONE' eq resourceLocation}"></c:when>
        <c:when test="${'CDN' eq resourceLocation}">
            <script type="text/javascript" src="${cdnContextPath}/jquery/1.10.2.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-ui/1.10.3/jquery-ui.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/jquery.tablesorter.combined.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/widgets/widget-output.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/parsers/parser-input-select.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/tablesorter-mottie/2.28.15/js/extras/jquery.tablesorter.pager.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/select2/4.0.5/dist/js/select2.min.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
            <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/select2/4.0.5/dist/js/select2.min.js"/>
        </c:when>
        <c:otherwise> <!-- LOCAL -->
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery/1.10.2.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-ui/1.10.3/jquery-ui.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/timepicker/jquery-ui-timepicker-1.5.0.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/tablesorter-mottie/2.28.15/js/jquery.tablesorter.combined.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/tablesorter-mottie/2.28.15/js/widgets/widget-output.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/tablesorter-mottie/2.28.15/js/parsers/parser-input-select.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/tablesorter-mottie/2.28.15/js/extras/jquery.tablesorter.pager.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/everpolate/everpolate.browserified.min.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/flot/axislabels/2.2.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/resources/cdn/jquery-plugins/select2/4.0.5/dist/js/select2.min.js"/>
        </c:otherwise>
        </c:choose>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/lib/jquery-migrate-1.4.1.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/tablesorter-widget.js"></script>

        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="primaryNavigation">
                    <ul>
                        <li ${'/energy-reach' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/energy-reach?start=${pageStart}&end=${pageEnd}">Energy Reach</a></li>
                        <li ${'/mod-anode' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/mod-anode?start=${pageStart}&end=${pageEnd}">Mod Anode</a></li>
                        <li ${'/bypassed' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/bypassed?start=${pageStart}&end=${pageEnd}">Bypassed</a></li>
                        <li ${fn:startsWith(currentPath, '/reports') ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/reports/cm-perf">Reports</a></li>
                        <li ${'/cryo' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/cryo?start=${pageStart}&end=${pageEnd}">Cryo</a></li>
                        <li ${'/links' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/links">Links</a></li>
                        <li ${'/help' eq currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/help">Help</a></li>
                    </ul>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
        <jsp:invoke fragment="secondaryNavigation"/>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</s:tabbed-page>