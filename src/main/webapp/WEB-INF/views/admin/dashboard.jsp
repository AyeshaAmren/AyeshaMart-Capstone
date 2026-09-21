<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container admin-content">
    <div class="page-loading" aria-hidden="true"><span>Loading&hellip;</span></div>

    <div class="row-between">
        <h1>Admin Dashboard</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/home">&larr; Back to store</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <div class="stat-grid">
        <div class="stat-card admin-stat">
            <span class="stat-value"><c:out value="${stats.totalUsers}"/></span>
            <span class="stat-label">Total Users</span>
        </div>
        <div class="stat-card admin-stat alt">
            <span class="stat-value"><c:out value="${stats.totalBuyers}"/></span>
            <span class="stat-label">Buyers</span>
        </div>
        <div class="stat-card admin-stat alt">
            <span class="stat-value"><c:out value="${stats.totalSellers}"/></span>
            <span class="stat-label">Sellers</span>
        </div>
        <div class="stat-card admin-stat">
            <span class="stat-value"><c:out value="${stats.totalProducts}"/></span>
            <span class="stat-label">Total Products</span>
        </div>
        <div class="stat-card admin-stat">
            <span class="stat-value"><c:out value="${stats.totalOrders}"/></span>
            <span class="stat-label">Total Orders</span>
        </div>
        <div class="stat-card admin-stat alt">
            <span class="stat-value"><c:out value="${stats.pendingOrders}"/></span>
            <span class="stat-label">Pending Orders</span>
        </div>
        <div class="stat-card admin-stat alt">
            <span class="stat-value"><c:out value="${stats.deliveredOrders}"/></span>
            <span class="stat-label">Delivered Orders</span>
        </div>
        <div class="stat-card admin-stat alt">
            <span class="stat-value"><c:out value="${stats.cancelledOrders}"/></span>
            <span class="stat-label">Cancelled Orders</span>
        </div>
        <div class="stat-card admin-stat highlight">
            <span class="stat-value">Rs. <c:out value="${stats.totalOrderValue}"/></span>
            <span class="stat-label">Total Order Value</span>
        </div>
    </div>

    <div class="admin-shortcuts">
        <a class="btn" href="${pageContext.request.contextPath}/admin/users">Manage Users &rarr;</a>
        <a class="btn" href="${pageContext.request.contextPath}/admin/products">Manage Products &rarr;</a>
        <a class="btn accent" href="${pageContext.request.contextPath}/admin/orders">Manage Orders &rarr;</a>
    </div>
</main>

<%@ include file="../fragments/footer.jsp" %>