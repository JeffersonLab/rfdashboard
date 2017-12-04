<%-- 
    Document   : comments-table
    Created on : Dec 1, 2017, 4:29:29 PM
    Author     : adamc
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="comments" required="true" type="java.util.Collection"%> <%-- A collection of Comment objects to be displayed in the table --%>
<c:choose>
    <c:when test="${comments eq null || fn:length(comments) eq 0}">No comments were found.</c:when>
    <c:otherwise>
        <table class="comments-table">
            <thead >
                <tr><th>Timestamp</th><th>Topic</th><th>Author</th><th>Comment</th></tr>
            </thead>
            <tbody>
                <c:forEach var="comment" items="${comments}">
                    <tr><td>${comment.timestamp}</td><td>${comment.topic}</td><td>${comment.username}</td><td><c:out value="${comment.content}" escapeXml="true"></c:out></td></tr>
                </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>
