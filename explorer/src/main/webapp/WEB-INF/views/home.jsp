<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>D4M Explorer</title>
<style>
body {
    background: #dfdfdf;
    color: #666;
    font: 14px/24px sans-serif;
}

#container {
    width: 600px;
    margin: 0 auto;
    padding: 20px;
}

.btn {
    display: inline-block;
    padding: 10px;
    border-radius: 5px; /*optional*/
    color: #aaa;
    font-size: .875em;
}

.pagination {
    background: #f2f2f2;
    padding: 10px;
    margin-bottom: 10px;
}

.page {
    display: inline-block;
    padding: 0px 3px;
    margin-right: 4px;
    border-radius: 3px;
    border: solid 1px #c0c0c0;
    background: #e9e9e9;
    box-shadow: inset 0px 1px 0px rgba(255,255,255, .8), 0px 1px 3px rgba(0,0,0, .1);
    font-size: .875em;
    font-weight: bold;
    text-decoration: none;
    color: #717171;
    text-shadow: 0px 1px 0px rgba(255,255,255, 1);
}

.page:hover, .page.gradient:hover {
    background: #fefefe;
    background: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#FEFEFE), to(#f0f0f0));
    background: -moz-linear-gradient(0% 0% 270deg,#FEFEFE, #f0f0f0);
}

.page.active {
    border: none;
    background: #616161;
    box-shadow: inset 0px 0px 8px rgba(0,0,0, .5), 0px 1px 0px rgba(255,255,255, .8);
    color: #f0f0f0;
    text-shadow: 0px 0px 3px rgba(0,0,0, .5);
}

.page.gradient {
    background: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#f8f8f8), to(#e9e9e9));
    background: -moz-linear-gradient(0% 0% 270deg,#f8f8f8, #e9e9e9);
}

.pagination.dark {
//  background: #414449;
    background: #dfdfdf;
    color: #feffff;
}

.page.disabled {
    border: solid 1px #32373b;
    background: #3e4347;
    box-shadow: inset 0px 1px 1px rgba(255,255,255, .1), 0px 1px 3px rgba(0,0,0, .1);
    color: grey;
    text-shadow: 0px 1px 0px rgba(0,0,0, .5);
}

.page.dark {
    width: 20px;
    text-align: right;
    border: solid 1px #32373b;
    background: #3e4347;
    box-shadow: inset 0px 1px 1px rgba(255,255,255, .1), 0px 1px 3px rgba(0,0,0, .1);
    color: #feffff;
    text-shadow: 0px 1px 0px rgba(0,0,0, .5);
}

.page.dark:hover, .page.dark.gradient:hover {
    background: #3d4f5d;
    background: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#547085), to(#3d4f5d));
    background: -moz-linear-gradient(0% 0% 270deg,#547085, #3d4f5d);
}

.page.dark.active {
    border: none;
    background: #ffff33;
    color: black;
    box-shadow: inset 0px 0px 8px rgba(0,0,0, .5), 0px 1px 0px rgba(255,255,255, .1);
}

.page.dark.gradient {
    background: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#565b5f), to(#3e4347));
    background: -moz-linear-gradient(0% 0% 270deg,#565b5f, #3e4347);
}

.page-count {
    color: black;
}
.newer {
    border-left: 2px yellow solid;
}
</style>
    </head>
    <body>
        <h1>D4M Explorer</h1>
        <h2>Columns</h2>

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
            <th style="width: 200px;">Name</th>
            <th>Count</th>
        </tr>
        <c:forEach items="${fieldPageInfoSet}" var="fieldInfo">
            <tr>
                <td>
                    <c:if test="${fieldInfo.timestamp > paginationTimestamp}">
                        <span class="newer">
                    </c:if>
                    <c:out value='${fieldInfo.fieldName}' />
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
