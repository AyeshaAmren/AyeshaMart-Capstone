package com.ayeshamart.chat;

/**
 * Contract for the AyeshaMart AI shopping assistant (Phase 8).
 *
 * <p>A provider turns one user message into an assistant reply. Returning
 * {@code null} or an empty string - or throwing - signals that no usable
 * answer could be produced, which lets {@code ChatService} fall back to the
 * offline FAQ provider instead of failing the request.
 */
public interface ChatProvider {

    /** Maximum length (characters) of a single user message the assistant accepts. */
    int MAX_MESSAGE_LENGTH = 500;

    /**
     * Produces an assistant reply for the given message.
     *
     * @param message trimmed user input (never null after ChatService validation)
     * @return a reply, or {@code null}/{@code ""} when no answer could be produced
     * @throws Exception when the provider fails and should be skipped by the caller
     */
    String ask(String message) throws Exception;
}