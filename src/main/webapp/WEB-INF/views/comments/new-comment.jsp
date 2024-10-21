<%-- 
    Document   : newjsphelp
    Created on : May 10, 2017, 3:15:49 PM
    Author     : adamc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="New Comment"/>
<t:reports-page title="${title}">
    <jsp:attribute name="stylesheets">
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/utils.js"></script>
        <script type="text/javascript">
            var jlab = jlab || {};
            jlab.comment = jlab.comment || {};

            jlab.comment.postComment = function () {
                if (jlab.isRequest()) {
                    window.console && console.log("Ajax already in progress");
                    return;
                }

                jlab.requestStart();

                let comment = $("#comment-field").val();
                let topic = $("#topic-field").val();

                let request = $.ajax({
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
                    $("#comment-field").val("");

            <%-- This string is a weird mix of javascript and JSTL so be careful when editing --%>
                    let redirectUrl = "${pageContext.request.contextPath}${currentPath}?limit=${requestScope.limit}&offset=${requestScope.offset}&topic=" + topic.encodeXml();
                    window.location.replace(redirectUrl);
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    window.console && console.log("Comment submission failed: " + textStatus + " - " + errorThrown);
                    window.console && console.log("Response text: " + jqXHR.responseText);
                    alert("Comment submission failed.\nError: " + errorThrown);
                }).always(function () {
                    jlab.requestEnd();
                });
            };

            $(document).ready(function () {
                let $topicSelect = $("#topic-select").select2({
                    maximumSelectionSize: 1,
                    width: 290
                });
                $topicSelect.on('select2:opening', function(e) {
                    if ($(this).select2('data').length > 0) {
                        e.preventDefault();
                    }
                });

                $(document).on("click", "#send-feedback-button", function () {
                    jlab.comment.postComment();
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
                        <form method="post" action="ajax/comments">
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key"><label for="topic-select">Topics</label></div>
                                    <div class="li-value">
                                        <select id="topic-select" class="multi-select" name="topic" multiple>
                                            <c:forEach var="valid" items="${requestScope.validTopics}">
                                                <c:choose>
                                                    <c:when test="${requestScope.topic == valid}"><option value="${valid}" selected>${valid}</option></c:when>
                                                    <c:otherwise><option value="${valid}">${valid}</option></c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </select>
                                    </div>
                                <li>
                                    <div class="li-key"><label class="required-field" for="comment-field">Comment</label></div>
                                    <div class="li-value"><textarea id="comment-field" name="comment"></textarea></div>
                                </li>                                       
                            </ul>
                            <button type="button" id="send-feedback-button">Submit</button>
                        </form>
                    </fieldset>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <a href="<c:url value="/sso"><c:param name="returnUrl" value="${domainRelativeReturnUrl}"/></c:url>">Comment Form</a>
                    </div>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${requestScope.topic ne null}"><h4>Comments for <c:out value="${requestScope.topic}"/></h4></c:when>
                <c:otherwise><h4>Comment History</h4></c:otherwise>
            </c:choose>
            <t:comments-table comments="${requestScope.comments}"></t:comments-table>
            </section>
    </jsp:body>         
</t:reports-page>
