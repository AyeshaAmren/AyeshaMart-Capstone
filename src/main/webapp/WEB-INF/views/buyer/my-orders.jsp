<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>

<main class="container">
    <div class="row-between">
        <h1>My Orders</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/products">Continue shopping</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty sessionScope.checkoutNotice}">
        <div class="alert alert-success"><c:out value="${sessionScope.checkoutNotice}"/>
            <c:remove var="checkoutNotice" scope="session"/>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="empty-state">
                <h2>No orders yet</h2>
                <p class="muted">When you place an order it will show up here.</p>
                <a class="btn accent" style="margin-top:0.9rem"
                   href="${pageContext.request.contextPath}/products">Start shopping</a>
            </div>
        </c:when>
        <c:otherwise>
            <table class="data-table">
                <thead>
                <tr>
                    <th>Order ID</th>
                    <th>Date</th>
                    <th>Items</th>
                    <th>Total</th>
                    <th>Payment</th>
                    <th>Status</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="o" items="${orders}">
                    <tr>
                        <td><strong>#<c:out value="${o.id}"/></strong></td>
                        <td>
                            <c:if test="${not empty o.createdAt}">
                                <c:out value="${o.createdAt.toLocalDate()}"/>
                            </c:if>
                        </td>
                        <td class="muted"><c:out value="${o.items.size()}"/> item(s)</td>
                        <td><strong>Rs. <c:out value="${o.totalAmount}"/></strong></td>
                        <td>
                            <span class="pay-badge"><c:out value="${o.paymentMethod}"/></span>
                            <span class="pay-badge pay-${fn:toLowerCase(o.paymentStatus)}"><c:out value="${o.paymentStatus}"/></span>
                        </td>
                        <td><span class="status-badge st-${fn:toLowerCase(o.status)}"><c:out value="${o.status}"/></span></td>
                        <td>
                            <a class="btn small" href="${pageContext.request.contextPath}/buyer/orders/details?id=${o.id}">View details</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../fragments/footer.jsp" %>