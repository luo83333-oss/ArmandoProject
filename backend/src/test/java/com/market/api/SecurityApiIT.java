package com.market.api;

import com.market.support.ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    void setUp() {
        ApiTestSupport.assumeInfrastructureReady(rest, port);
    }

    @Test
    void protectedEndpointRequiresAuth() throws Exception {
        ResponseEntity<String> response = ApiTestSupport.get(rest, port, "/api/favorites", null);
        assertEquals(401, ApiTestSupport.parseCode(response.getBody()));
    }

    @Test
    void invalidTokenRejected() throws Exception {
        ResponseEntity<String> response = ApiTestSupport.get(rest, port, "/api/orders", "not-a-valid-jwt");
        assertEquals(401, ApiTestSupport.parseCode(response.getBody()));
    }

    @Test
    void publicProductsAccessibleWithoutAuth() throws Exception {
        ResponseEntity<String> response = rest.getForEntity("http://localhost:" + port + "/api/products", String.class);
        assertEquals(0, ApiTestSupport.parseCode(response.getBody()));
    }

    @Test
    void sqlInjectionKeywordDoesNotCrash() throws Exception {
        ResponseEntity<String> response = rest.getForEntity(
                "http://localhost:" + port + "/api/products?keyword=' OR 1=1--",
                String.class
        );
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotEquals(500, ApiTestSupport.parseCode(response.getBody()));
    }

    @Test
    void buyerCannotAccessPlatformOrders() throws Exception {
        String token = ApiTestSupport.registerBuyer(rest, port);
        ResponseEntity<String> response = ApiTestSupport.get(rest, port, "/api/platform/orders", token);
        assertEquals(403, ApiTestSupport.parseCode(response.getBody()));
    }
}
