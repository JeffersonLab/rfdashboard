<%-- 
    Document   : tablesorter
    Created on : Apr 5, 2017, 2:33:26 PM
    Author     : adamc
--%>

<%@tag description="Sortable table tag for use with jQuery plugin tablesorter and it's pager addon" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="tableId"%>
<%@attribute name="tableTitle"%>

<%-- Content --%>
<div ${tableId ne null ? "id=".concat(tableId).concat("-wrap") : ""} class="tablesorter-wrap">
    <div ${tableId ne null ? "id=".concat(tableId).concat("-header") : ""}  class="tablesorter-header">
        <div ${tableId ne null ? "id=".concat(tableId).concat("-title") : ""}  class="tablesorter-title"><strong>${tableTitle}</strong></div>
        <div ${tableId ne null ? "id=".concat(tableId).concat("-pager") : ""}  class="pager">
            <form>
                <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/first.png" class="first"/>
                <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/prev.png" class="prev"/>
                <input type="text" disabled="true" class="pagedisplay"/>
                <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/next.png" class="next"/>
                <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/last.png" class="last"/>
                <select class="pagesize">
                    <option selected="selected"  value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option  value="100">100</option>
                </select>
            </form>
        </div>
    </div>
    <div ${tableId ne null ? "id=".concat(tableId).concat("-table") : ""}  class="tablesorter-table">
        <%-- The HTML table should have id=<tableId>.  I.e., the value of the attribute passed to this widget --%>
        <%-- This is where the actual table should be placed using something like jquery.prepend --%>
    </div>
</div>