<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${requestScope.appName}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/home">${requestScope.appName}</a>
    <nav>
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <c:if test="${sessionScope.userRole == 'SELLER'}">
                    <a href="${pageContext.request.contextPath}/seller/products">My Products</a>
                </c:if>
                <span class="user">
                    <c:out value="${sessionScope.userName}"/> (<c:out value="${sessionScope.userRole}"/>)
                </span>
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login">Login</a>
                <a href="${pageContext.request.contextPath}/register">Register</a>
            </c:otherwise>
        </c:choose>
    </nav>
</header>