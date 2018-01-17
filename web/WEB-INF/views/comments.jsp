<%-- 
    Document   : newjsphelp
    Created on : May 10, 2017, 3:15:49 PM
    Author     : adamc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Comment"/>
<t:reports-page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" href="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.css"/>
        <link rel="stylesheet" href="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.3.1.css"/>

        <style type="text/css">
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
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/select2/3.5.2/select2.min.js"></script>
        <script type="text/javascript" src="${initParam.cdnContextPath}/jquery-plugins/timepicker/jquery-ui-timepicker-1.3.1.js"></script>
        <script type="text/javascript">
            var jlab = jlab || {};
            jlab.comment = jlab.comment || {};

            jlab.comment.postComment = function () {
                if (jlab.isRequest()) {
                    window.console && console.log("Ajax already in progress");
                    return;
                }

                jlab.requestStart();

                var comment = $("#comment-field").val();
                var topic = $("#topic-field").val();

                var request = $.ajax({
                    url: jlab.contextPath + "/ajax/comments",
                    type: "POST",
                    data: {
                        topic: topic,
                        comment: comment
                    },
                    dataType: "json"
                });

                request.done(function (data, textStatus, jqXHR) {
                    alert("Comment submitted");
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    console.log("Comment submission failed: " + textStatus + " - " + errorThrown);
                    console.log("Response text: " + jqXHR.responseText);
                    alert("Comment submission failed.");
                }).always(function () {
                    jlab.requestEnd();
                });
            };

            $(document).on("click", "#send-feedback-button", function () {
                jlab.comment.postComment();
            });

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
            <c:choose>
                <c:when test="${pageContext.request.userPrincipal ne null}">
                    <div style="font-weight: bold; margin-bottom: 4px;"></div>
                    <fieldset id="feedback-fieldset">
                        <legend>Add a Comment</legend>
                        <form method="post" action="ajax/comments">
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key"><label class="required-field" for="topic">Topic</label></div>
                                    <div class="li-value"><input type="text" id="topic-field" name="topic"/></div>
                                </li>
                                <li>
                                    <div class="li-key"><label class="required-field" for="comment">Comment</label></div>
                                    <div class="li-value"><textarea id="comment-field" name="comment"></textarea></div>
                                </li>                                       
                            </ul>
                            <button type="button" id="send-feedback-button">Submit</button>
                        </form>
                    </fieldset>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <a href=<c:url value="/login?returnUrl=${pageContext.request.contextPath}/comments"/>>Comment Form</a>
                    </div>
                </c:otherwise>
            </c:choose>
            <h3>Comment History</h3>
            <form method="GET" action="${pageContext.request.contextPath}/comments">
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
                                            <c:when test="${includeUsers.containsKey(author.key)}"><option value="${author.key}" selected>${author.key}</option></c:when>
                                            <c:otherwise><option value="${author.key}">${author.key}</option></c:otherwise>                                
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
                                            <c:when test="${excludeUsers.containsKey(author.key)}"><option value="${author.key}" selected>${author.key}</option></c:when>
                                            <c:otherwise><option value="${author.key}">${author.key}</option></c:otherwise>                                
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
</t:reports-page>
