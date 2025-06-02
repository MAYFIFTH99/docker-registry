package opensource.dockerregistry.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

            String targetImage = extractImageFromPath(path);
            String username = getAuthenticatedUsername();

            if (response.getStatusCode().is2xxSuccessful()) {
                if (method == HttpMethod.PUT && path.contains("/manifests/")) {
                    auditLogService.log(new AuditLogRequestDto(username, "PUSH", targetImage));
                } else if (method == HttpMethod.GET && path.contains("/manifests/")) {
                    auditLogService.log(new AuditLogRequestDto(username, "PULL", targetImage));
                }
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, value) -> {
                if (key.equalsIgnoreCase("Transfer-Encoding")) {
                    return; // 생략
                }
                if (key.equalsIgnoreCase("Location")) {
                    String proxyBase = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    List<String> rewritten = value.stream()
                            .map(loc -> {
                                try {
                                    URI locUri = URI.create(loc);
                                    String newPath = locUri.getRawPath();
                                    String newQuery = locUri.getRawQuery();
                                    return proxyBase + newPath + (newQuery != null ? "?" + newQuery : "");
                                } catch (Exception e) {
                                    log.warn("Location 헤더 rewrite 실패: {}", loc, e);
                                    return loc;
                                }
                            })
                            .toList();
                    responseHeaders.put(key, rewritten);
                } else {
                    responseHeaders.put(key, value); // ✅ Docker-Content-Digest, Content-Length 등 유지
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

    private String extractImageFromPath(String path) {
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        String[] segments = trimmed.split("/");
        return segments.length >= 2 ? segments[0] : "unknown";
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "anonymous";
    }
}
