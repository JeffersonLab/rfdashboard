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
<t:comments-page title="${title}">
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
                    $("#comment-field").val("");

            <%-- This string is a weird mix of javascript and JSTL so be careful when editing --%>
                    var redirectUrl = "${pageContext.request.contextPath}${currentPath}?limit=${limit}&offset=${offset}&topic=" + topic.encodeXml();
                    window.location.replace(redirectUrl);
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    console.log("Comment submission failed: " + textStatus + " - " + errorThrown);
                    console.log("Response text: " + jqXHR.responseText);
                    alert("Comment submission failed.\nError: " + errorThrown);
                }).always(function () {
                    jlab.requestEnd();
                });
            };

            $(document).ready(function () {
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
                                    <div class="li-key"><label class="required-field" for="topic">Topic</label></div>
                                    <c:choose>
                                        <c:when test="${not empty topic}">
                                            <div class="li-value"><input type="text" id="topic-field" name="topic" value='<c:out value="${topic}" escapeXml="true"></c:out>' disabled/></div>
                                            </c:when>
                                            <c:otherwise>
                                            <div class="li-value"><input type="text" id="topic-field" name="topic" value='' /></div>
                                            </c:otherwise>
                                        </c:choose>
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
                        <a href=<c:url value="/login"><c:param name="returnUrl" value="${domainRelativeReturnUrl}"/></c:url>>Comment Form</a>
                    </div>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${topic ne null}"><h4>Comments for ${topic}</h4></c:when>
                <c:otherwise><h4>Comment History</h4></c:otherwise>
            </c:choose>
            <t:comments-table comments="${comments}"></t:comments-table>
            </section>
    </jsp:body>         
</t:comments-page>
