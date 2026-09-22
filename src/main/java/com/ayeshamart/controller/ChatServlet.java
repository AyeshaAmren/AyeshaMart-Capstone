package com.ayeshamart.controller;

import com.ayeshamart.service.ChatService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI chatbot endpoint (Phase 8).
 *
 * <pre>
 *   GET  /api/v1/chat  -> service info (probe)
 *   POST /api/v1/chat  -> body {"message": "..."}  -> {"reply": "..."}
 * </pre>
 *
 * <p>The endpoint is public (not behind AuthFilter) so visitors can ask the
 * assistant before logging in. A session is created to rate-limit at
 * 10 messages/minute/session inside {@link ChatService}. The API key lives
 * only on the server ({@code AYESHAMART_AI_API_KEY}) and is never sent to
 * the browser. Request bodies are length-limited and replies are JSON with a
 * UTF-8 content type.
 */
@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {

    static final int MAX_BODY_CHARS = 4096;

    private static final String SERVICE_INFO = "AyeshaMart AI shopping assistant - POST {\"message\": \"...\"}";

    private final Gson gson = new Gson();
    private final ChatService chatService = new ChatService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        writeJson(response, Map.of("service", SERVICE_INFO));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String message;
        try {
            String body = readBody(request);
            message = parseMessage(body);
        } catch (IllegalArgumentException e) {
            writeJson(response, Map.of("reply", "I could not read your message. Please try again."));
            return;
        }

        HttpSession session = request.getSession(true);
        String reply = chatService.ask(session.getId(), message);

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("reply", reply);
        writeJson(response, payload);
    }

    private String parseMessage(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("empty body");
        }
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("message")) {
            throw new IllegalArgumentException("missing 'message' field");
        }
        return root.get("message").isJsonNull() ? "" : root.get("message").getAsString();
    }

    /** Reads the request body up to {@value #MAX_BODY_CHARS} characters. */
    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
                if (content.length() > MAX_BODY_CHARS) {
                    throw new IOException("Request body too large");
                }
            }
        }
        return content.toString();
    }

    private void writeJson(HttpServletResponse response, Map<String, ?> payload) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(payload));
    }
}