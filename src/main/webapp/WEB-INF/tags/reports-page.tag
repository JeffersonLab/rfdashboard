<%@tag description="The Comments Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@attribute name="title"%>
<%@attribute name="pageStart" %>
<%@attribute name="pageEnd" %>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<t:page title="Reports - ${title}"> 
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>    
    <jsp:body>
        <div id="two-columns">
            <div id="left-column">
                <section>
                    <h2 id="left-column-header">Reports</h2>
                    <nav id="secondary-nav">
                        <ul> 
                            <li${'/reports/cm-perf' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/cm-perf">Cryomodule Performance</a></li>
                            <li${'/reports/cavity-perf' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/cavity-perf">Cavity Performance</a></li>
                            <li${'/reports/egain-hist' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/egain-hist">Energy Gain History</a></li>
                            <li${'/reports/cavity' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/cavity?start=${pageStart}&end=${pageEnd}">Cavity Data</a></li>
                            <li${'/reports/ced-prop-hist' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/ced-prop-hist">RF Cavity Tool History</a></li>
                            <li${'/reports/comments/history' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/comments/history">Comment History</a></li>
                            <li${'/reports/comments/new-comment' eq requestScope.currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/comments/new-comment">New Comment</a></li>
                        </ul>               
                    </nav>
                </section>
            </div>
            <div id="right-column">
                <jsp:doBody/>
            </div>
        </div> 
    </jsp:body>         
</t:page>