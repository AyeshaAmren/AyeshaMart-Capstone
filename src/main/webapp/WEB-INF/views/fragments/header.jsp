<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
        <span class="soon">Products</span>
        <span class="soon">Cart</span>
        <span class="soon">Login</span>
    </nav>
</header>