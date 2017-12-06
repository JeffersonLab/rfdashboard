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
        <link rel="stylesheet" href="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.3.1.css"/>

        
<!--        <style type="text/css">
            #subject {
                width: 375px;
            }
            #body {
                width: 375px;
                height: 200px;
            }
            #send-feedback-button {
                float: right;
            }
            #feedback-fieldset {
                width: 500px;
            }
        </style>-->
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.3.1.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                jlab.util.initDateTimePickers();
                $('.multi-select').select2({
                    width: 290
                });
            });
        </script>        
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <!--<h3>Comment History</h3>-->
            <form method="GET" action="${pageContext.request.contextPath}/comments/history">
                <fieldset>
                    <legend>Filters</legend>
                    <input type="number" name="limit" value="${limit}"  hidden>
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
            <table class="comments-table">
                <thead >
                    <tr><th>Timestamp</th><th>Device</th><th>Author</th><th>Comment</th></tr>
                </thead>
                <tbody>
                    <c:forEach var="comment" items="${comments}">
                        <tr><td>${comment.timestamp}</td><td>${comment.topic}</td><td>${comment.username}</td><td><c:out value="${comment.content}" escapeXml="true"></c:out></td></tr>
                    </c:forEach>
                </tbody>
            </table>
        </section>
    </jsp:body>         
</t:comments-page>
