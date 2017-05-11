<%-- 
    Document   : newjsphelp
    Created on : May 10, 2017, 3:15:49 PM
    Author     : adamc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Help"/>
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
            jlab.feedback = jlab.feedback || {};

            jlab.feedback.sendEmail = function() {
                if (jlab.isRequest()) {
                    window.console && console.log("Ajax already in progress");
                    return;
                }

                jlab.requestStart();

                var subject = $("#subject").val(),
                        body = $("#body").val();

                var request = jQuery.ajax({
                    url: jlab.contextPath + "/ajax/feedback",
                    type: "POST",
                    data: {
                        subject: subject,
                        body: body
                    },
                    dataType: "html"
                });

                request.done(function(data) {
                    if ($(".status", data).html() !== "Success") {
                        alert('Unable to send email: ' + $(".reason", data).html());
                    } else {
                        $("#subject").val('');
                        $("#body").val('');
                        alert('Email sent');
                    }

                });

                request.error(function(xhr, textStatus) {
                    window.console && console.log('Unable to send email: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
                    alert('Unable to send email');
                });

                request.always(function() {
                    jlab.requestEnd();
                });
            };

            $(document).on("click", "#send-feedback-button", function() {
                jlab.feedback.sendEmail();
            });
        </script>        
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <h3>RF Dashboard</h3>
            <ul class="key-value-list">
                <li>
                    <div class="li-key">Release Version:</div>
                    <div class="li-value"><c:out value="${initParam.releaseNumber}"/></div>
                </li>
                <li>
                    <div class="li-key">Release Date:</div>
                    <div class="li-value"><c:out value="${initParam.releaseDate}"/></div>
                </li>                    
                <li>
                    <div class="li-key">Content Contact:</div>
                    <div class="li-value">Ken Baggett (baggett)</div>                        
                </li>
                <li>
                    <div class="li-key">Technical Contact:</div>
                    <div class="li-value">Adam Carpenter (adamc)</div>                        
                </li>                    
            </ul>
            <h3>Feedback Form</h3>
            <c:choose>
                <c:when test="${pageContext.request.userPrincipal ne null}">
                    <div style="font-weight: bold; margin-bottom: 4px;">(<span class="required-field"></span> required)</div>
                    <fieldset id="feedback-fieldset">
                        <form method="post" action="ajax/feedback">
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key"><label class="required-field" for="subject">Subject</label></div>
                                    <div class="li-value"><input type="text" id="subject" name="subject"/></div>
                                </li>
                                <li>
                                    <div class="li-key"><label class="required-field" for="body">Message</label></div>
                                    <div class="li-value"><textarea id="body" name="body"></textarea></div>
                                </li>                                       
                            </ul>
                            <button type="button" id="send-feedback-button">Submit</button>
                        </form>
                    </fieldset>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <a href=<c:url value="/login?returnUrl=${pageContext.request.contextPath}/help"/>>Feedback Form</a>
                    </div>
                </c:otherwise>
            </c:choose>         
        </section>
    </jsp:body>         
</t:page>
