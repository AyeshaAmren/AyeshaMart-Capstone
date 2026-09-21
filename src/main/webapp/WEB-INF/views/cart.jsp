<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="fragments/header.jsp" %>

<main class="container">
    <div class="row-between">
        <h1>Your Cart</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/products">Keep shopping</a>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success"><c:out value="${message}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty items}">
            <div class="empty-state">
                <h2>Your cart is empty</h2>
                <p class="muted">Looks like you haven&rsquo;t added anything yet.</p>
                <a class="btn accent" style="margin-top:0.9rem"
                   href="${pageContext.request.contextPath}/products">Continue shopping</a>
            </div>
        </c:when>
        <c:otherwise>
            <table class="data-table">
                <thead>
                <tr>
                    <th>Product</th>
                    <th>Unit price</th>
                    <th>Quantity</th>
                    <th>Subtotal</th>
                    <th></th>
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
                            <div class="muted small">
                                <c:out value="${item.sellerName}"/> |
                                Stock: <c:out value="${item.stockQty}"/>
                            </div>
                        </td>
                        <td>Rs. <c:out value="${item.unitPrice}"/></td>
                        <td>
                            <div class="qty">
                                <form method="post" action="${pageContext.request.contextPath}/cart/update">
                                    <input type="hidden" name="productId" value="${item.productId}">
                                    <button type="submit" name="quantity" value="${item.quantity - 1}"
                                            class="btn small ghost" title="Decrease quantity"
                                        <c:if test="${item.quantity <= 1}">disabled</c:if>>&minus;</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/cart/update">
                                    <input type="hidden" name="productId" value="${item.productId}">
                                    <input type="number" name="quantity" value="${item.quantity}" min="1"
                                           max="${item.stockQty}" class="qty-input">
                                    <button type="submit" class="btn small">Update</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/cart/update">
                                    <input type="hidden" name="productId" value="${item.productId}">
                                    <button type="submit" name="quantity" value="${item.quantity + 1}"
                                            class="btn small ghost" title="Increase quantity"
                                        <c:if test="${item.quantity >= item.stockQty}">disabled</c:if>>&plus;</button>
                                </form>
                            </div>
                        </td>
                        <td><strong>Rs. <c:out value="${item.subtotal}"/></strong></td>
                        <td>
                            <form class="inline" method="post"
                                  action="${pageContext.request.contextPath}/cart/remove"
                                  onsubmit="return confirm('Remove &quot;<c:out value="${item.productName}"/>&quot; from your cart?');">
                                <input type="hidden" name="productId" value="${item.productId}">
                                <button type="submit" class="btn small danger">Remove</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div class="row-between cart-total">
                <h2>Cart Total: Rs. <c:out value="${cartTotal}"/></h2>
                <a class="btn accent" href="${pageContext.request.contextPath}/buyer/checkout">Proceed to Checkout</a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="fragments/footer.jsp" %>