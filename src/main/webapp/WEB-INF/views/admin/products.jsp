<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container admin-content">
    <div class="row-between">
        <h1>Manage Products</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/admin/dashboard">&larr; Dashboard</a>
    </div>

    <form class="search-bar admin-search" method="get"
          action="${pageContext.request.contextPath}/admin/products">
        <input type="text" name="q" placeholder="Search by name, description or category..."
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
        <c:when test="${empty products}">
            <div class="empty-state">
                <h2>No products found</h2>
                <p class="muted"><c:out value="${not empty q ? 'Try a different search.' : 'There are no products yet.'}"/></p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-scroll">
                <table class="data-table admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Product</th>
                        <th>Seller</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td><strong>#<c:out value="${p.id}"/></strong></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty p.imageUrl}">
                                        <img class="thumb" src="<c:out value="${p.imageUrl}"/>"
                                             alt="<c:out value="${p.name}"/>">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="thumb placeholder"></div>
                                    </c:otherwise>
                                </c:choose>
                                <strong><c:out value="${p.name}"/></strong>
                            </td>
                            <td class="muted"><c:out value="${p.sellerName}"/></td>
                            <td class="muted"><c:out value="${p.category}"/></td>
                            <td><strong>Rs. <c:out value="${p.price}"/></strong></td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.stockQty > 0}">
                                        <span class="stock-badge in"><c:out value="${p.stockQty}"/> in stock</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="stock-badge out">Out of stock</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <form class="inline" method="post"
                                      action="${pageContext.request.contextPath}/admin/products/remove"
                                      onsubmit="return confirm('Remove listing &quot;<c:out value="${p.name}"/>&quot;? This cannot be undone.');">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <button type="submit" class="btn small danger">Remove</button>
                                </form>
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