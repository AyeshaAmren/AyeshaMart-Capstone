<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="fragments/header.jsp" %>

<main class="container narrow">
    <h1>Register</h1>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="post" class="card-form">
        <label>Full name
            <input type="text" name="name" value="<c:out value="${requestScope.name}"/>" required>
        </label>

        <label>Email
            <input type="email" name="email" value="<c:out value="${requestScope.email}"/>" required>
        </label>

        <label>Password
            <input type="password" name="password" required>
        </label>

        <label>Confirm password
            <input type="password" name="confirmPassword" required>
        </label>

        <label>Register as
            <select name="role" required>
                <option value="" disabled <c:if test="${empty requestScope.role}">selected</c:if>>Select a role</option>
                <option value="BUYER" <c:if test="${requestScope.role == 'BUYER'}">selected</c:if>>Buyer</option>
                <option value="SELLER" <c:if test="${requestScope.role == 'SELLER'}">selected</c:if>>Seller</option>
            </select>
        </label>

        <button type="submit" class="btn">Create account</button>
        <p class="muted">Already have an account? <a href="${pageContext.request.contextPath}/login">Login</a></p>
    </form>
</main>

<%@ include file="fragments/footer.jsp" %>