<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container">
    <div class="page-loading" aria-hidden="true"><span>Loading&hellip;</span></div>

    <div class="row-between">
        <h1>Order #<c:out value="${orderId}"/></h1>
        <a class="btn" href="${pageContext.request.contextPath}/seller/orders">&larr; Incoming Orders</a>
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
            <strong><c:out value="${buyerName}"/></strong>
        </div>
        <div>
            <span class="muted">Order date</span>
            <strong><c:out value="${orderDate.toLocalDate()}"/></strong>
        </div>
        <div>
            <span class="muted">Status</span>
            <strong><span class="status-badge st-${fn:toLowerCase(orderStatus)}"><c:out value="${orderStatus}"/></span></strong>
        </div>
        <div>
            <span class="muted">Your items total</span>
            <strong>Rs. <c:out value="${orderTotal}"/></strong>
        </div>
        <div>
            <span class="muted">Payment</span>
            <strong>
                <span class="pay-badge"><c:out value="${paymentMethod}"/></span>
                <span class="pay-badge pay-${fn:toLowerCase(paymentStatus)}"><c:out value="${paymentStatus}"/></span>
            </strong>
        </div>
    </div>

    <div class="table-scroll">
        <table class="data-table seller-table">
            <thead>
            <tr>
                <th>Product</th>
                <th>Quantity</th>
                <th>Unit price</th>
                <th>Subtotal</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="line" items="${lines}">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${not empty line.imageUrl}">
                                <img class="thumb" src="<c:out value="${line.imageUrl}"/>"
                                     alt="<c:out value="${line.productName}"/>">
                            </c:when>
                            <c:otherwise>
                                <div class="thumb placeholder"></div>
                            </c:otherwise>
                        </c:choose>
                        <strong><c:out value="${line.productName}"/></strong>
                    </td>
                    <td><c:out value="${line.quantity}"/></td>
                    <td>Rs. <c:out value="${line.unitPrice}"/></td>
                    <td><strong>Rs. <c:out value="${line.subtotal}"/></strong></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <c:if test="${not empty shippingAddressLine1}">
        <div class="shipping-card">
            <h2 class="small-title">Shipping Address</h2>
            <p>
                <strong><c:out value="${shippingFullName}"/></strong> &middot; <c:out value="${shippingPhone}"/><br>
                <c:out value="${shippingAddressLine1}"/><c:if test="${not empty shippingAddressLine2}">, <c:out value="${shippingAddressLine2}"/></c:if><br>
                <c:out value="${shippingCity}"/>, <c:out value="${shippingState}"/> - <c:out value="${shippingPincode}"/>
                <c:if test="${not empty shippingLandmark}"><br><span class="muted">Landmark: <c:out value="${shippingLandmark}"/></span></c:if>
            </p>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty allowedNext}">
            <div class="alert demo-note">This order is final (<c:out value="${orderStatus}"/>) - no further changes are allowed.</div>
        </c:when>
        <c:otherwise>
            <div class="status-actions">
                <span class="muted">Advance order status:</span>
                <c:forEach var="next" items="${allowedNext}">
                    <form class="inline" method="post"
                          action="${pageContext.request.contextPath}/seller/orders/status"
                          <c:if test="${next == 'CANCELLED'}">
                              onsubmit="return confirm('Cancel order #<c:out value="${orderId}"/>? This action cannot be undone.');"
                          </c:if>>
                        <input type="hidden" name="orderId" value="${orderId}">
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