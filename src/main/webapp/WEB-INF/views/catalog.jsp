<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="fragments/header.jsp" %>

<main class="container wide">
    <div class="row-between">
        <h1>Browse Products</h1>
        <a class="btn" href="${pageContext.request.contextPath}/products">Clear filter</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <form class="card-form filter-bar" method="get"
          action="${pageContext.request.contextPath}/products">
        <label>Search
            <input type="text" name="q" value="<c:out value="${q}"/>"
                   placeholder="Search by name or description">
        </label>
        <label>Category
            <select name="category">
                <option value="">All categories</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat}"
                        <c:if test="${selectedCategory == cat}">selected</c:if>><c:out value="${cat}"/></option>
                </c:forEach>
            </select>
        </label>
        <div class="filter-actions">
            <button type="submit" class="btn">Search</button>
        </div>
    </form>

    <p class="muted">
        <c:choose>
            <c:when test="${empty products}">No products match your search.</c:when>
            <c:otherwise>${fn:length(products)} product(s) found.</c:otherwise>
        </c:choose>
    </p>

    <div class="catalog">
        <c:forEach var="p" items="${products}">
            <a class="card" href="${pageContext.request.contextPath}/products/detail?id=${p.id}">
                <c:choose>
                    <c:when test="${not empty p.imageUrl}">
                        <img src="<c:out value="${p.imageUrl}"/>" alt="<c:out value="${p.name}"/>">
                    </c:when>
                    <c:otherwise>
                        <div class="card-img placeholder"></div>
                    </c:otherwise>
                </c:choose>
                <div class="card-body">
                    <h3><c:out value="${p.name}"/></h3>
                    <c:if test="${not empty p.description}">
                        <p class="muted"><c:out value="${fn:substring(p.description, 0, 90)}"/></p>
                    </c:if>
                    <div class="row-between">
                        <span class="price">Rs. <c:out value="${p.price}"/></span>
                        <span class="badge"><c:out value="${p.category}"/></span>
                    </div>
                    <div class="muted small">
                        Stock: <c:out value="${p.stockQty}"/> | Sold by <c:out value="${p.sellerName}"/>
                    </div>
                </div>
            </a>
        </c:forEach>
    </div>
</main>

<%@ include file="fragments/footer.jsp" %>