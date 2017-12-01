<%@tag description="The Comments Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<t:page title="Comments - ${title}"> 
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
                    <h2 id="left-column-header">Comments</h2>
                    <nav id="secondary-nav">
                        <ul> 
                            <li${'/comments/new-comment' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/comments/new-comment">New Comment</a></li>                     
                            <li${'/comments/history' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/comments/history">Comment History</a></li>
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