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
    <c:if test="${param.reviewed == '1'}">
        <div class="alert alert-success">Thanks for your review!</div>
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

    <section class="reviews" aria-label="Customer reviews">
        <div class="row-between">
            <h2>Ratings &amp; Reviews</h2>
            <span class="muted">
                <c:choose>
                    <c:when test="${ratingCount > 0}">
                        <span class="stars">
                            <c:forEach var="i" begin="1" end="5">
                                <c:choose>
                                    <c:when test="${i <= averageRating.intValue()}">&#9733;</c:when>
                                    <c:otherwise>&#9734;</c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </span>
                        <c:out value="${averageRating}"/> / 5 (<c:out value="${ratingCount}"/> review(s))
                    </c:when>
                    <c:otherwise>No reviews yet</c:otherwise>
                </c:choose>
            </span>
        </div>

        <c:if test="${empty sessionScope.userId}">
            <div class="review-cta">
                <a class="btn accent" href="${pageContext.request.contextPath}/login?redirect=%2Fproducts%2Fdetail%3Fid%3D${product.id}">Login to review this product</a>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.userId && sessionScope.userRole == 'BUYER'}">
            <c:choose>
                <c:when test="${canReview}">
                    <form class="card-form review-form" method="post"
                          action="${pageContext.request.contextPath}/buyer/review">
                        <input type="hidden" name="productId" value="${product.id}">
                        <h3 style="font-size:1rem">Write a review</h3>
                        <label>Your rating
                            <select name="rating" required>
                                <option value="5">5 - Excellent</option>
                                <option value="4">4 - Good</option>
                                <option value="3">3 - Average</option>
                                <option value="2">2 - Poor</option>
                                <option value="1">1 - Terrible</option>
                            </select>
                        </label>
                        <label>Comment
                            <textarea name="comment" rows="3" maxlength="1000"
                                      placeholder="Share what you liked or disliked about this product"></textarea>
                        </label>
                        <button type="submit" class="btn accent">Submit Review</button>
                    </form>
                </c:when>
                <c:when test="${hasReviewed}">
                    <div class="alert alert-success">You have already reviewed this product. Thank you!</div>
                </c:when>
                <c:otherwise>
                    <div class="alert demo-note">You can review this product once you purchase it.</div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:choose>
            <c:when test="${empty reviews}">
                <p class="muted">This product has no customer reviews yet.</p>
            </c:when>
            <c:otherwise>
                <div class="review-list">
                    <c:forEach var="review" items="${reviews}">
                        <div class="review-card">
                            <div class="row-between">
                                <strong><c:out value="${review.userName}"/></strong>
                                <span class="stars">
                                    <c:forEach var="i" begin="1" end="5">
                                        <c:choose>
                                            <c:when test="${i <= review.rating}">&#9733;</c:when>
                                            <c:otherwise>&#9734;</c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </span>
                            </div>
                            <div class="muted small">
                                <c:if test="${not empty review.createdAt}"><c:out value="${review.createdAt.toLocalDate()}"/></c:if>
                            </div>
                            <c:if test="${not empty review.comment}">
                                <p class="review-comment"><c:out value="${review.comment}"/></p>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<%@ include file="fragments/footer.jsp" %>