package com.ayeshamart.chat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI provider tests (Phase 8) run against an in-process {@code HttpServer} so
 * the request format, authentication header and response parsing are verified
 * WITHOUT any external network call or real API key.
 */
class OpenAIChatProviderTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private interface Handler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private String startServer(Handler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            try {
                handler.handle(exchange);
            } finally {
                exchange.close();
            }
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions";
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private OpenAIChatProvider provider(String url, String apiKey) {
        return new OpenAIChatProvider(HttpClient.newHttpClient(), apiKey, url, "gpt-test",
                Duration.ofSeconds(5));
    }

    @Test
    void sendsWellFormedRequestAndExtractsContent() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> auth = new AtomicReference<>();
        String url = startServer(exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, 200,
                    "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Hello from AI\"}}]}");
        });

        String reply = provider(url, "sk-test-123").ask("hello");

        assertEquals("Hello from AI", reply);
        assertEquals("Bearer sk-test-123", auth.get());
        assertTrue(body.get().contains("\"model\":\"gpt-test\""), "request carries the configured model");
        assertTrue(body.get().contains("\"role\":\"system\""), "request carries a system prompt");
        assertTrue(body.get().contains("\"role\":\"user\""), "request carries the user message");
        assertTrue(body.get().contains("hello"), "request contains the user input");
    }

    @Test
    void returnsNullOnNon200() throws Exception {
        String url = startServer(exchange -> respond(exchange, 500, "{\"error\":\"boom\"}"));
        assertNull(provider(url, "sk-test-123").ask("hello"));
    }

    @Test
    void returnsNullOnMalformedBody() throws Exception {
        String url = startServer(exchange -> respond(exchange, 200, "{}"));
        assertNull(provider(url, "sk-test-123").ask("hello"));
    }

    @Test
    void returnsNullOnEmptyChoices() throws Exception {
        String url = startServer(exchange -> respond(exchange, 200, "{\"choices\":[]}"));
        assertNull(provider(url, "sk-test-123").ask("hello"));
    }

    @Test
    void neverCallsServerWithoutApiKey() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        String url = startServer(exchange -> {
            hits.incrementAndGet();
            respond(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"x\"}}]}");
        });

        assertNull(provider(url, null).ask("hello"));
        assertEquals(0, hits.get(), "no HTTP call should be made when no API key is configured");
    }
}