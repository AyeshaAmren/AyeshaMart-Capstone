<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container">
    <div class="row-between">
        <h1>Seller Dashboard</h1>
        <a class="btn accent" href="${pageContext.request.contextPath}/seller/orders">View Incoming Orders</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <div class="stat-grid">
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.totalProducts}"/></span>
            <span class="stat-label">Total Products</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.activeProducts}"/></span>
            <span class="stat-label">Active Products</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.totalIncomingOrders}"/></span>
            <span class="stat-label">Total Incoming Orders</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.pendingOrders}"/></span>
            <span class="stat-label">Pending Orders</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.confirmedOrders}"/></span>
            <span class="stat-label">Confirmed Orders</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.shippedOrders}"/></span>
            <span class="stat-label">Shipped Orders</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.deliveredOrders}"/></span>
            <span class="stat-label">Delivered Orders</span>
        </div>
        <div class="stat-card">
            <span class="stat-value"><c:out value="${stats.cancelledOrders}"/></span>
            <span class="stat-label">Cancelled Orders</span>
        </div>
    </div>
</main>

<%@ include file="../fragments/footer.jsp" %>