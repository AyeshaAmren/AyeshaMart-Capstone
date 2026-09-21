<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container">
    <div class="page-loading" aria-hidden="true"><span>Loading&hellip;</span></div>

    <div class="row-between">
        <h1>Incoming Orders</h1>
        <a class="btn" href="${pageContext.request.contextPath}/seller/dashboard">&larr; Dashboard</a>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success"><c:out value="${message}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="empty-state">
                <h2>No incoming orders</h2>
                <p class="muted">Orders containing your products will appear here.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-scroll">
                <table class="data-table seller-table">
                    <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Unit price</th>
                        <th>Subtotal</th>
                        <th>Buyer</th>
                        <th>Order date</th>
                        <th>Status</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td><strong>#<c:out value="${o.orderId}"/></strong></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty o.imageUrl}">
                                        <img class="thumb" src="<c:out value="${o.imageUrl}"/>"
                                             alt="<c:out value="${o.productName}"/>">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="thumb placeholder"></div>
                                    </c:otherwise>
                                </c:choose>
                                <strong><c:out value="${o.productName}"/></strong>
                            </td>
                            <td><c:out value="${o.quantity}"/></td>
                            <td>Rs. <c:out value="${o.unitPrice}"/></td>
                            <td><strong>Rs. <c:out value="${o.subtotal}"/></strong></td>
                            <td class="muted"><c:out value="${o.buyerName}"/></td>
                            <td class="muted"><c:out value="${o.orderDate.toLocalDate()}"/></td>
                            <td><span class="status-badge st-${fn:toLowerCase(o.status)}"><c:out value="${o.status}"/></span></td>
                            <td>
                                <a class="btn small"
                                   href="${pageContext.request.contextPath}/seller/orders/details?id=${o.orderId}">View</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../fragments/footer.jsp" %>