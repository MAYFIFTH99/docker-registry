package opensource.dockerregistry.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opensource.dockerregistry.backend.dto.AuditLogRequestDto;
import opensource.dockerregistry.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RegistryProxyController {

    private final RestTemplate restTemplate;
    private final AuditLogService auditLogService;

    @Value("${docker.registry.url}")
    private String registryUrl;

    @RequestMapping("/v2/**")
    public ResponseEntity<byte[]> proxyRegistry(HttpMethod method,
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {
        try {
            String path = request.getRequestURI().replaceFirst("/v2", "");
            String query = request.getQueryString();
            String fullUri = registryUrl + path + (query != null ? "?" + query : "");
            URI uri = UriComponentsBuilder.fromHttpUrl(fullUri).build(true).toUri();

            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!List.of("host", "content-length", "transfer-encoding", "connection")
                        .contains(headerName.toLowerCase())) {
                    headers.put(headerName, Collections.list(request.getHeaders(headerName)));
                }
            }

            HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, method, httpEntity, byte[].class);

            // 인증 유저 추출
            String authHeader = headers.getFirst("Authorization");
            String username = extractUsernameFromHeader(authHeader);
            String targetImage = extractImageFromPath(path);

            // PUSH 및 PULL 요청 로그 기록 (정상 응답일 때만 기록)
            if (response.getStatusCode().is2xxSuccessful()) {
                if (method == HttpMethod.PUT && path.contains("/manifests/")) {
                    auditLogService.log(new AuditLogRequestDto(username, "PUSH", targetImage));
                } else if (method == HttpMethod.GET && path.contains("/manifests/")) {
                    auditLogService.log(new AuditLogRequestDto(username, "PULL", targetImage));
                }
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, value) -> {
                if (!key.equalsIgnoreCase("Transfer-Encoding")) {
                    responseHeaders.put(key, value);
                }
            });

            return ResponseEntity.status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("프록시 에러", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Proxy error: " + e.getMessage()).getBytes());
        }
    }

    private String extractUsernameFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length());
                byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
                String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
                return decoded.split(":", 2)[0]; // username:password
            } catch (Exception e) {
                log.warn("Authorization 디코딩 실패", e);
                return "unknown";
            }
        }
        return "anonymous";
    }

    private String extractImageFromPath(String path) {
        // e.g., /myapp/manifests/latest → myapp
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        String[] segments = trimmed.split("/");
        return segments.length >= 2 ? segments[0] : "unknown";
    }
}
