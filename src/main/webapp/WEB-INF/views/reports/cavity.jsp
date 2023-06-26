<%-- 
    Document   : energy-reach
    Created on : Apr 17, 2017, 2:06:06 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Cavity Data" />
<t:reports-page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/cavity.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cavity.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div class="page-title-bar">
                <h2 id="page-header-title"><c:out value="${title}"/></h2>
                (<a href="#" id="page-details-opener" title="Page Details">Details</a>)
            </div>
            <div id="page-details-dialog" class="dialog" title="Details">
                <h3> Cavity Details</h3>
                Cavity information provided here is pulled from the Archiver and the CED History deployment.  All data is representative
                of values from these sources at 12 AM on the dates provided.
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
                The "Cavity Properties" table displays the selected properties for the start date, the end date, and their delta.  Cavities
                can be filtered out based on their Linac location or Cryomoule type.  This table supports a number of features.  It is sortable
                by clicking/Shift-clicking the header fields of the columns.  It supports filtering results by entering search strings and
                a limited command set (documentation <a href="https://mottie.github.io/tablesorter/docs/example-widget-filter.html">here</a>).
                The data can be output in a number of different formats using the 'Output' button and 'Output Options' dropdown menu.
            </div>
            <div id="control-form">
                <form action="${pageContext.request.contextPath}/reports/cavity" method="get">
                    <div class="fieldset-container">
                        <div class="fieldset-row">
                            <div class="fieldset-cell">
                                <div class="fieldset-label">Date Range</div>
                                <div class="input-elem">
                                    <t:calendar-start-end id="main-calendar" start="${requestScope.start}" end="${requestScope.end}" startLabel="Start Date" endLabel="End Date"></t:calendar-start-end>
                                    </div>
                                </div>
                                <div class="fieldset-cell">
                                    <div class="fieldset-label">Cryomodule Type</div>
                                    <div class="input-elem">
                                        <div class="li-key">
                                            <label for="QTR">QTR</label>
                                        </div>
                                        <div class="li-value">
                                        <c:choose>
                                            <c:when test="${cmtypes.containsKey('QTR')}">
                                                <input type="checkbox" id="QTR" name="cmtypes" value="QTR" checked/>
                                            </c:when>
                                            <c:when test="${not cmtypes.containsKey('QTR')}">
                                                <input type="checkbox" id="QTR" name="cmtypes" value="QTR" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="c25" >C25</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${cmtypes.containsKey('C25')}">
                                                <input type="checkbox" id="c25" name="cmtypes" value="C25" checked/>
                                            </c:when>
                                            <c:when test="${not cmtypes.containsKey('C25')}">
                                                <input type="checkbox" id="c25" name="cmtypes" value="C25" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="c50" >C50</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${cmtypes.containsKey('C50')}">
                                                <input type="checkbox" id="c50" name="cmtypes" value="C50" checked/>
                                            </c:when>
                                            <c:when test="${not cmtypes.containsKey('C50')}">
                                                <input type="checkbox" id="c50" name="cmtypes" value="C50" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="c75">C75</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${cmtypes.containsKey('C75')}">
                                                <input type="checkbox" id="c75" name="cmtypes" value="C75" checked/>
                                            </c:when>
                                            <c:when test="${not cmtypes.containsKey('C75')}">
                                                <input type="checkbox" id="c75" name="cmtypes" value="C75" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="c100">C100</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${cmtypes.containsKey('C100')}">
                                                <input type="checkbox" id="c100" name="cmtypes" value="C100" checked/>
                                            </c:when>
                                            <c:when test="${not cmtypes.containsKey('C100')}">
                                                <input type="checkbox" id="c100" name="cmtypes" value="C100" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                            <div class="fieldset-cell">
                                <div class="fieldset-label">Linac</div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="injector">Injector</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${linacs.containsKey('injector')}">
                                                <input type="checkbox" id="injector" name="linacs" value="injector" checked/>
                                            </c:when>
                                            <c:when test="${not linacs.containsKey('injector')}">
                                                <input type="checkbox" id="injector" name="linacs" value="injector" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="north">North</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${linacs.containsKey('north')}">
                                                <input type="checkbox" id="north" name="linacs" value="north" checked/>
                                            </c:when>
                                            <c:when test="${not linacs.containsKey('north')}">
                                                <input type="checkbox" id="north" name="linacs" value="north" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="south">South</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${linacs.containsKey('south')}">
                                                <input type="checkbox" id="south" name="linacs" value="south" checked/>
                                            </c:when>
                                            <c:when test="${not linacs.containsKey('south')}">
                                                <input type="checkbox" id="south" name="linacs" value="south" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="fieldset-row">
                            <div class="fieldset-cell">
                                <div class="fieldset-label">Cavity Properties</div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="cmtype">Module Type</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('cmtype')}">
                                                <input type="checkbox" id="cmtype" name="properties" value="cmtype" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('cmtype')}">
                                                <input type="checkbox" id="cmtype" name="properties" value="cmtype"/>
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="linac">Linac</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('linac')}">
                                                <input type="checkbox" id="linac" name="properties" value="linac" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('linac')}">
                                                <input type="checkbox" id="linac" name="properties" value="linac"/>
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="length">Length</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('length')}">
                                                <input type="checkbox" id="length" name="properties" value="length" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('length')}">
                                                <input type="checkbox" id="length" name="properties" value="length"/>
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="odvh">ODVH</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('odvh')}">
                                                <input type="checkbox" id="odvh" name="properties" value="odvh" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('odvh')}">
                                                <input type="checkbox" id="odvh" name="properties" value="odvh" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="bypassed">Bypassed</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('bypassed')}">
                                                <input type="checkbox" id="bypassed" name="properties" value="bypassed" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('bypassed')}">
                                                <input type="checkbox" id="bypassed" name="properties" value="bypassed" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="gset">GSET</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('gset')}">
                                                <input type="checkbox" id="gset" name="properties" value="gset" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('gset')}">
                                                <input type="checkbox" id="gset" name="properties" value="gset" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="tunerBad">TunerBad</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('tunerBad')}">
                                                <input type="checkbox" id="tunerBad" name="properties" value="tunerBad" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('tunerBad')}">
                                                <input type="checkbox" id="tunerBad" name="properties" value="tunerBad" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="opsGsetMax">OpsGsetMax</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('opsGsetMax')}">
                                                <input type="checkbox" id="opsGsetMax" name="properties" value="opsGsetMax" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('opsGsetMax')}">
                                                <input type="checkbox" id="opsGsetMax" name="properties" value="opsGsetMax" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="maxGset">MaxGSET</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('maxGset')}">
                                                <input type="checkbox" id="maxGset" name="properties" value="maxGset" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('maxGset')}">
                                                <input type="checkbox" id="maxGset" name="properties" value="maxGset" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="q0">Q0</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('q0')}">
                                                <input type="checkbox" id="q0" name="properties" value="q0" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('q0')}">
                                                <input type="checkbox" id="q0" name="properties" value="q0" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="qExternal">Q External</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('qExternal')}">
                                                <input type="checkbox" id="qExternal" name="properties" value="qExternal" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('qExternal')}">
                                                <input type="checkbox" id="qExternal" name="properties" value="qExternal" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="tripOffset">Trip Offset</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('tripOffset')}">
                                                <input type="checkbox" id="tripOffset" name="properties" value="tripOffset" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('tripOffset')}">
                                                <input type="checkbox" id="tripOffset" name="properties" value="tripOffset" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="tripSlope">Trip Slope</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('tripSlope')}">
                                                <input type="checkbox" id="tripSlope" name="properties" value="tripSlope" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('tripSlope')}">
                                                <input type="checkbox" id="tripSlope" name="properties" value="tripSlope" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="modAnode">Mod Anode</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('modAnode')}">
                                                <input type="checkbox" id="modAnode" name="properties" value="modAnode" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('modAnode')}">
                                                <input type="checkbox" id="modAnode" name="properties" value="modAnode" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="comments">Comments</label>
                                    </div>
                                    <div class="li-value">
                                        <c:choose>
                                            <c:when test="${properties.containsKey('comments')}">
                                                <input type="checkbox" id="comments" name="properties" value="comments" checked/>
                                            </c:when>
                                            <c:when test="${not properties.containsKey('comments')}">
                                                <input type="checkbox" id="comments" name="properties" value="comments" />
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <input type="submit" value="Submit"/>
                </form>
                <hr>
            </div>
            <t:tablesorter tableTitle="Cavity Properties<br/>(${requestScope.start} vs ${requestScope.end})" widgetId="details-table" filename="${requestScope.start}_${requestScope.end}_cavProps.csv"></t:tablesorter>
            </section>
            <script>
                // Not terribly elegant, but need to get request parameters into javascript for further use.
                var jlab = jlab || {};
                jlab.start = "${requestScope.start}";
                jlab.end = "${requestScope.end}";
                jlab.properties = new Array();
            <c:forEach var="prop" items="${properties}">jlab.properties.push("${prop.key}");</c:forEach>
                jlab.linacs = new Array();
            <c:forEach var="linac" items="${linacs}">jlab.linacs.push("${linac.key}");</c:forEach>
                jlab.cmtypes = new Array();
            <c:forEach var="cmtype" items="${cmtypes}">jlab.cmtypes.push("${cmtype.key}");</c:forEach>
                jlab.cavityData = ${requestScope.cavityData};
        </script>
    </jsp:body>
</t:reports-page>
