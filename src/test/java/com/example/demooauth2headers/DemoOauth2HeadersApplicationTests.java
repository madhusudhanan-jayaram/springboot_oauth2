package com.example.demooauth2headers;

import com.example.demooauth2headers.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "app.jwt.secret=testSecretKey123456789012345678901234567890",
    "app.jwt.access-mins=15",
    "app.jwt.refresh-days=7"
})
class DemoOauth2HeadersApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Test
    void contextLoads() {
        assertThat(jwtService).isNotNull();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String url = "http://localhost:" + port + "/auth/login";
        
        Map<String, String> loginRequest = Map.of(
            "username", "user",
            "password", "password"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("login ok");
        assertThat(response.getHeaders().getFirst("X-Access-Token")).isNotNull();
        assertThat(response.getHeaders().getFirst("X-Refresh-Token")).isNotNull();
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() {
        // First login to get a token
        String loginUrl = "http://localhost:" + port + "/auth/login";
        Map<String, String> loginRequest = Map.of(
            "username", "user",
            "password", "password"
        );

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);
        
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(loginUrl, loginEntity, Map.class);
        String accessToken = loginResponse.getHeaders().getFirst("X-Access-Token");
        assertThat(accessToken).isNotNull();
        
        // Now access protected endpoint
        String apiUrl = "http://localhost:" + port + "/api/hello";
        HttpHeaders apiHeaders = new HttpHeaders();
        if (accessToken != null) {
            apiHeaders.setBearerAuth(accessToken);
        }
        HttpEntity<Void> apiEntity = new HttpEntity<>(apiHeaders);
        
        ResponseEntity<String> apiResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, apiEntity, String.class);
        
        assertThat(apiResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getBody()).isEqualTo("Hello, user!");
    }

    @Test
    void shouldRejectUnauthenticatedRequest() {
        String url = "http://localhost:" + port + "/api/hello";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}