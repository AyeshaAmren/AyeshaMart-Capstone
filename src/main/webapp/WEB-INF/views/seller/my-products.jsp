<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container">
    <div class="row-between">
        <h1>My Products</h1>
        <a class="btn" href="${pageContext.request.contextPath}/seller/products/add">+ Add Product</a>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success"><c:out value="${message}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty products}">
            <p class="muted">You have no products yet. Use the Add Product button to create your first one.</p>
        </c:when>
        <c:otherwise>
            <table class="data-table">
                <thead>
                <tr>
                    <th>Image</th>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${products}">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${not empty p.imageUrl}">
                                    <img class="thumb" src="<c:out value="${p.imageUrl}"/>" alt="<c:out value="${p.name}"/>">
                                </c:when>
                                <c:otherwise>
                                    <div class="thumb placeholder"></div>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <strong><c:out value="${p.name}"/></strong>
                            <c:if test="${not empty p.description}">
                                <div class="muted small"><c:out value="${fn:substring(p.description, 0, 80)}"/></div>
                            </c:if>
                        </td>
                        <td><c:out value="${p.category}"/></td>
                        <td>Rs. <c:out value="${p.price}"/></td>
                        <td><c:out value="${p.stockQty}"/></td>
                        <td>
                            <a class="btn small" href="${pageContext.request.contextPath}/seller/products/edit?id=${p.id}">Edit</a>
                            <form class="inline" method="post"
                                  action="${pageContext.request.contextPath}/seller/products/delete"
                                  onsubmit="return confirm('Delete &quot;<c:out value="${p.name}"/>&quot;?');">
                                <input type="hidden" name="id" value="${p.id}">
                                <button type="submit" class="btn small danger">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../fragments/footer.jsp" %>