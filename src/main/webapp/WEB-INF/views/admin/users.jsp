<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container admin-content">
    <div class="row-between">
        <h1>Manage Users</h1>
        <a class="btn ghost" href="${pageContext.request.contextPath}/admin/dashboard">&larr; Dashboard</a>
    </div>

    <form class="search-bar admin-search" method="get"
          action="${pageContext.request.contextPath}/admin/users">
        <input type="text" name="q" placeholder="Search by name or email..."
               value="<c:out value="${not empty q ? q : ''}"/>">
        <button type="submit">Search</button>
    </form>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty users}">
            <div class="empty-state">
                <h2>No users found</h2>
                <p class="muted"><c:out value="${not empty q ? 'Try a different search.' : 'There are no users yet.'}"/></p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-scroll">
                <table class="data-table admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Joined</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td><strong>#<c:out value="${u.id}"/></strong></td>
                            <td><c:out value="${u.name}"/></td>
                            <td class="muted"><c:out value="${u.email}"/></td>
                            <td>
                                <span class="role-badge role-${fn:toLowerCase(u.role)}"><c:out value="${u.role}"/></span>
                            </td>
                            <td>
                                <c:if test="${not empty u.createdAt}"><c:out value="${u.createdAt.toLocalDate()}"/></c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../fragments/footer.jsp" %>