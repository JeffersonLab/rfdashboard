<%-- 
    Document   : newjsphelp
    Created on : May 10, 2017, 3:15:49 PM
    Author     : adamc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="History"/>
<t:comments-page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                jlab.util.initDateTimePickers();
                $('.multi-select').select2({
                    width: 290
                });

                $(document).on("click", "#next-button, #previous-button", function () {
                    $("#offset-input").val($(this).attr("data-offset"));
                    $("#filter-form").submit();
                });
            });
        </script>        
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <form id="filter-form" method="GET" action="${pageContext.request.contextPath}/comments/history">
                <fieldset>
                    <legend>Filters</legend>
                    <input id="limit-input" type="number" name="limit" value="${limit}"  hidden>
                    <input id="offset-input" type="number" name="offset" value="0"  hidden>
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
                            <div class="li-key">Include Authors:</div>
                            <div class="li-value">
                                <select class="multi-select" name="includeUser" multiple>
                                    <c:forEach var="author" items="${authors}">
                                        <c:choose>
                                            <c:when test="${includeUsers.containsKey(author)}"><option value="${author}" selected>${author}</option></c:when>
                                            <c:otherwise><option value="${author}">${author}</option></c:otherwise>                                
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">Exclude Authors:</div>
                            <div class="li-value">
                                <select class="multi-select" name="excludeUser" multiple>
                                    <c:forEach var="author" items="${authors}">
                                        <c:choose>
                                            <c:when test="${excludeUsers.containsKey(author)}"><option value="${author}" selected>${author}</option></c:when>
                                            <c:otherwise><option value="${author}">${author}</option></c:otherwise>                                
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">Topics:</div>
                            <div class="li-value">
                                <select class="multi-select" name="topic" multiple>
                                    <c:forEach var="valid" items="${validTopics}">
                                        <c:choose>
                                            <c:when test="${topics.containsKey(valid)}"><option value="${valid}" selected>${valid}</option></c:when>
                                            <c:otherwise><option value="${valid}">${valid}</option></c:otherwise>                                
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                    </ul>
                    <input type="submit">
                </fieldset>
            </form>
            ${selectionMessage}
            <t:comments-table comments="${comments}"></t:comments-table>
            <c:if test="${fn:length(comments) > 0}"> 
                <div class="event-controls">
                    <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                    <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>                 
                </div>
            </c:if>
        </section>
    </jsp:body>         
</t:comments-page>
