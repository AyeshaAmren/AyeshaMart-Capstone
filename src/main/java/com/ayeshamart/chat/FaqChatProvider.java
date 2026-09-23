package com.ayeshamart.chat;

import java.util.Arrays;
import java.util.List;

/**
 * Offline FAQ fallback for the AyeshaMart assistant (Phase 8).
 *
 * <p>Keyword-based intent matching so the chatbot always answers AyeshaMart
 * questions even when the AI provider is not configured, times out or fails.
 * It never throws, never calls external services and never writes anything
 * to logs, so it is a safe last line of defence.
 */
public class FaqChatProvider implements ChatProvider {

    @Override
    public String ask(String message) {
        String q = message == null ? "" : message.toLowerCase().trim();

        if (matchesAny(q,
                "what do you sell", "what products", "products you sell", "what is ayeshamart",
                "about ayeshamart", "what is this site", "tell me about", "categories",
                "books available", "products available", "store sell")) {
            return "AyeshaMart is a multi-seller bookstore demo. Sellers list books "
                    + "(Fiction, Thriller, Mystery, Children, Fantasy, Biography, "
                    + "Science and Self-Help) and buyers search them by "
                    + "name or category. Browse everything from the home page or the catalogue.";

        }
        if (matchesAny(q,
                "search", "find a product", "looking for", "filter", "category")) {
            return "Use the search bar in the header to search by product name. The category strip "
                    + "above the catalogue filters by category, and the home page shows featured "
                    + "products. Only in-stock products are shown.";

        }
        if (matchesAny(q,
                "add to cart", "shopping cart", "cart", "buy a product", "how do i buy",
                "order a product", "purchase")) {
            return "To buy: open a product and click Add to Cart, then open the Cart from the "
                    + "header. Go to Checkout, enter your delivery address, pick a mock payment "
                    + "method (UPI, Google Pay, Card demo or Cash on Delivery) and place the "
                    + "order. Prices are always recalculated on the server.";

        }
        if (matchesAny(q,
                "payment method", "how do i pay", "mock payment", "upi", "gpay", "gpay ",
                "credit card", "debit card", "cash on delivery", "cod", "pay online", "card")) {
            return "Payments are a DEMO only - no real money is collected. AyeshaMart supports "
                    + "UPI, Google Pay, Card and Cash on Delivery. Card/UPI values are validated "
                    + "and immediately discarded; only the method, status and a payment reference "
                    + "are stored with the order - never card numbers, CVV or OTPs.";

        }
        if (matchesAny(q,
                "shipping", "delivery", "free delivery", "how long", "arrive", "dispatch",
                "deliver to", "address")) {
            return "Every product ships with free delivery. After payment your order starts as "
                    + "PENDING, then the seller confirms, ships and finally marks it DELIVERED. "
                    + "The delivery address you entered at checkout is saved with the order.";

        }
        if (matchesAny(q,
                "order status", "track order", "where is my order", "order history", "cancelled",
                "cancel order", "delivered", "my orders")) {
            return "Your orders (and their status: PENDING, CONFIRMED, SHIPPED, DELIVERED or "
                    + "CANCELLED) are listed under My Orders. You can track a specific order from "
                    + "its details page. To cancel, contact the seller or the admin.";

        }
        if (matchesAny(q,
                "return", "refund", "replace", "money back", "defective")) {
            return "For a demo, returns and refunds are handled by the seller or an administrator. "
                    + "Contact them from the order details page and they can cancel the order. "
                    + "Cash on Delivery orders are only charged when delivered.";

        }
        if (matchesAny(q,
                "review", "rating", "rate a product", "star", "feedback")) {
            return "Buyers who purchased a product can rate it 1-5 stars and leave a short review "
                    + "on the product page (one review per product). Reviews help other shoppers "
                    + "decide.";

        }
        if (matchesAny(q,
                "become a seller", "sell on", "start selling", "list a product", "seller account",
                "how do i sell", "register as seller", "sell products")) {
            return "Create an account and choose the SELLER role. From My Products you can add, "
                    + "edit and remove your listings, and the seller dashboard shows your incoming "
                    + "orders and lets you update their status.";

        }
        if (matchesAny(q,
                "login", "log in", "register", "create account", "sign up", "forgot password",
                "password", "account")) {
            return "Use Login or Create Account in the header. New buyers and sellers register "
                    + "freely; administrator accounts are only created in the database. Signing in "
                    + "unlocks cart, checkout, orders, seller tools and the admin dashboard.";

        }
        if (matchesAny(q,
                "admin", "administrator", "manage users", "moderate", "dashboard")) {
            return "Administrators can see an overview dashboard, manage users, moderate "
                    + "products (remove/unlist listings) and move any order through its "
                    + "status workflow.";

        }
        if (matchesAny(q,
                "contact", "help", "support", "human", "email them", "call")) {
            return "This is a college demo, so there is no live support desk. For help, write "
                    + "to the project's administrator or use the README to explore the database "
                    + "and the code.";

        }
        if (matchesAny(q,
                "hi", "hello", "hey", "namaste", "good morning", "good afternoon", "good evening",
                "yo", "thanks", "thank you", "bye", "goodbye")) {
            return "Hello! I'm the AyeshaMart assistant. Ask me about products, searching, "
                    + "ordering, shipping, payments, reviews or becoming a seller.";
        }

        return "I can help with AyeshaMart questions: products and search, the shopping cart, "
                + "checkout and mock payment, shipping and returns, reviews, seller accounts and "
                + "the admin dashboard. Try asking \"What do you sell?\" or \"How do I place an "
                + "order?\".";
    }

    private static boolean matchesAny(String query, String... phrases) {
        List<String> words = Arrays.asList(phrases);
        for (String phrase : words) {
            if (query.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
}