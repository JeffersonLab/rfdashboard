<%-- 
    Document   : chart-widget
    Created on : Apr 5, 2017, 2:33:26 PM
    Author     : adamc
--%>

<%@tag description="Flot chart widget tag" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="placeholderId"%>

<%-- Content --%>
<div class="chart-legend-panel">
    <div class="chart-panel">
        <%-- This lets you have multiple chart-widgets on the same page without duplicating their ID --%>
        <div class="chart-wrap">
            <div class="chart-placeholder" ${placeholderId ne null ? "id=".concat(placeholderId) : ""}></div>
        </div>
    </div>
    <div class="legend-panel" ${placeholderId ne null ? "id=".concat(placeholderId).concat("-legend-panel") : ""}>
        <jsp:doBody/>       
    </div>
</div>