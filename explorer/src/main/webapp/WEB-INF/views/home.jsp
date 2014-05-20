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
        <h2>Fields</h2>

        <div class="pagination dark">
            <c:if test="${preferences.pageNumber == 1}">
                <span style="width: 25px;" class="page disabled">first</span>
                <span style="width: 50px;" class="page disabled">previous</span>
            </c:if>
            <c:if test="${preferences.pageNumber > 1}">
                <a href="/home" style="width: 25px;" class="page dark gradient">first</a>
                <a href="/home/previous" style="width: 50px;" class="page dark gradient">previous</a>
            </c:if>
            <c:forEach items="${pageList}" var="page">
                <c:if test="${preferences.pageNumber == page}">
                    <span class="page dark active"><c:out value='${page}' /></span>
                </c:if>
                <c:if test="${preferences.pageNumber != page}">
                    <a href="/home/<c:out value='${page}' />" class="page dark gradient"><c:out value='${page}' /></a>
                </c:if>
            </c:forEach>
            <c:if test="${endOfTable == false}">
                <a href="/home/next" style="width: 30px;" class="page dark gradient">next?</a>
                <a href="/home/last" style="width: 30px;" class="page dark gradient">last</a>
            </c:if>
            <c:if test="${endOfTable == true}">
                <span style="width: 30px;" class="page disabled">next?</span>
                <span style="width: 30px;" class="page disabled">last</span>
            </c:if>
            &nbsp;
            <form method="post" action="/home/changePageSize" style="display: inline;">
                <span class="page-count">Page 
                    <input type="text" name="pageNumber" size="3" value="<c:out value='${preferences.pageNumber}' />"/>
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
    </div>

    <p>
        Showing <c:out value='${firstFieldName}' /> ... <c:out value='${lastFieldName}' />
    </p>

    <table cellspacing="3" cellpadding="3" border="1">
        <tr>
            <th style="width: 200px;">Field Name</th>
            <th style="width: 100px;">Entry Count</th>
        </tr>
        <c:forEach items="${fieldPageInfoSet}" var="fieldInfo">
            <tr>
                <td>
                    <c:if test="${fieldInfo.timestamp > paginationTimestamp}">
                        <span class="newer">
                        </c:if>
                        <a href="/field_values/<c:out value='${fieldInfo.fieldName}' />"><c:out value='${fieldInfo.fieldName}' /></a>
                        <c:if test="${fieldInfo.timestamp > paginationTimestamp}">
                        </span >
                    </c:if>
                </td>
                <td align="right"><fmt:formatNumber value="${fieldInfo.entryCount}" /></td>
            </tr>
        </c:forEach>
    </table>

</body>
</html>
