package com.ayeshamart.chat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Talks to an OpenAI-compatible chat-completions endpoint using the JDK HTTP
 * client (no extra dependency).
 *
 * <p>The API key is read ONLY at runtime from the
 * {@code AYESHAMART_AI_API_KEY} environment variable (or the
 * {@code ayeshamart.ai.apiKey} system property) - it is never stored in
 * source code, never written to logs, and is sent only inside the request's
 * {@code Authorization} header. Every request carries a short timeout so the
 * caller can fall back to the FAQ when the provider is slow or unreachable.
 * On any failure this provider returns {@code null} ({@code ChatService} then
 * uses the FAQ fallback) and logs only the HTTP status - never the user
 * message and never the response body.
 */
public class OpenAIChatProvider implements ChatProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIChatProvider.class);

    /** Environment variable names - the only place the key may come from in production. */
    public static final String API_KEY_ENV = "AYESHAMART_AI_API_KEY";
    public static final String URL_ENV = "AYESHAMART_AI_URL";
    public static final String MODEL_ENV = "AYESHAMART_AI_MODEL";
    public static final String TIMEOUT_ENV = "AYESHAMART_AI_TIMEOUT_MS";

    /** System property fallback used mainly by tests/deployment tooling. */
    public static final String API_KEY_PROPERTY = "ayeshamart.ai.apiKey";

    static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    static final String DEFAULT_MODEL = "gpt-4o-mini";
    static final long DEFAULT_TIMEOUT_MS = 12000L;

    private static final String SYSTEM_PROMPT =
            "You are the AyeshaMart shopping assistant, an AI helper for the AyeshaMart "
                    + "multi-seller e-commerce demo. Answer only questions about AyeshaMart: its "
                    + "products, sellers, ordering, shipping, mock payment, reviews, returns and "
                    + "accounts. Be friendly, concise (at most 120 words) and factual. If a "
                    + "question is unrelated to AyeshaMart, politely say you can only help with "
                    + "AyeshaMart shopping questions.";

    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final String apiKey;
    private final String url;
    private final String model;
    private final Duration timeout;

    /** Builds a provider from environment variables / system properties. */
    public OpenAIChatProvider() {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(resolveTimeout()))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                resolveApiKey(),
                resolveUrl(),
                resolveModel(),
                Duration.ofMillis(resolveTimeout()));
    }

    public OpenAIChatProvider(String apiKey, String url, String model, Duration timeout) {
        this(HttpClient.newBuilder()
                        .connectTimeout(timeout)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                apiKey, url, model, timeout);
    }

    /** Package-private so tests can inject an in-process HTTP client/endpoint. */
    OpenAIChatProvider(HttpClient httpClient, String apiKey, String url, String model, Duration timeout) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.url = url;
        this.model = model;
        this.timeout = timeout;
    }

    /** Whether a usable API key is configured (env var or system property). */
    public static boolean isConfigured() {
        return notBlank(resolveApiKey());
    }

    @Override
    public String ask(String message) throws Exception {
        if (notBlank(apiKey) == false) {
            log.debug("No AI API key configured - skipping AI provider");
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(message), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.warn("AI provider request failed (status unavailable) - falling back to FAQ");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }

        if (response.statusCode() != 200) {
            // Status code only - never log the body (it may reflect user input).
            log.warn("AI provider returned HTTP {} - falling back to FAQ", response.statusCode());
            return null;
        }

        String content = extractContent(response.body());
        if (content == null || content.isBlank()) {
            log.warn("AI provider returned an empty reply - falling back to FAQ");
            return null;
        }
        return content.trim();
    }

    private String buildRequestBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", message)));
        body.put("temperature", 0.7);
        body.put("max_tokens", 220);
        return gson.toJson(body);
    }

    /** Reads {@code choices[0].message.content} from a chat-completions response. */
    private String extractContent(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null || !message.has("content")) {
                return null;
            }
            return message.get("content").getAsString();
        } catch (Exception e) {
            log.warn("Could not parse AI provider response - falling back to FAQ");
            return null;
        }
    }

    private static String resolveApiKey() {
        String fromEnv = System.getenv(API_KEY_ENV);
        if (notBlank(fromEnv)) {
            return fromEnv;
        }
        String fromProperty = System.getProperty(API_KEY_PROPERTY);
        return notBlank(fromProperty) ? fromProperty : null;
    }

    private static String resolveUrl() {
        return notBlank(System.getenv(URL_ENV)) ? System.getenv(URL_ENV) : DEFAULT_URL;
    }

    private static String resolveModel() {
        return notBlank(System.getenv(MODEL_ENV)) ? System.getenv(MODEL_ENV) : DEFAULT_MODEL;
    }

    private static long resolveTimeout() {
        String configured = System.getenv(TIMEOUT_ENV);
        if (configured != null) {
            try {
                return Long.parseLong(configured.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid {} value '{}' - using {}ms", TIMEOUT_ENV, configured, DEFAULT_TIMEOUT_MS);
            }
        }
        return DEFAULT_TIMEOUT_MS;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}