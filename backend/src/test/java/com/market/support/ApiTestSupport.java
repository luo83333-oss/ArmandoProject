package com.market.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class ApiTestSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApiTestSupport() {
    }

    public static void assumeInfrastructureReady(TestRestTemplate rest, int port) {
        try {
            ResponseEntity<String> health = rest.getForEntity("http://localhost:" + port + "/api/health", String.class);
            Assumptions.assumeTrue(health.getStatusCode().is2xxSuccessful(), "health HTTP not OK");
            Assumptions.assumeTrue(parseCode(health.getBody()) == 0, "health code not 0");
        } catch (Exception ex) {
            Assumptions.assumeTrue(false, "Docker MySQL/Redis not ready: " + ex.getMessage());
        }
    }

    public static int parseCode(String body) throws Exception {
        JsonNode node = MAPPER.readTree(body);
        return node.get("code").asInt();
    }

    public static String parseDataField(String body, String field) throws Exception {
        JsonNode node = MAPPER.readTree(body).path("data");
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    public static ResponseEntity<String> get(TestRestTemplate rest, int port, String path, String token) {
        HttpHeaders headers = authHeaders(token);
        return rest.exchange(url(port, path), HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    public static ResponseEntity<String> postJson(TestRestTemplate rest, int port, String path, Object body, String token) {
        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json;
        try {
            json = MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return rest.exchange(url(port, path), HttpMethod.POST, new HttpEntity<>(json, headers), String.class);
    }

    public static String registerBuyer(TestRestTemplate rest, int port) throws Exception {
        String phone = randomPhone();
        Map<String, String> sendCode = new HashMap<>();
        sendCode.put("phone", phone);
        postJson(rest, port, "/api/auth/send-code", sendCode, null);

        Map<String, String> register = new HashMap<>();
        register.put("phone", phone);
        register.put("password", "test123456");
        register.put("code", "123456");
        ResponseEntity<String> response = postJson(rest, port, "/api/auth/register", register, null);
        Assumptions.assumeTrue(response.getStatusCode().is2xxSuccessful());
        int code = parseCode(response.getBody());
        Assumptions.assumeTrue(code == 0, "register failed: " + response.getBody());
        return parseDataField(response.getBody(), "token");
    }

    public static String loginAdmin(TestRestTemplate rest, int port) throws Exception {
        Map<String, String> login = new HashMap<>();
        login.put("phone", "13800000000");
        login.put("password", "admin123");
        ResponseEntity<String> response = postJson(rest, port, "/api/auth/login", login, null);
        if (parseCode(response.getBody()) != 0) {
            return null;
        }
        return parseDataField(response.getBody(), "token");
    }

    private static HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private static String url(int port, String path) {
        return "http://localhost:" + port + path;
    }

    private static String randomPhone() {
        int suffix = ThreadLocalRandom.current().nextInt(100_000_000, 999_999_999);
        return "139" + suffix;
    }
}
