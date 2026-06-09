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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    void setUp() {
        ApiTestSupport.assumeInfrastructureReady(rest, port);
    }

    @Test
    void healthReturnsOk() throws Exception {
        ResponseEntity<String> response = rest.getForEntity("http://localhost:" + port + "/api/health", String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(0, ApiTestSupport.parseCode(response.getBody()));
    }
}
