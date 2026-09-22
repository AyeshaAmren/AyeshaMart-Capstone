/**
 * AyeshaMart AI shopping assistant (Phase 8).
 *
 * <p>{@link com.ayeshamart.chat.ChatProvider} is the provider contract,
 * {@link com.ayeshamart.chat.OpenAIChatProvider} talks to an OpenAI-compatible
 * API (key supplied only via environment variables / system properties) and
 * {@link com.ayeshamart.chat.FaqChatProvider} is the always-available offline
 * fallback used when the AI provider is missing, slow or unreachable.
 */
package com.ayeshamart.chat;