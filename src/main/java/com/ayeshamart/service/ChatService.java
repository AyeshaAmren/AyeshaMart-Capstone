package com.ayeshamart.service;

import com.ayeshamart.chat.ChatProvider;
import com.ayeshamart.chat.FaqChatProvider;
import com.ayeshamart.chat.OpenAIChatProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chatbot business logic (Phase 8).
 *
 * <ul>
 *   <li>validates input: blank messages are nudged, messages longer than
 *       {@value #MAX_MESSAGE_LENGTH} characters are rejected,</li>
 *   <li>rate-limits each session to {@value #MAX_MESSAGES_PER_MINUTE} messages
 *       per rolling minute so the AI endpoint cannot be flooded,</li>
 *   <li>asks the AI provider first and transparently falls back to the offline
 *       FAQ when the AI provider is not configured, times out or returns
 *       nothing usable,</li>
 *   <li>caps overly long replies so the widget stays readable.</li>
 * </ul>
 *
 * <p>Security note: user messages are never written to logs - only anonymous
 * counters/warnings with no message content.
 */
public class ChatService {

    public static final int MAX_MESSAGES_PER_MINUTE = 10;
    public static final int MAX_MESSAGE_LENGTH = ChatProvider.MAX_MESSAGE_LENGTH;
    public static final int MAX_REPLY_LENGTH = 1200;
    public static final long WINDOW_MILLIS = 60_000L;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final Map<String, Deque<Long>> MESSAGE_HISTORY = new ConcurrentHashMap<>();

    private final ChatProvider aiProvider;
    private final ChatProvider faqProvider;

    public ChatService() {
        this(OpenAIChatProvider.isConfigured() ? new OpenAIChatProvider() : null,
                new FaqChatProvider());
    }

    /** Package-private so tests can inject stub/fake providers. */
    ChatService(ChatProvider aiProvider, ChatProvider faqProvider) {
        this.aiProvider = aiProvider;
        this.faqProvider = faqProvider;
    }

    /**
     * Produces a reply for the user's message within a chat session.
     *
     * @param sessionId the HTTP session id (used only for rate limiting)
     * @param message   the raw user message
     * @return a reply that is always safe to display
     */
    public String ask(String sessionId, String message) {
        String cleaned = message == null ? "" : message.trim();

        if (cleaned.isBlank()) {
            return "Please type a question, for example: \"What products does AyeshaMart sell?\"";
        }
        if (cleaned.length() > MAX_MESSAGE_LENGTH) {
            return "Your message is too long. Please keep it under "
                    + MAX_MESSAGE_LENGTH + " characters.";
        }
        if (!acquireSlot(sessionId)) {
            return "You're sending messages very quickly. Please wait a moment and try again.";
        }

        if (aiProvider != null) {
            try {
                String reply = aiProvider.ask(cleaned);
                if (reply != null && !reply.isBlank()) {
                    return cap(reply);
                }
            } catch (Exception e) {
                // Warning only - never log the user's message.
                log.warn("AI chatbot provider failed unexpectedly - using FAQ fallback");
            }
        }
        try {
            String reply = faqProvider.ask(cleaned);
            if (reply != null && !reply.isBlank()) {
                return cap(reply);
            }
        } catch (Exception e) {
            log.warn("FAQ chatbot provider failed unexpectedly");
        }
        return "I'm sorry, I could not answer that right now. Please try again in a moment.";
    }

    private String cap(String reply) {
        if (reply.length() <= MAX_REPLY_LENGTH) {
            return reply;
        }
        return reply.substring(0, MAX_REPLY_LENGTH).trim() + "...";
    }

    /**
     * Rolling-minute slot reservation per session id. Returns false when the
     * session has already used this minute's budget.
     */
    private boolean acquireSlot(String sessionId) {
        String key = sessionId == null || sessionId.isBlank() ? "anonymous" : sessionId;
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = MESSAGE_HISTORY.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_MESSAGES_PER_MINUTE) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    /** Test helper - clears rate-limit state. */
    static void clearRateLimits() {
        MESSAGE_HISTORY.clear();
    }
}