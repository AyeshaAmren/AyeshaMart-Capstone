<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="fragments/header.jsp" %>

<main class="container">
    <section class="hero">
        <h1>Welcome to ${requestScope.appName}</h1>
        <p>${requestScope.tagline}: buy and sell products across multiple sellers.</p>
        <p class="muted">Phase 1 - project setup and MVC skeleton. Features arrive in later phases.</p>
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