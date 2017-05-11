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
    </jsp:attribute>
    <jsp:attribute name="scripts">
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
                                    <div class="li-key"><label class="required-field" for="name">Name</label></div>
                                    <div class="li-value"><input type="text" id="name" name="name"/></div>
                                </li>
                                <li>
                                    <div class="li-key"><label class="required-field" for="email">Email</label></div>
                                    <div class="li-value"><input type="text" id="email" name="email"/></div>
                                </li>
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
