<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="fragments/header.jsp" %>

<main class="container">

    <c:if test="${param.logout == '1'}">
        <div class="alert alert-success">You have been logged out.</div>
    </c:if>

    <section class="banner">
        <div class="banner-text">
            <h1>Shop Everything <em>&amp; More</em></h1>
            <p class="tagline">${requestScope.tagline} - books, electronics and fashion from multiple sellers.</p>
            <div class="hero-actions">
                <a class="btn accent" href="${pageContext.request.contextPath}/products">Shop Now</a>
                <a class="btn ghost" href="${pageContext.request.contextPath}/register">Become a Seller</a>
            </div>
        </div>
        <img class="banner-img"
             src="https://picsum.photos/seed/ayeshamart-banner/560/360"
             alt="Featured products">
    </section>

    <c:choose>
        <c:when test="${not empty sessionScope.userId}">
            <div class="welcome">
                <p>
                    Hello <strong><c:out value="${sessionScope.userName}"/></strong>! You are logged in as
                    <c:out value="${sessionScope.userRole}"/>.
                    <c:if test="${sessionScope.userRole == 'SELLER'}">
                        Head over to <a href="${pageContext.request.contextPath}/seller/products">My Products</a>.
                    </c:if>
                </p>
                <c:if test="${sessionScope.userRole == 'BUYER'}">
                    <a class="btn accent" href="${pageContext.request.contextPath}/cart">View Cart</a>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="welcome">
                <p>New here? <strong>Register as a buyer or seller</strong> to start shopping or selling.</p>
                <div>
                    <a class="btn accent" href="${pageContext.request.contextPath}/login">Login</a>
                    <a class="btn ghost" href="${pageContext.request.contextPath}/register">Create account</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty categories}">
        <div class="tiles">
            <c:forEach var="cat" items="${categories}" begin="0" end="9">
                <a class="tile" href="${pageContext.request.contextPath}/products?category=${cat}">
                    <img src="https://picsum.photos/seed/ayeshamart-${cat}/160/160"
                         alt="<c:out value="${cat}"/>"><span><c:out value="${cat}"/></span>
                </a>
            </c:forEach>
        </div>
    </c:if>

    <div class="section-head">
        <h2>Featured for You</h2>
        <a class="muted-link" href="${pageContext.request.contextPath}/products">View all</a>
    </div>

    <div class="catalog">
        <c:forEach var="p" items="${featured}" begin="0" end="7">
            <a class="card" href="${pageContext.request.contextPath}/products/detail?id=${p.id}">
                <div class="card-img-wrap">
                    <c:choose>
                        <c:when test="${not empty p.imageUrl}">
                            <img src="<c:out value="${p.imageUrl}"/>" alt="<c:out value="${p.name}"/>">
                        </c:when>
                        <c:otherwise>
                            <div class="img-placeholder"></div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="card-body">
                    <h3><c:out value="${p.name}"/></h3>
                    <div class="row-between" style="margin:0">
                        <span class="price">Rs. <c:out value="${p.price}"/></span>
                        <span class="badge"><c:out value="${p.category}"/></span>
                    </div>
                    <span class="free-delivery">Free delivery</span>
                </div>
            </a>
        </c:forEach>
    </div>

    <div class="section-head">
        <h2>Project Roadmap</h2>
    </div>
    <section class="welcome">
        <ul style="padding-left:1.2rem;line-height:1.9">
            <c:forEach var="item" items="${[
                'F1: Registration & Login',
                'F2: Product CRUD',
                'F3: Browse, Search & Filter',
                'F4: Shopping Cart',
                'F5: Checkout (mock payment)',
                'F6: Orders (Buyer & Seller)',
                'F7: Admin Dashboard',
                'F8: Reviews & Ratings',
                'AI: Shopping Assistant Chatbot'
            ]}">
                <li>${item}</li>
            </c:forEach>
        </ul>
    </section>
</main>

<%@ include file="fragments/footer.jsp" %>