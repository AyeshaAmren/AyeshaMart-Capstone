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

<!-- AI shopping assistant widget (Phase 8) -->
<button type="button" id="chatbot-toggle" class="chatbot-toggle"
        data-context-path="${pageContext.request.contextPath}"
        aria-label="Open AyeshaMart assistant">
    <span class="chatbot-toggle-icon">&#128172;</span>
    <span class="chatbot-toggle-label">Assistant</span>
</button>

<div class="chatbot" id="chatbot" hidden role="dialog" aria-label="AyeshaMart assistant">
    <div class="chatbot-header">
        <div>
            <strong>AyeshaMart Assistant</strong>
            <small>AI shopping help</small>
        </div>
        <button type="button" id="chatbot-close" aria-label="Close assistant">&times;</button>
    </div>
    <div class="chatbot-messages" id="chatbot-messages" aria-live="polite">
        <div class="chat-msg bot">Hi! I'm the AyeshaMart assistant. Ask me about products, orders, shipping, payments or reviews.</div>
    </div>
    <form class="chatbot-form" id="chatbot-form">
        <input type="text" id="chatbot-input" maxlength="500"
               placeholder="Ask about products, orders, shipping..." autocomplete="off"
               aria-label="Message">
        <button type="submit" aria-label="Send message">Send</button>
    </form>
    <div class="chatbot-note">Demo assistant - no real payments. Replies are generated server-side.</div>
</div>

<script src="${pageContext.request.contextPath}/js/main.js"></script>
<script src="${pageContext.request.contextPath}/js/chatbot.js"></script>
</body>
</html>