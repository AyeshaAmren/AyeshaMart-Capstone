package com.ayeshamart.service;

import com.ayeshamart.exception.ValidationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for the order lifecycle (Phases 6 + 7).
 *
 * <p>PENDING -&gt; CONFIRMED -&gt; SHIPPED -&gt; DELIVERED. CANCELLED is
 * allowed from PENDING or CONFIRMED, DELIVERED and CANCELLED are terminal.
 * Used by both the Seller module and the Administrator module.
 */
public final class OrderStatus {

    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    private static final Map<String, List<String>> TRANSITIONS;

    static {
        Map<String, List<String>> transitions = new LinkedHashMap<>();
        transitions.put(PENDING, List.of(CONFIRMED, CANCELLED));
        transitions.put(CONFIRMED, List.of(SHIPPED, CANCELLED));
        transitions.put(SHIPPED, List.of(DELIVERED));
        transitions.put(DELIVERED, List.of());
        transitions.put(CANCELLED, List.of());
        TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    private OrderStatus() {
    }

    public static String normalize(String status) {
        return status == null ? null : status.trim().toUpperCase();
    }

    /** Statuses an actor may advance an order to from the given status. */
    public static List<String> allowedNext(String status) {
        if (status == null) {
            return List.of();
        }
        List<String> next = TRANSITIONS.get(status);
        return next == null ? List.of() : next;
    }

    /**
     * Server-side transition check - the source of truth for the workflow.
     * Returns the validated requested status or throws a ValidationException
     * with a human-readable reason.
     */
    public static String transition(String current, String requested) {
        if (current == null) {
            throw new ValidationException("Order not found");
        }
        if (!TRANSITIONS.containsKey(requested)) {
            throw new ValidationException("Unknown status: " + requested);
        }
        if (!TRANSITIONS.containsKey(current)) {
            throw new ValidationException("Order status is final and cannot be changed");
        }
        if (!TRANSITIONS.get(current).contains(requested)) {
            throw new ValidationException("Cannot change order status from " + current + " to " + requested);
        }
        return requested;
    }
}