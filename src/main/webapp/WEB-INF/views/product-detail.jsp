<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="fragments/header.jsp" %>

<main class="container">
    <a class="muted" href="${pageContext.request.contextPath}/products">&larr; Back to products</a>

    <c:if test="${param.added == '1'}">
        <div class="alert alert-success">Added to your cart.</div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-error"><c:out value="${param.error}"/></div>
    </c:if>

    <div class="detail">
        <div class="detail-img">
            <c:choose>
                <c:when test="${not empty product.imageUrl}">
                    <img src="<c:out value="${product.imageUrl}"/>" alt="<c:out value="${product.name}"/>">
                </c:when>
                <c:otherwise>
                    <div class="card-img placeholder"></div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="detail-body">
            <h1><c:out value="${product.name}"/></h1>
            <p><span class="badge"><c:out value="${product.category}"/></span></p>
            <p class="price big">Rs. <c:out value="${product.price}"/></p>
            <p><c:out value="${product.description}"/></p>
            <p class="muted">Sold by <c:out value="${product.sellerName}"/></p>

            <c:choose>
                <c:when test="${product.stockQty > 0}">
                    <p class="muted">In stock: <c:out value="${product.stockQty}"/> available</p>
                </c:when>
                <c:otherwise>
                    <p class="muted">Out of stock</p>
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${empty sessionScope.userId}">
                    <p><a class="btn" href="${pageContext.request.contextPath}/login?redirect=%2Fproducts">Login to add to cart</a></p>
                </c:when>
                <c:when test="${product.stockQty > 0}">
                    <form method="post" class="row gap"
                          action="${pageContext.request.contextPath}/cart/add">
                        <input type="hidden" name="productId" value="${product.id}">
                        <label>Quantity
                            <input type="number" name="quantity" value="1" min="1"
                                   max="${product.stockQty}">
                        </label>
                        <button type="submit" class="btn">Add to Cart</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p class="muted">This product is currently unavailable.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jsp" %>