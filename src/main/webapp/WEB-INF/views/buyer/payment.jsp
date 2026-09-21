<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>

<main class="container narrow">
    <h1>Mock Payment</h1>
    <ol class="steps">
        <li class="done">Cart</li>
        <li class="done">Order Summary</li>
        <li class="active">Payment</li>
        <li>Confirmation</li>
    </ol>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <div class="alert demo-note">Demo only - no real card is charged.</div>

    <div class="card-form">
        <div class="row-between">
            <h2 style="font-size:1.05rem">Amount to pay</h2>
            <span class="price big" style="color:var(--orange)">Rs. <c:out value="${cartTotal}"/></span>
        </div>
        <p class="muted">2 item(s) in this order. Balance is recalculated on the server.</p>
    </div>

    <form class="card-form" method="post" action="${pageContext.request.contextPath}/buyer/checkout/pay">
        <label>Cardholder name
            <input type="text" name="cardName" autocomplete="cc-name" required>
        </label>
        <label>Card number
            <input type="text" name="cardNumber" inputmode="numeric" autocomplete="cc-number"
                   placeholder="4242 4242 4242 4242" required>
        </label>
        <div class="row">
            <label>Expiry (MM/YY)
                <input type="text" name="expiry" inputmode="numeric" placeholder="12/30" required>
            </label>
            <label>CVV
                <input type="password" name="cvv" inputmode="numeric" maxlength="4" placeholder="123" required>
            </label>
        </div>

        <button type="submit" class="btn accent">Pay Rs. <c:out value="${cartTotal}"/> &amp; Place Order</button>
        <a class="muted-link" href="${pageContext.request.contextPath}/buyer/checkout">&larr; Back to order summary</a>
    </form>
</main>

<%@ include file="../fragments/footer.jsp" %>