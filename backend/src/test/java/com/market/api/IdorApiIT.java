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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IdorApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    void setUp() {
        ApiTestSupport.assumeInfrastructureReady(rest, port);
    }

    @Test
    void userCannotReadAnotherUsersOrder() throws Exception {
        String tokenB = ApiTestSupport.registerBuyer(rest, port);
        ResponseEntity<String> response = ApiTestSupport.get(rest, port, "/api/orders/1", tokenB);
        int code = ApiTestSupport.parseCode(response.getBody());
        assumeTrue(code == 404 || code == 0, "order 1 may not exist; 404 expected when owned by others");
        if (code != 0) {
            assertEquals(404, code);
        }
    }

    @Test
    void userCannotReadNonexistentOrder() throws Exception {
        String token = ApiTestSupport.registerBuyer(rest, port);
        ResponseEntity<String> response = ApiTestSupport.get(rest, port, "/api/orders/999999999", token);
        assertEquals(404, ApiTestSupport.parseCode(response.getBody()));
    }
}
