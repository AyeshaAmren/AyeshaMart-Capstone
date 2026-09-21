<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>

<main class="container">
    <ol class="steps">
        <li class="done">Cart</li>
        <li class="done">Order Summary</li>
        <li class="done">Payment</li>
        <li class="active">Confirmation</li>
    </ol>

    <div class="confirmation">
        <div class="confirmation-icon">&#10003;</div>
        <h1>Order Confirmed!</h1>
        <p class="confirmation-copy">
            Thank you for your purchase. This is a <strong>mock payment</strong> - no real
            card was charged. Your order has been placed and the seller has been notified.
        </p>

        <div class="confirmation-detail">
            <div>
                <span class="muted">Order number</span>
                <strong>#<c:out value="${order.id}"/></strong>
            </div>
            <c:if test="${not empty order.createdAt}">
                <div>
                    <span class="muted">Order date</span>
                    <strong><c:out value="${order.createdAt.toLocalDate()}"/></strong>
                </div>
            </c:if>
            <div>
                <span class="muted">Order total</span>
                <strong>Rs. <c:out value="${order.totalAmount}"/></strong>
            </div>
            <div>
                <span class="muted">Status</span>
                <strong><span class="status-badge st-pending">PENDING</span></strong>
            </div>
        </div>

        <div class="confirmation-actions">
            <a class="btn accent" href="${pageContext.request.contextPath}/buyer/orders/details?id=${order.id}">View order details</a>
            <a class="btn ghost" href="${pageContext.request.contextPath}/buyer/orders">My Orders</a>
            <a class="btn ghost" href="${pageContext.request.contextPath}/products">Continue shopping</a>
        </div>
    </div>
</main>

<%@ include file="../fragments/footer.jsp" %>