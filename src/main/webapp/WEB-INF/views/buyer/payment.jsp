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

    <div class="alert demo-note">Demo only - no real payment is collected.</div>

    <div class="card-form">
        <div class="row-between">
            <h2 style="font-size:1.05rem">Amount to pay</h2>
            <span class="price big" style="color:var(--orange)">Rs. <c:out value="${cartTotal}"/></span>
        </div>
        <p class="muted">Balance is recalculated on the server.</p>
    </div>

    <c:if test="${not empty shipping}">
        <div class="card-form">
            <h2 style="font-size:1.05rem;margin-bottom:0.4rem">Deliver to</h2>
            <p class="small">
                <strong><c:out value="${shipping.fullName}"/></strong> &middot;
                <c:out value="${shipping.phone}"/><br>
                <c:out value="${shipping.addressLine1}"/><c:if test="${not empty shipping.addressLine2}">, <c:out value="${shipping.addressLine2}"/></c:if><br>
                <c:out value="${shipping.city}"/>, <c:out value="${shipping.state}"/> - <c:out value="${shipping.pincode}"/>
            </p>
            <a class="muted-link" href="${pageContext.request.contextPath}/buyer/checkout">&larr; Edit delivery details</a>
        </div>
    </c:if>

    <form class="card-form" method="post"
          action="${pageContext.request.contextPath}/buyer/checkout/pay" id="payment-form">

        <fieldset class="pay-methods">
            <legend class="muted small">Select a payment method</legend>
            <label class="pay-method">
                <input type="radio" name="paymentMethod" value="UPI" checked>
                <span><strong>UPI</strong><small>pay via your UPI app</small></span>
            </label>
            <label class="pay-method">
                <input type="radio" name="paymentMethod" value="GPAY">
                <span><strong>Google Pay</strong><small>pay via Google Pay</small></span>
            </label>
            <label class="pay-method">
                <input type="radio" name="paymentMethod" value="CARD">
                <span><strong>Card</strong><small>debit / credit card (demo)</small></span>
            </label>
            <label class="pay-method">
                <input type="radio" name="paymentMethod" value="COD">
                <span><strong>Cash on Delivery</strong><small>pay at your doorstep</small></span>
            </label>
        </fieldset>

        <div class="method-fields" id="fields-UPI">
            <label>UPI ID
                <input type="text" name="upiId" inputmode="email" placeholder="yourname@bank">
            </label>
        </div>
        <div class="method-fields" id="fields-GPAY" hidden>
            <label>UPI ID (Google Pay)
                <input type="text" name="upiId" inputmode="email" placeholder="yourname@okhdfcbank">
            </label>
        </div>
        <div class="method-fields" id="fields-CARD" hidden>
            <label>Cardholder name
                <input type="text" name="cardName" autocomplete="cc-name">
            </label>
            <label>Card number
                <input type="text" name="cardNumber" inputmode="numeric" autocomplete="cc-number"
                       placeholder="4242 4242 4242 4242">
            </label>
            <div class="row">
                <label>Expiry (MM/YY)
                    <input type="text" name="expiry" inputmode="numeric" placeholder="12/30">
                </label>
                <label>CVV
                    <input type="password" name="cvv" inputmode="numeric" maxlength="4" placeholder="123">
                </label>
            </div>
        </div>
        <div class="method-fields" id="fields-COD" hidden>
            <div class="alert demo-note">Cash on Delivery - payment will be PENDING until paid at your door.</div>
        </div>

        <button type="submit" class="btn accent">
            <span id="pay-label">Pay Rs. <c:out value="${cartTotal}"/> &amp; Place Order</span>
        </button>
        <a class="muted-link" href="${pageContext.request.contextPath}/buyer/checkout">&larr; Back to order summary</a>
    </form>
</main>

<script>
    (function () {
        "use strict";
        var form = document.getElementById("payment-form");
        var radios = form.querySelectorAll("input[name='paymentMethod']");
        var fieldsByMethod = {
            "UPI": document.getElementById("fields-UPI"),
            "GPAY": document.getElementById("fields-GPAY"),
            "CARD": document.getElementById("fields-CARD"),
            "COD": document.getElementById("fields-COD")
        };

        function show(method) {
            Object.keys(fieldsByMethod).forEach(function (key) {
                fieldsByMethod[key].hidden = key !== method;
            });
        }

        radios.forEach(function (radio) {
            radio.addEventListener("change", function () {
                show(radio.value);
            });
        });
    })();
</script>

<%@ include file="../fragments/footer.jsp" %>