<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="../fragments/header.jsp" %>
<%@ include file="nav.jsp" %>

<main class="container narrow">
    <c:set var="editing" value="${mode == 'edit'}"/>
    <h1><c:out value="${editing ? 'Edit Product' : 'Add Product'}"/></h1>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/seller/products/${editing ? 'edit' : 'add'}"
          method="post" class="card-form">
        <c:if test="${editing}">
            <input type="hidden" name="id" value="<c:out value="${product.id}"/>">
        </c:if>

        <label>Product name
            <input type="text" name="name" required
                   value="<c:out value="${editing ? product.name : requestScope.name}"/>">
        </label>

        <label>Description
            <textarea name="description" rows="3"><c:out value="${editing ? product.description : requestScope.description}"/></textarea>
        </label>

        <div class="row">
            <label>Price (Rs.)
                <input type="number" step="0.01" min="0" name="price" required
                       value="<c:out value="${editing ? product.price : requestScope.price}"/>">
            </label>
            <label>Stock quantity
                <input type="number" min="0" name="stockQty" required
                       value="<c:out value="${editing ? product.stockQty : requestScope.stockQty}"/>">
            </label>
        </div>

        <label>Category
            <c:set var="currentCategory" value="${editing ? product.category : requestScope.category}"/>
            <select name="category" required>
                <option value="" disabled
                    <c:if test="${empty currentCategory}">selected</c:if>>Select a category</option>
                <c:forEach var="cat" items="${['Fiction','Thriller','Mystery','Children','Fantasy','Biography','Science','Self-Help','Books']}">
                    <option value="${cat}"
                        <c:if test="${currentCategory == cat}">selected</c:if>><c:out value="${cat}"/></option>
                </c:forEach>
            </select>
        </label>

        <label>Image URL (optional)
            <input type="text" name="imageUrl"
                   value="<c:out value="${editing ? product.imageUrl : requestScope.imageUrl}"/>">
        </label>

        <div class="row">
            <button type="submit" class="btn"><c:out value="${editing ? 'Save changes' : 'Add product'}"/></button>
            <a class="btn ghost" href="${pageContext.request.contextPath}/seller/products">Cancel</a>
        </div>
    </form>
</main>

<%@ include file="../fragments/footer.jsp" %>