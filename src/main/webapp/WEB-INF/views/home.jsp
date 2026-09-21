<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="fragments/header.jsp" %>

<main class="container">
    <section class="hero">
        <h1>Welcome to ${requestScope.appName}</h1>
        <p>${requestScope.tagline}: buy and sell products across multiple sellers.</p>

        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <p class="muted">Hello <c:out value="${sessionScope.userName}"/>! You are logged in as
                    <c:out value="${sessionScope.userRole}"/>.</p>
                <c:if test="${sessionScope.userRole == 'SELLER'}">
                    <a class="btn" href="${pageContext.request.contextPath}/seller/products">Go to My Products</a>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:if test="${param.logout == '1'}">
                    <div class="alert alert-success">You have been logged out.</div>
                </c:if>
                <div class="row gap">
                    <a class="btn" href="${pageContext.request.contextPath}/login">Login</a>
                    <a class="btn ghost" href="${pageContext.request.contextPath}/register">Create account</a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section>
        <h2>Roadmap</h2>
        <ul>
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