<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container admin-content">
    <div class="row-between">
        <h1>Manage Orders</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/admin/dashboard">&larr; Dashboard</a>
    </div>

    <form class="search-bar admin-search" method="get"
          action="${pageContext.request.contextPath}/admin/orders">
        <input type="text" name="q" placeholder="Search by order id, buyer, status or payment..."
               value="<c:out value="${not empty q ? q : ''}"/>">
        <button type="submit">Search</button>
    </form>

    <c:if test="${not empty message}">
        <div class="alert alert-success"><c:out value="${message}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="empty-state">
                <h2>No orders found</h2>
                <p class="muted"><c:out value="${not empty q ? 'Try a different search.' : 'There are no orders yet.'}"/></p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-scroll">
                <table class="data-table admin-table">
                    <thead>
                    <tr>
                        <th>Order</th>
                        <th>Date</th>
                        <th>Buyer</th>
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
                                <c:if test="${not empty o.createdAt}"><c:out value="${o.createdAt.toLocalDate()}"/></c:if>
                            </td>
                            <td><c:out value="${o.buyerName}"/></td>
                            <td><strong>Rs. <c:out value="${o.totalAmount}"/></strong></td>
                            <td>
                                <span class="pay-badge"><c:out value="${o.paymentMethod}"/></span>
                                <span class="pay-badge pay-${fn:toLowerCase(o.paymentStatus)}"><c:out value="${o.paymentStatus}"/></span>
                            </td>
                            <td><span class="status-badge st-${fn:toLowerCase(o.status)}"><c:out value="${o.status}"/></span></td>
                            <td>
                                <a class="btn small" href="${pageContext.request.contextPath}/admin/orders/details?id=${o.id}">View details</a>
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