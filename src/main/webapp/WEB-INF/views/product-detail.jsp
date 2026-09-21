<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="fragments/header.jsp" %>

<main class="container">
    <a class="muted-link" href="${pageContext.request.contextPath}/products">&larr; Back to products</a>

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
                    <div class="place-block"></div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="detail-body">
            <span class="badge"><c:out value="${product.category}"/></span>
            <h1><c:out value="${product.name}"/></h1>
            <p class="desc"><c:out value="${product.description}"/></p>

            <p class="price big">Rs. <c:out value="${product.price}"/></p>
            <p class="free-delivery">Eligible for FREE delivery</p>
            <p class="seller-line">Sold by <c:out value="${product.sellerName}"/></p>

            <c:choose>
                <c:when test="${product.stockQty > 0}">
                    <p class="stock-badge">In stock</p>
                    <p class="muted">${product.stockQty} available</p>
                </c:when>
                <c:otherwise>
                    <p class="stock-badge out">Out of stock</p>
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${empty sessionScope.userId}">
                    <div class="detail-actions">
                        <a class="btn accent"
                           href="${pageContext.request.contextPath}/login?redirect=%2Fproducts">Login to add to cart</a>
                    </div>
                </c:when>
                <c:when test="${product.stockQty > 0}">
                    <form method="post" class="detail-actions"
                          action="${pageContext.request.contextPath}/cart/add">
                        <input type="hidden" name="productId" value="${product.id}">
                        <label class="muted">Quantity
                            <input type="number" name="quantity" value="1" min="1"
                                   max="${product.stockQty}" class="qty-input">
                        </label>
                        <button type="submit" class="btn accent">Add to Cart</button>
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