<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>D4M Explorer</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/css/pagination.css" />">
    </head>
    <body>
        <h1>D4M Explorer</h1>
        <h2><a href='/home'>Fields</a> &gt; <c:out value='${fieldName}' /></h2>

        <div class="pagination dark">
            <c:if test="${preferences.fieldValuePageNumber == 1}">
                <span style="width: 25px;" class="page disabled">first</span>
                <span style="width: 50px;" class="page disabled">previous</span>
            </c:if>
            <c:if test="${preferences.fieldValuePageNumber > 1}">
                <a href="/field_values" style="width: 25px;" class="page dark gradient">first</a>
                <a href="/field_values/previous" style="width: 50px;" class="page dark gradient">previous</a>
            </c:if>
            <c:forEach items="${pageList}" var="page">
                <c:if test="${preferences.fieldValuePageNumber == page}">
                    <span class="page dark active"><c:out value='${page}' /></span>
                </c:if>
                <c:if test="${preferences.fieldValuePageNumber != page}">
                    <a href="/field_values/<c:out value='${page}' />" class="page dark gradient"><c:out value='${page}' /></a>
                </c:if>
            </c:forEach>
            <c:if test="${endOfTable == false}">
                <a href="/field_values/next" style="width: 30px;" class="page dark gradient">next?</a>
                <a href="/field_values/last" style="width: 30px;" class="page dark gradient">last</a>
            </c:if>
            <c:if test="${endOfTable == true}">
                <span style="width: 30px;" class="page disabled">next?</span>
                <span style="width: 30px;" class="page disabled">last</span>
            </c:if>
            &nbsp;
            <form method="post" action="/field_values/changePageSize" style="display: inline;">
                <span class="page-count">Page 
                    <input type="text" name="pageNumber" size="3" value="<c:out value='${preferences.fieldValuePageNumber}' />"/>
                    of <fmt:formatNumber value='${numPages}' /></span>
                &nbsp;
                &nbsp;
                <span class="page-count">Page Size: </span>
                <select name="pageSize">
                    <c:forEach items="${pageSizes}" var="pageSize">
                        <option value="<c:out value='${pageSize}' />" <c:if test="${pageSize == preferences.pageSize}">selected</c:if>><c:out value='${pageSize}' /></option>
                    </c:forEach>
                </select>
                <input type="submit" value="Go"/>
            </form>
        </div>

        <table cellspacing="3" cellpadding="3" border="1">
            <tr>
                <th style="width: 200px;">Field Name</th>
                <th style="width: 200px;">Field Value</th>
                <th style="width: 100px;">Entry Count</th>
            </tr>
            <c:forEach items="${items}" var="item">
                <tr>
                    <td align="right"><c:out value="${fieldName}" /></td>
                    <td>
                        <c:if test="${item.timestamp > paginationTimestamp}">
                            <span class="newer">
                        </c:if>
                        <c:out value='${item.fieldValue}' />
                        <c:if test="${item.timestamp > paginationTimestamp}">
                            </span >
                        </c:if>
                    </td>
                    <td align="right"><fmt:formatNumber value="${item.entryCount}" /></td>
                </tr>
            </c:forEach>
        </table>

    </body>
</html>
