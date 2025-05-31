package opensource.dockerregistry.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opensource.dockerregistry.backend.dto.TagListResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final RestTemplate registryRestTemplate;

    public ResponseEntity<Void> checkV2Support() {
        try {
            ResponseEntity<String> response = registryRestTemplate.exchange("http://localhost:5000/v2/", HttpMethod.GET, null, String.class);
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public List<String> fetchAllImages(Optional<String> filterOpt) {
        try {
            Map response = registryRestTemplate.getForObject("http://localhost:5000/v2/_catalog", Map.class);
            List<String> repositories = (List<String>) response.getOrDefault("repositories", List.of());
            return filterOpt.map(filter -> repositories.stream()
                    .filter(name -> name.toLowerCase().contains(filter.toLowerCase()))
                    .collect(Collectors.toList())).orElse(repositories);
        } catch (Exception e) {
            return List.of();
        }
    }

    public ResponseEntity<String> getManifest(String name, String reference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.docker.distribution.manifest.v2+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            return registryRestTemplate.exchange("http://localhost:5000/v2/" + name + "/manifests/" + reference,
                    HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    public ResponseEntity<String> listTags(String name) {
        return registryRestTemplate.exchange("http://localhost:5000/v2/" + name + "/tags/list",
                HttpMethod.GET, null, String.class);
    }

    public ResponseEntity<Void> deleteImage(String name, String reference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", String.join(", ",
                    "application/vnd.oci.image.manifest.v1+json",
                    "application/vnd.docker.distribution.manifest.v2+json",
                    "application/vnd.docker.distribution.manifest.v1+json"
            ));
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> headResp = registryRestTemplate.exchange("http://localhost:5000/v2/" + name + "/manifests/" + reference,
                    HttpMethod.HEAD, entity, Void.class);
            String digest = headResp.getHeaders().getFirst("Docker-Content-Digest");
            if (digest == null) return ResponseEntity.notFound().build();
            return registryRestTemplate.exchange("http://localhost:5000/v2/" + name + "/manifests/" + digest,
                    HttpMethod.DELETE, null, Void.class);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<String> deleteAllTagsForImage(String name) {
        try {
            TagListResponse tagList = registryRestTemplate.getForObject("http://localhost:5000/v2/" + name + "/tags/list", TagListResponse.class);
            if (tagList == null || tagList.getTags() == null || tagList.getTags().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<String> deletedTags = tagList.getTags().stream().map(tag -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Accept", String.join(", ",
                            "application/vnd.oci.image.manifest.v1+json",
                            "application/vnd.docker.distribution.manifest.v2+json",
                            "application/vnd.docker.distribution.manifest.v1+json"
                    ));
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<Void> headResp = registryRestTemplate.exchange("http://localhost:5000/v2/" + name + "/manifests/" + tag,
                            HttpMethod.HEAD, entity, Void.class);
                    String digest = headResp.getHeaders().getFirst("Docker-Content-Digest");
                    if (digest == null) return null;

                    registryRestTemplate.exchange("http://localhost:5000/v2/" + name + "/manifests/" + digest,
                            HttpMethod.DELETE, null, Void.class);
                    log.info("Deleted tag '{}' (digest={})", tag, digest);
                    return tag;
                } catch (Exception e) {
                    return null;
                }
            }).filter(tag -> tag != null).collect(Collectors.toList());

            if (deletedTags.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok("Deleted tags: " + String.join(", ", deletedTags));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting tags: " + e.getMessage());
        }
    }
}
