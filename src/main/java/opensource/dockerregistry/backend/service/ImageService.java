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
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    public static final String REGISTRY_URL = "http://localhost:5000/v2/";
    public static final String APPLICATION_VND_OCI_IMAGE_MANIFEST_V_1_JSON = "application/vnd.oci.image.manifest.v1+json";
    public static final String APPLICATION_VND_DOCKER_DISTRIBUTION_MANIFEST_V_2_JSON = "application/vnd.docker.distribution.manifest.v2+json";
    public static final String APPLICATION_VND_DOCKER_DISTRIBUTION_MANIFEST_V_1_JSON = "application/vnd.docker.distribution.manifest.v1+json";
    public static final String DOCKER_CONTENT_DIGEST = "Docker-Content-Digest";
    private final RestTemplate registryRestTemplate;

    public List<String> fetchAllImages(Optional<String> filterOpt) {
        try {
            var response = registryRestTemplate.getForObject(REGISTRY_URL + "_catalog", Map.class);
            var repositories = (List<String>) response.getOrDefault("repositories", List.of());
            return filterOpt.map(filter -> repositories.stream()
                    .filter(name -> name.toLowerCase().contains(filter.toLowerCase()))
                    .collect(Collectors.toList())).orElse(repositories);
        } catch (Exception e) {
            return List.of();
        }
    }


    public ResponseEntity<String> listTags(String name) {
        return registryRestTemplate.exchange(REGISTRY_URL + name + "/tags/list",
                HttpMethod.GET, null, String.class);
    }

    public ResponseEntity<Void> deleteTag(String name, String tag) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", String.join(", ",
                    APPLICATION_VND_OCI_IMAGE_MANIFEST_V_1_JSON,
                    APPLICATION_VND_DOCKER_DISTRIBUTION_MANIFEST_V_2_JSON,
                    APPLICATION_VND_DOCKER_DISTRIBUTION_MANIFEST_V_1_JSON
            ));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> headResp = registryRestTemplate.exchange(REGISTRY_URL + name + "/manifests/" + tag,
                    HttpMethod.HEAD, entity, Void.class);

            String digest = headResp.getHeaders().getFirst(DOCKER_CONTENT_DIGEST);

            if (digest == null) return ResponseEntity.notFound().build();
            return registryRestTemplate.exchange(REGISTRY_URL + name + "/manifests/" + digest,
                    HttpMethod.DELETE, null, Void.class);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<String> deleteAllTagsForImage(String name) {
        try {
            TagListResponse tagList = registryRestTemplate.getForObject(REGISTRY_URL + name + "/tags/list", TagListResponse.class);
            if (tagList == null || tagList.getTags() == null || tagList.getTags().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<String> deletedTags = tagList.getTags().stream().map(tag -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Accept", String.join(", ",
                            APPLICATION_VND_OCI_IMAGE_MANIFEST_V_1_JSON,
                            APPLICATION_VND_DOCKER_DISTRIBUTION_MANIFEST_V_2_JSON,
                            APPLICATION_VND_DOCKER_DISTRIBUTION_MANIFEST_V_1_JSON
                    ));

                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<Void> headResp = registryRestTemplate.exchange(REGISTRY_URL + name + "/manifests/" + tag,
                            HttpMethod.HEAD, entity, Void.class);
                    String digest = headResp.getHeaders().getFirst(DOCKER_CONTENT_DIGEST);
                    if (digest == null) return null;

                    registryRestTemplate.exchange(REGISTRY_URL + name + "/manifests/" + digest,
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
