package com.ayeshamart.service;

import com.ayeshamart.chat.ChatProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chatbot service tests (Phase 8) - input limits, per-session rate limiting
 * and AI -> FAQ fallback behaviour, using stub providers so no external AI
 * call is ever made.
 */
class ChatServiceTest {

    private ChatProvider faq;

    @BeforeEach
    void setUp() {
        faq = message -> "FAQ reply";
        ChatService.clearRateLimits();
    }

    private ChatService serviceWith(ChatProvider ai) {
        return new ChatService(ai, faq);
    }

    @Test
    void usesAiReplyWhenAvailable() {
        ChatService service = serviceWith(message -> "AI reply for " + message);
        assertEquals("AI reply for hello", service.ask("s1", "hello"));
    }

    @Test
    void fallsBackToFaqWhenAiThrows() {
        ChatService service = serviceWith(message -> {
            throw new RuntimeException("provider down");
        });
        assertEquals("FAQ reply", service.ask("s1", "hello"));
    }

    @Test
    void fallsBackToFaqWhenAiReturnsNull() {
        assertEquals("FAQ reply", serviceWith(message -> null).ask("s1", "hello"));
    }

    @Test
    void fallsBackToFaqWhenAiReturnsBlank() {
        assertEquals("FAQ reply", serviceWith(message -> "  ").ask("s1", "hello"));
    }

    @Test
    void fallsBackToFaqWhenAiIsNotConfigured() {
        assertEquals("FAQ reply", new ChatService(null, faq).ask("s1", "hello"));
    }

    @Test
    void promptsForBlankMessage() {
        assertTrue(serviceWith(message -> "AI").ask("s1", "   ")
                .startsWith("Please type a question"));
    }

    @Test
    void promptsForNullMessage() {
        assertTrue(serviceWith(message -> "AI").ask("s1", null).contains("type a question"));
    }

    @Test
    void rejectsMessageLongerThanLimit() {
        String longMessage = IntStream.range(0, ChatService.MAX_MESSAGE_LENGTH + 1)
                .mapToObj(i -> "a").collect(Collectors.joining());
        assertTrue(serviceWith(message -> "AI").ask("s1", longMessage).contains("too long"));
    }

    @Test
    void capsOverlongAiReply() {
        String huge = IntStream.range(0, ChatService.MAX_REPLY_LENGTH + 500)
                .mapToObj(i -> "x").collect(Collectors.joining());
        String reply = serviceWith(message -> huge).ask("s1", "hello");
        assertTrue(reply.length() <= ChatService.MAX_REPLY_LENGTH + 3);
        assertTrue(reply.endsWith("..."));
    }

    @Test
    void rateLimitsEachSessionToTenMessagesPerMinute() {
        ChatService service = serviceWith(message -> "ok");
        for (int i = 0; i < ChatService.MAX_MESSAGES_PER_MINUTE; i++) {
            assertEquals("ok", service.ask("sess-rate", "msg " + i));
        }
        String blocked = service.ask("sess-rate", "one more");
        assertTrue(blocked.contains("very quickly"), "11th message should be rate limited");
    }

    @Test
    void rateLimitIsPerSession() {
        ChatService service = serviceWith(message -> "ok");
        for (int i = 0; i < ChatService.MAX_MESSAGES_PER_MINUTE; i++) {
            service.ask("sess-a", "msg " + i);
        }
        assertTrue(service.ask("sess-a", "more").contains("very quickly"));
        assertEquals("ok", service.ask("sess-b", "fresh session still works"));
    }

    @Test
    void userMessageIsNeverLeakedIntoReplies() {
        ChatService service = serviceWith(message -> "echo");
        assertFalse(service.ask("s1", "<script>alert(1)</script>").contains("<script>"));
    }
}