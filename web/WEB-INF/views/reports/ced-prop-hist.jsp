<%-- 
    Document   : ced-prop-hist
    Created on : May 1, 2018, 1:58:14 PM
    Author     : adamc
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<t:reports-page title="CED Property Updates">
    <jsp:attribute name="stylesheets">
        <!--<link rel="stylesheet" href="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.css"/>-->
        <link rel="stylesheet" href="${initParam.cdnContextPath}/jquery-plugins/select2/4.0.5/dist/css/select2.min.css"/>
        <style>
            .select2-selection {
                max-height: 125px;
                overflow-y: auto;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <!--<script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.min.js"></script>-->
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/select2/4.0.5/dist/js/select2.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                jlab.util.initDateTimePickers();
                $('.multi-select').select2({
                    width: 290,
                    closeOnSelect: false
                });
            });
            $("#all-cavs-checkbox").click(function () {
                if ($("#all-cavs-checkbox").is(':checked')) {
                    $("#cav-selector > option").prop("selected", "selected");
                    $("#cav-selector").trigger("change");
                } else {
                    $("#cav-selector > option").removeAttr("selected");
                    $("#cav-selector").trigger("change");
                }
            });

        </script>
    </jsp:attribute>
    <jsp:body>
        <form id="filter-form" method="GET" action="${pageContext.request.contextPath}/reports/ced-prop-hist">
            <fieldset>
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">Start Time:</div>
                        <div class="li-value"><input type="text" name="start" class="datetime-picker" value="${start}"></div>
                    </li>
                    <li>
                        <div class="li-key">End Time:</div>
                        <div class="li-value"><input type="text" name="end" class="datetime-picker" value="${end}"></div>
                    </li>
                    <li>
                        <div class="li-key">Cavities:</div>
                        <div class="li-value">
                            <select id="cav-selector" class="multi-select" name="e" multiple="multiple">
                                <c:forEach var="cav" items="${cavNames}">
                                    <c:choose>
                                        <c:when test="${elems.contains(cav)}">
                                            <option value="${cav}" selected="selected">${cav}</option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="${cav}">${cav}</option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </select>
                            <input type="checkbox" id="all-cavs-checkbox">Select All</input>
                        </div>
                    </li>
                    <li>                    
                        <div class="li-key">Properties:</div>
                        <div class="li-value">
                            <select class="multi-select" name="props" multiple="multiple">
                                <c:forEach var="prop" items="${cavProps}">
                                    <c:choose>
                                        <c:when test="${props.contains(prop)}">
                                            <option value="${prop}" selected="selected">${prop}</option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="${prop}">${prop}</option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </select>
                        </div>
                    </li>
                </ul>
                <input type="submit" value="Submit">
            </fieldset>
        </form>
        <hr>
        <c:choose>
            <c:when test="${fn:length(cedUpdates) == 0}">
                No CED Property Updates Found
            </c:when>
            <c:otherwise>
                <table class="data-table">
                    <thead>
                        <tr><th>Timestamp</th><th>Element</th><th>Property</th><th>Value</th><th>User</th><th>Comment</th></tr>
                    </thead>
                    <c:forEach  var="update" items="${cedUpdates}">
                        <tr><td>${update.dateString}</td><td>${update.elementName}</td><td>${update.propertyName}</td><td>${update.value}</td><td>${update.username}</td><td>${update.comment}</td></tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose> 
    </jsp:body>
</t:reports-page>