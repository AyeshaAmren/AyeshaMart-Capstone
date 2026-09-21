<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>

<main class="container">
    <h1>Checkout</h1>
    <ol class="steps">
        <li class="active">Cart</li>
        <li>Order Summary</li>
        <li>Payment</li>
        <li>Confirmation</li>
    </ol>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <div class="row-between">
        <h2 style="font-size:1.1rem">Order Summary</h2>
        <a class="muted-link" href="${pageContext.request.contextPath}/cart">&larr; Back to cart</a>
    </div>

    <table class="data-table">
        <thead>
        <tr>
            <th>Product</th>
            <th>Unit price</th>
            <th>Quantity</th>
            <th>Subtotal</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="item" items="${items}">
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
                <td>Rs. <c:out value="${item.unitPrice}"/></td>
                <td><c:out value="${item.quantity}"/></td>
                <td><strong>Rs. <c:out value="${item.subtotal}"/></strong></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <div class="cart-total">
        <div>
            <h2 style="font-size:1.05rem;margin-bottom:0.2rem">Total: Rs. <c:out value="${cartTotal}"/></h2>
            <span class="free-delivery">Free delivery on this order</span>
        </div>
        <a class="btn accent" href="${pageContext.request.contextPath}/buyer/checkout/payment">Proceed to Payment</a>
    </div>
</main>

<%@ include file="../fragments/footer.jsp" %>