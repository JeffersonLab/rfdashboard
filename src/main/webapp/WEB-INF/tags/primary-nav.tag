<%@tag description="Primary Navigation Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<ul>
    <li ${'/energy-reach' eq requestScope.currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/energy-reach">Energy Reach</a></li>
    <li ${'/mod-anode' eq requestScope.currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/mod-anode">Mod Anode</a></li>
    <li ${'/bypassed' eq requestScope.currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/bypassed">Bypassed</a></li>
    <li ${fn:startsWith(requestScope.currentPath, '/reports') ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/reports/cm-perf">Reports</a></li>
    <li ${'/cryo' eq requestScope.currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/cryo">Cryo</a></li>
    <li ${'/links' eq requestScope.currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/links">Links</a></li>
    <li ${'/help' eq requestScope.currentPath ? 'class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/help">Help</a></li>
</ul>