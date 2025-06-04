<%-- 
    Document   : energy-reach
    Created on : Apr 17, 2017, 2:06:06 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="cf" uri="http://jlab.org/rfd/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Cavity Data"/>
<t:reports-page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/cavity.css"/>
        <style>
            /*Make the select2 boxes take up the full length.  Should consider :has(.multi-select) if that gains wider
            adoption*/
            .li-value {
                width: 100%;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                jlab.util.initDateTimePickers();
                $('.multi-select').select2({
                    width: '100%',
                    closeOnSelect: false,
                    maximumSelectionLength: 50
                });
            });
        </script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div class="page-title-bar">
                <h2 id="page-header-title"><c:out value="${title}"/></h2>
                (<a href="#" id="page-details-opener" title="Page Details">Details</a>)
            </div>
            <div id="page-details-dialog" class="dialog" title="Details">
                <h3> Cavity Details</h3>
                Cavity information provided here is pulled from the Archiver and the CED History deployment. All data is
                representative of values from these sources at 12 AM on the dates provided.
                <h3>Page Controls</h3>
                <ul>
                    <li>
                        Start Date - The start date for displaying cavity details
                    </li>
                    <li>
                        End Date - The end date for displaying cavity details
                    </li>
                    <li>
                        Cryomodule Type - Select the cryomodule types to display
                    </li>
                    <li>
                        Linac - Select the Linacs to display
                    </li>
                    <li>
                        Cavity Properties - Select the cavity properties to display
                    </li>
                </ul>
                Note: Dates assume 12 AM at the start of the specified day.
                <br><br>
                <h3> Tables </h3>
                The "Cavity Properties" table displays the selected properties for the start date, the end date, and
                their delta. Cavities
                can be filtered out based on their Linac location or Cryomodule type. This table supports a number of
                features. It is sortable
                by clicking/Shift-clicking the header fields of the columns. It supports filtering results by entering
                search strings and
                a limited command set (documentation <a
                    href="https://mottie.github.io/tablesorter/docs/example-widget-filter.html">here</a>).
                The data can be output in a number of different formats using the 'Output' button and 'Output Options'
                dropdown menu.
            </div>
            <div id="control-form">
                <form action="${pageContext.request.contextPath}/reports/cavity" method="get">
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key"><label for=start-input">Start Time</label></div>
                            <div class="li-value"><input id=start-input" type="text" name="start"
                                                         class="datetime-picker" value="${requestScope.start}">
                            </div>
                        </li>
                        <li>
                            <div class="li-key"><label for="end-input">End Time</label></div>
                            <div class="li-value"><input id="end-input" type="text" name="end" class="datetime-picker"
                                                         value="${requestScope.end}">
                            </div>
                        </li>


                        <li>
                            <div class="li-key">
                                <label for="linac-selector">Linacs</label>
                            </div>
                            <div class="li-value">
                                <select id="linac-selector" class="multi-select" name="linacs" multiple="multiple">
                                    <c:forEach var="linac" items="${requestScope.linacs}">
                                        <c:choose>
                                            <c:when test="${linac.value}">
                                                <option value="${linac.key}"
                                                        selected="selected">${cf:capitalizeFirst(linac.key)}</option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${linac.key}">${cf:capitalizeFirst(linac.key)}</option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>


                        <li>
                            <div class="li-key">
                                <label for="cmtype-selector">Cryomodule Types</label>
                            </div>
                            <div class="li-value">
                                <select id="cmtype-selector" class="multi-select" name="cmtypes" multiple="multiple">
                                    <c:forEach var="type" items="${requestScope.cmtypes}">
                                        <c:choose>
                                            <c:when test="${type.value}">
                                                <option value="${type.key}"
                                                        selected="selected">${cf:capitalizeFirst(type.key)}</option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${type.key}">${cf:capitalizeFirst(type.key)}</option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">
                                <label for="cav-property-selector">Cavity Properties</label>
                            </div>
                            <div class="li-value">
                                <select id="cav-property-selector" class="multi-select" name="properties"
                                        multiple="multiple">
                                    <c:forEach var="prop" items="${requestScope.properties}">
                                        <c:choose>
                                            <c:when test="${prop.value}">
                                                <option value="${prop.key}"
                                                        selected="selected">${cf:capitalizeFirst(prop.key)}</option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${prop.key}">${cf:capitalizeFirst(prop.key)}</option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                    </ul>
                    <input type="submit" value="Submit"/>
                </form>
                <hr>
            </div>
                <%--suppress CheckTagEmptyBody --%>
            <t:tablesorter tableTitle="Cavity Properties<br/>(${requestScope.start} vs ${requestScope.end})"
                           widgetId="details-table"
                           filename="${requestScope.start}_${requestScope.end}_cavProps.csv"></t:tablesorter>
        </section>
        <script>
            // Not terribly elegant, but need to get request parameters into javascript for further use.
            var jlab = jlab || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
            jlab.properties = [];
            <c:forEach var="prop" items="${requestScope.properties}">
            <c:if test="${prop.value}">jlab.properties.push("${prop.key}");
            </c:if>
            </c:forEach>
            jlab.linacs = [];
            <c:forEach var="linac" items="${requestScope.linacs}">
            <c:if test="${linac.value}">jlab.linacs.push("${linac.key}");
            </c:if>
            </c:forEach>
            jlab.cmtypes = [];
            <c:forEach var="cmtype" items="${requestScope.cmtypes}">
            <c:if test="${cmtype.value}">jlab.cmtypes.push("${cmtype.key}");
            </c:if>
            </c:forEach>
            jlab.cavityData = ${requestScope.cavityData};
        </script>
    </jsp:body>
</t:reports-page>
