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
<t:page title="${title}">
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/utils.js"></script>
        <script type="text/javascript">
            var jlab = jlab || {};
            jlab.comment = jlab.comment || {};

            jlab.comment.postComment = function() {
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

                request.done(function(data, textStatus, jqXHR) {
                    alert("Comment submitted");
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    console.log("Comment submission failed: " + textStatus + " - " + errorThrown);
                    console.log("Response text: " + jqXHR.responseText);
                    alert("Comment submission failed.");
                }).always(function() {
                    jlab.requestEnd();
                });
            };

            $(document).on("click", "#send-feedback-button", function() {
                jlab.comment.postComment();
            });
        </script>        
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${pageContext.request.userPrincipal ne null}">
                    <div style="font-weight: bold; margin-bottom: 4px;">(<span class="required-field"></span> required)</div>
                    <fieldset id="feedback-fieldset">
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
        </section>
    </jsp:body>         
</t:page>
