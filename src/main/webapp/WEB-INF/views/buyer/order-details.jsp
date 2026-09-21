<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>

<main class="container">
    <div class="row-between">
        <h1>Order Details</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/buyer/orders">&larr; My Orders</a>
    </div>

    <div class="order-info">
        <div>
            <span class="muted">Order</span>
            <strong>#<c:out value="${order.id}"/></strong>
        </div>
        <div>
            <span class="muted">Placed on</span>
            <strong>
                <c:if test="${not empty order.createdAt}"><c:out value="${order.createdAt.toLocalDate()}"/></c:if>
            </strong>
        </div>
        <div>
            <span class="muted">Status</span>
            <strong><span class="status-badge st-${fn:toLowerCase(order.status)}"><c:out value="${order.status}"/></span></strong>
        </div>
        <div>
            <span class="muted">Payment</span>
            <strong>
                <span class="pay-badge"><c:out value="${order.paymentMethod}"/></span>
                <span class="pay-badge pay-${fn:toLowerCase(order.paymentStatus)}"><c:out value="${order.paymentStatus}"/></span>
                <c:if test="${not empty order.paymentReference}">
                    <div class="muted small">Ref: <c:out value="${order.paymentReference}"/></div>
                </c:if>
            </strong>
        </div>
    </div>

    <table class="data-table">
        <thead>
        <tr>
            <th>Product</th>
            <th>Quantity</th>
            <th>Unit price</th>
            <th>Subtotal</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="item" items="${order.items}">
            <tr>
                <td>
                    <c:choose>
                        <c:when test="${not empty item.imageUrl}">
                            <img class="thumb" src="<c:out value="${item.imageUrl}"/>"
                                 alt="<c:out value="${item.productName}"/>">
                        </c:when>
                        <c:otherwise>
                            <div class="thumb placeholder"></div>
                        </c:otherwise>
                    </c:choose>
                    <strong><c:out value="${item.productName}"/></strong>
                    <div class="muted small"><c:out value="${item.sellerName}"/></div>
                </td>
                <td><c:out value="${item.quantity}"/></td>
                <td>Rs. <c:out value="${item.unitPrice}"/></td>
                <td><strong>Rs. <c:out value="${item.subtotal}"/></strong></td>
                <td>
                    <a class="btn small" href="${pageContext.request.contextPath}/products/detail?id=${item.productId}">View</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <div class="cart-total">
        <div>
            <h2 style="font-size:1.05rem;margin-bottom:0.2rem">Order total: Rs. <c:out value="${order.totalAmount}"/></h2>
            <span class="free-delivery">Free delivery included</span>
        </div>
        <a class="btn accent" href="${pageContext.request.contextPath}/products">Continue shopping</a>
    </div>

    <c:if test="${not empty order.shippingAddressLine1}">
        <div class="shipping-card">
            <h2 class="small-title">Delivery Address</h2>
            <p>
                <strong><c:out value="${order.shippingFullName}"/></strong> &middot; <c:out value="${order.shippingPhone}"/><br>
                <c:out value="${order.shippingAddressLine1}"/><c:if test="${not empty order.shippingAddressLine2}">, <c:out value="${order.shippingAddressLine2}"/></c:if><br>
                <c:out value="${order.shippingCity}"/>, <c:out value="${order.shippingState}"/> - <c:out value="${order.shippingPincode}"/><br>
                <c:if test="${not empty order.shippingLandmark}"><span class="muted">Landmark: <c:out value="${order.shippingLandmark}"/></span></c:if>
            </p>
        </div>
    </c:if>
</main>

<%@ include file="../fragments/footer.jsp" %>