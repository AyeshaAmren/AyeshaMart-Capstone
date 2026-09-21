<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container admin-content">
    <div class="page-loading" aria-hidden="true"><span>Loading&hellip;</span></div>

    <div class="row-between">
        <h1>Order #<c:out value="${order.id}"/></h1>
        <a class="btn" href="${pageContext.request.contextPath}/admin/orders">&larr; Manage Orders</a>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success"><c:out value="${message}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <div class="order-info">
        <div>
            <span class="muted">Buyer</span>
            <strong><c:out value="${order.buyerName}"/></strong>
            <div class="muted small">#<c:out value="${order.buyerId}"/></div>
        </div>
        <div>
            <span class="muted">Order date</span>
            <strong><c:if test="${not empty order.createdAt}"><c:out value="${order.createdAt.toLocalDate()}"/></c:if></strong>
        </div>
        <div>
            <span class="muted">Order total</span>
            <strong>Rs. <c:out value="${order.totalAmount}"/></strong>
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

    <c:if test="${not empty order.shippingAddressLine1}">
        <div class="shipping-card">
            <h2 class="small-title">Shipping Address</h2>
            <p>
                <strong><c:out value="${order.shippingFullName}"/></strong> &middot; <c:out value="${order.shippingPhone}"/><br>
                <c:out value="${order.shippingAddressLine1}"/><c:if test="${not empty order.shippingAddressLine2}">, <c:out value="${order.shippingAddressLine2}"/></c:if><br>
                <c:out value="${order.shippingCity}"/>, <c:out value="${order.shippingState}"/> - <c:out value="${order.shippingPincode}"/>
                <c:if test="${not empty order.shippingLandmark}"><br><span class="muted">Landmark: <c:out value="${order.shippingLandmark}"/></span></c:if>
            </p>
        </div>
    </c:if>

    <div class="table-scroll">
        <table class="data-table admin-table">
            <thead>
            <tr>
                <th>Product</th>
                <th>Seller</th>
                <th>Quantity</th>
                <th>Unit price</th>
                <th>Subtotal</th>
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
                    </td>
                    <td class="muted"><c:out value="${item.sellerName}"/></td>
                    <td><c:out value="${item.quantity}"/></td>
                    <td>Rs. <c:out value="${item.unitPrice}"/></td>
                    <td><strong>Rs. <c:out value="${item.subtotal}"/></strong></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <c:choose>
        <c:when test="${empty allowedNext}">
            <div class="alert demo-note">This order is final (<c:out value="${order.status}"/>) - no further changes are allowed.</div>
        </c:when>
        <c:otherwise>
            <div class="status-actions">
                <span class="muted">Manage order status:</span>
                <c:forEach var="next" items="${allowedNext}">
                    <form class="inline" method="post"
                          action="${pageContext.request.contextPath}/admin/orders/status"
                          <c:if test="${next == 'CANCELLED'}">
                              onsubmit="return confirm('Cancel order #<c:out value="${order.id}"/>? This action cannot be undone.');"
                          </c:if>>
                        <input type="hidden" name="orderId" value="${order.id}">
                        <c:choose>
                            <c:when test="${next == 'CANCELLED'}">
                                <button type="submit" name="status" value="CANCELLED" class="btn small danger">Cancel Order</button>
                            </c:when>
                            <c:otherwise>
                                <button type="submit" name="status" value="${next}" class="btn small accent">
                                    Mark as <c:out value="${fn:toLowerCase(next)}"/>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </form>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../fragments/footer.jsp" %>