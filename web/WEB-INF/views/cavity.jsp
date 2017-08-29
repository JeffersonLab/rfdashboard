<%-- 
    Document   : energy-reach
    Created on : Apr 17, 2017, 2:06:06 PM
    Author     : adamc
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Cavity Details" />
<t:page title="${title}" pageStart="${requestScope.start}" pageEnd="${requestScope.end}"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/flot-barchart.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/tablesorter.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${initParam.cdnContextPath}/everpolate/everpolate.browserified.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/axislabels/2.2.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/flot/sideBySideImproved/jquery.flot.orderBars.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cavity-utils.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/lib/jquery.tablesorter.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/lib/jquery.tablesorter.pager.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/flot-charts.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/cavity.js"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="page-title-bar">
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            (<a href="#" id="page-details-opener">Details</a>)
        </div>
        <div id="page-details-dialog" title="Details">
            <h3> Cavity Details</h3>
            Cavity information provided here is pulled from the Archiver and the CED History deployment.  All data is representative
            of values from these sources at 12 AM on the dates provided.
            <h3>Page Controls</h3>
            <ul>
                <li>
                    Start Date - The start date to be displayed in the energy reach chart
                </li>
                <li>
                    End Date - The end date to be displayed in the energy reach chart
                </li>
                <li>
                    Delta Start - Start date to use in the "Cavity Set" tables.
                </li>
                <li>
                    Delta End - End date to use in the "Cavity Set" tables.
                </li>
            </ul>
            Note: Dates assume 12 AM at the start of the specified day.
            <br><br>
            Delta Start/End dates can also be controlled by clicking on bars in the energy reach chart.
            <h3> Charts and Tables </h3>
        </div>
        <div id="control-form">
            <form action="${pageContext.request.contextPath}/cavity" method="get">
                <div class="fieldset-container">
                    <hr>
                    <div class="fieldset-row">
                        <div class="fieldset-cell">
                            <fieldset>
                                <div class="fieldset-label">Date Range</div>
                                <div class="li-key">
                                    <label class="required-field" for="start" title="Inclusive (Closed)">Start Date</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" class="date-field" id="start" name="start" placeholder="YYYY-MM-DD" value="${requestScope.start}"/>
                                </div>
                                <div class="li-key">
                                    <label class="required-field" for="end" title="Exclusive (Open)">End Date</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD" value="${requestScope.end}"/>
                                </div>
                            </fieldset>
                        </div>
                        <div class="fieldset-cell">
                            <fieldset>
                                <div class="fieldset-label">Cryomodule Type</div>
                                <div class="li-key">
                                    <label for="c25" title="">C25</label>
                                </div>
                                <div class="li-value">
                                    <c:choose>
                                        <c:when test="${cmtypes.containsKey('C25')}">
                                            <input type="checkbox" id="c25" name="cmtypes" value="C25" checked/>
                                        </c:when>
                                        <c:when test="${not cmtypes.containsKey('C25')}">
                                            <input type="checkbox" id="c25" name="cmtypes" value="C25" checked/>
                                        </c:when>
                                    </c:choose>
                                </div>
                                <div class="li-key">
                                    <label for="c50" title="">C50</label>
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
                                <div class="li-key">
                                    <label for="c100" title="">C100</label>
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
                            </fieldset>
                        </div>
                        <div class="fieldset-cell">
                            <fieldset>
                                <div class="fieldset-label">Linac</div>
                                <div class="li-key">
                                    <label for="inj" title="">Injector</label>
                                </div>
                                <div class="li-value">
                                    <c:choose>
                                        <c:when test="${linacs.containsKey('inj')}">
                                            <input type="checkbox" id="inj" name="linacs" value="inj" checked/>
                                        </c:when>
                                        <c:when test="${not linacs.containsKey('inj')}">
                                            <input type="checkbox" id="inj" name="linacs" value="inj" />
                                        </c:when>
                                    </c:choose>
                                </div>
                                <div class="li-key">
                                    <label for="north" title="">North</label>
                                </div>
                                <div class="li-value">
                                    <c:choose>
                                        <c:when test="${linacs.containsKey('north')}">
                                            <input type="checkbox" id="nlorth" name="linacs" value="north" checked/>
                                        </c:when>
                                        <c:when test="${not linacs.containsKey('north')}">
                                            <input type="checkbox" id="nlorth" name="linacs" value="north" />
                                        </c:when>
                                    </c:choose>
                                </div>
                                <div class="li-key">
                                    <label for="south" title="">South</label>
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
                            </fieldset>
                        </div>
                    </div>
                    <div class="fieldset-row">
                        <div class="fieldset-cell">
                            <fieldset>
                                <div class="fieldset-label">Cavity Properties</div>
                                <div class="input-elem">
                                    <div class="li-key">
                                        <label for="cmtype" title="cmtype">Module Type</label>
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
                                        <label for="odvh" title="odvh">ODVH</label>
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
                                        <label for="opsGsetMax" title="opsGsetMax">OpsGsetMax</label>
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
                                        <label for="maxGset" title="maxGset">MaxGSET</label>
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
                                        <label for="q0" title="q0">Q0</label>
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
                                        <label for="qExternal" title="qExternal">Q External</label>
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
                                        <label for="tripOffset" title="tripOffset">Trip Offset</label>
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
                                        <label for="tripSlope" title="tripSlope">Trip Slope</label>
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
                                        <label for="modAnode" title="modAnode">Mod Anode</label>
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
                                        <label for="comments" title="comments">Comments</label>
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
                        </fieldset>
                    </div>
                </div>
                <input type="submit" value="Submit"/>

            </form>
            <hr>

        </div>
        <%--
                <t:tablesorter tableTitle="Cavity Properties (${requestScope.diffStart} to ${requestScope.diffEnd})" tableId="summary-table"></t:tablesorter>
        --%>
        <script>

            // Not terribly elegant, but need to get request parameters into javascript for further use.
            var jlab = jlab || {};
            jlab.start = "${requestScope.start}";
            jlab.end = "${requestScope.end}";
            jlab.diffStart = "${requestScope.diffStart}";
            jlab.diffEnd = "${requestScope.diffEnd}";
            jlab.timeUnit = "${requestScope.timeUnit}";

            jlab.properties = new Array();
            <c:forEach var="prop" items="${properties}">
                jlab.properties.push("${prop.key}");
            </c:forEach>

            jlab.linacs = new Array();
            <c:forEach var="linac" items="${linacs}">
                jlab.linacs.push("${linac.key}");
            </c:forEach>

            jlab.cmtypes = new Array();
            <c:forEach var="cmtype" items="${cmtypes}">
                jlab.cmtypes.push("${cmtype.key}");
            </c:forEach>

        </script>
    </jsp:body>
</t:page>