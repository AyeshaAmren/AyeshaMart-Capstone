<footer class="footer">
    <div class="footer-top">
        <div>
            <h4>AyeshaMart</h4>
            <a href="${pageContext.request.contextPath}/home">Home</a>
            <a href="${pageContext.request.contextPath}/products">Browse Products</a>
            <a href="${pageContext.request.contextPath}/register">Become a Seller</a>
            <a href="${pageContext.request.contextPath}/cart">Cart</a>
        </div>
        <div>
            <h4>Categories</h4>
            <a href="${pageContext.request.contextPath}/products?category=Electronics">Electronics</a>
            <a href="${pageContext.request.contextPath}/products?category=Fiction">Fiction</a>
            <a href="${pageContext.request.contextPath}/products?category=Thriller">Thriller</a>
            <a href="${pageContext.request.contextPath}/products?category=Children">Children</a>
        </div>
        <div>
            <h4>Collections</h4>
            <a href="${pageContext.request.contextPath}/products?category=Fantasy">Fantasy</a>
            <a href="${pageContext.request.contextPath}/products?category=Mystery">Mystery</a>
            <a href="${pageContext.request.contextPath}/products?category=Biography">Biography</a>
            <a href="${pageContext.request.contextPath}/products?category=Science">Science</a>
        </div>
        <div>
            <h4>Help</h4>
            <a href="${pageContext.request.contextPath}/login">Login</a>
            <a href="${pageContext.request.contextPath}/register">Create Account</a>
        </div>
    </div>
    <div class="footer-bottom">
        &copy; 2026 ${requestScope.appName} - Multi-Seller E-Commerce Demo
    </div>
</footer>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>