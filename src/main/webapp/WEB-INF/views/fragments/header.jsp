<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty requestScope.appName ? 'AyeshaMart' : requestScope.appName}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<header class="site-header">
    <div class="navbar">
        <a class="brand" href="${pageContext.request.contextPath}/home">
            AyeshaMart
            <small>Shop Smarter</small>
        </a>

        <form class="search-bar" method="get"
              action="${pageContext.request.contextPath}/products">
            <input type="text" name="q" placeholder="Search for products, books and more..."
                   aria-label="Search products">
            <button type="submit" title="Search">&rarr;</button>
        </form>

        <nav aria-label="Account">
            <c:choose>
                <c:when test="${not empty sessionScope.userId}">
                    <span class="user-chip">
                        <c:out value="${sessionScope.userName}"/> (<c:out value="${sessionScope.userRole}"/>)
                    </span>
                    <c:if test="${sessionScope.userRole == 'SELLER'}">
                        <a class="nav-link" href="${pageContext.request.contextPath}/seller/products">My Products</a>
                    </c:if>
                    <c:if test="${sessionScope.userRole == 'BUYER'}">
                        <a class="nav-link" href="${pageContext.request.contextPath}/cart">Cart</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/buyer/orders">My Orders</a>
                    </c:if>
                    <c:if test="${sessionScope.userRole == 'ADMIN'}">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin">Admin</a>
                    </c:if>
                    <a class="nav-link" href="${pageContext.request.contextPath}/logout">Logout</a>
                </c:when>
                <c:otherwise>
                    <a class="nav-link" href="${pageContext.request.contextPath}/login">Login</a>
                    <a class="nav-link" href="${pageContext.request.contextPath}/register">Register</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>

    <nav class="nav-cats" aria-label="Categories">
        <a href="${pageContext.request.contextPath}/products">All</a>
        <a href="${pageContext.request.contextPath}/products?category=Electronics">Electronics</a>
        <a href="${pageContext.request.contextPath}/products?category=Fiction">Fiction</a>
        <a href="${pageContext.request.contextPath}/products?category=Thriller">Thriller</a>
        <a href="${pageContext.request.contextPath}/products?category=Mystery">Mystery</a>
        <a href="${pageContext.request.contextPath}/products?category=Children">Children</a>
        <a href="${pageContext.request.contextPath}/products?category=Fantasy">Fantasy</a>
        <a href="${pageContext.request.contextPath}/products?category=Biography">Biography</a>
        <a href="${pageContext.request.contextPath}/products?category=Science">Science</a>
        <a href="${pageContext.request.contextPath}/products?category=Self-Help">Self-Help</a>
        <a href="${pageContext.request.contextPath}/products?category=Sports">Sports</a>
        <a href="${pageContext.request.contextPath}/products?category=Home">Home</a>
        <a href="${pageContext.request.contextPath}/products?category=Clothing">Clothing</a>
    </nav>
</header>