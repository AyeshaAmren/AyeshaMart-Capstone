<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>

<main class="container">
    <h1>Checkout</h1>
    <ol class="steps">
        <li class="done">Cart</li>
        <li class="active">Order Summary</li>
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

    <div class="table-scroll">
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
    </div>

    <div class="cart-total">
        <div>
            <h2 style="font-size:1.05rem;margin-bottom:0.2rem">Total: Rs. <c:out value="${cartTotal}"/></h2>
            <span class="free-delivery">Free delivery on this order</span>
        </div>
    </div>

    <form class="card-form" method="post"
          action="${pageContext.request.contextPath}/buyer/checkout" novalidate>
        <h2 style="font-size:1.1rem;margin-bottom:0.75rem">Delivery Details</h2>
        <p class="muted small" style="margin-bottom:1rem">
            These details are saved with your order as a snapshot and used to deliver it.
        </p>

        <label>Full Name
            <input type="text" name="fullName" maxlength="100" required
                   value="<c:out value="${not empty shipping ? shipping.fullName : ''}"/>">
        </label>
        <label>Phone
            <input type="tel" name="phone" inputmode="numeric" maxlength="13" required
                   placeholder="10-digit mobile number"
                   value="<c:out value="${not empty shipping ? shipping.phone : ''}"/>">
        </label>
        <label>Address Line 1
            <input type="text" name="addressLine1" maxlength="200" required
                   placeholder="House no, street, area"
                   value="<c:out value="${not empty shipping ? shipping.addressLine1 : ''}"/>">
        </label>
        <label>Address Line 2 <span class="muted small">(optional)</span>
            <input type="text" name="addressLine2" maxlength="200" placeholder="Building, apartment, etc."
                   value="<c:out value="${not empty shipping ? shipping.addressLine2 : ''}"/>">
        </label>
        <div class="row">
            <label>City
                <input type="text" name="city" maxlength="100" required
                       value="<c:out value="${not empty shipping ? shipping.city : ''}"/>">
            </label>
            <label>State
                <input type="text" name="state" maxlength="100" required
                       value="<c:out value="${not empty shipping ? shipping.state : ''}"/>">
            </label>
        </div>
        <div class="row">
            <label>Pincode
                <input type="text" name="pincode" inputmode="numeric" maxlength="6" required
                       placeholder="6-digit postal code"
                       value="<c:out value="${not empty shipping ? shipping.pincode : ''}"/>">
            </label>
            <label>Landmark <span class="muted small">(optional)</span>
                <input type="text" name="landmark" maxlength="200"
                       value="<c:out value="${not empty shipping ? shipping.landmark : ''}"/>">
            </label>
        </div>

        <button type="submit" class="btn accent">Continue to Payment &rarr;</button>
        <a class="muted-link" href="${pageContext.request.contextPath}/cart">&larr; Back to cart</a>
    </form>
</main>

<%@ include file="../fragments/footer.jsp" %>