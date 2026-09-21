<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="fragments/header.jsp" %>

<main class="container narrow">
    <div class="auth-header">
        <span class="brand-logo">AyeshaMart</span>
        <h1 style="font-size:1.1rem;color:var(--muted);font-weight:400;margin-top:0.2rem">Login</h1>
    </div>

    <c:if test="${param.registered == '1'}">
        <div class="alert alert-success">Account created! You can now login.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="post" class="card-form">
        <c:if test="${not empty param.redirect}">
            <input type="hidden" name="redirect" value="<c:out value="${param.redirect}"/>">
        </c:if>

        <label>Email
            <input type="email" name="email" value="<c:out value="${requestScope.email}"/>" required>
        </label>

        <label>Password
            <input type="password" name="password" required>
        </label>

        <button type="submit" class="btn">Login</button>
        <p class="muted">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
    </form>
</main>

<%@ include file="fragments/footer.jsp" %>