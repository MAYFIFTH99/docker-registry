package opensource.dockerregistry.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opensource.dockerregistry.backend.dto.TagListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final WebClient registryWebClient;

    public Mono<ResponseEntity<Void>> checkV2Support() {
        return registryWebClient.get()
                .uri("/v2/")
                .exchangeToMono(response -> Mono.just(ResponseEntity.status(response.statusCode()).build()));
    }

    public Mono<List<String>> fetchAllImages(Optional<String> filterOpt) {
        return registryWebClient.get()
                .uri("/v2/_catalog")
                .retrieve()
                .bodyToMono(Map.class)
                .map(data -> {
                    List<String> repositories = (List<String>) data.getOrDefault("repositories", List.of());

                    if (filterOpt.isPresent()) {
                        String filter = filterOpt.get().toLowerCase();
                        return repositories.stream()
                                .filter(name -> name.toLowerCase().contains(filter))
                                .toList();
                    }

                    return repositories;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(List.of());  // 실패 시 빈 리스트 반환
                });
    }


    public Mono<ResponseEntity<String>> getManifest(String name, String reference) {
        return registryWebClient.get()
                .uri("/v2/" + name + "/manifests/" + reference)
                .header("Accept", "application/vnd.docker.distribution.manifest.v2+json")
                .retrieve()
                .toEntity(String.class)
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
                });
    }

    public Mono<ResponseEntity<String>> listTags(String name) {
        return registryWebClient.get()
                .uri("/v2/" + name + "/tags/list")
                .retrieve()
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<Void>> deleteImage(String name, String reference) {
        return registryWebClient.head()
                .uri("/v2/" + name + "/manifests/" + reference)
                .header("Accept", String.join(", ",
                        "application/vnd.oci.image.manifest.v1+json",
                        "application/vnd.docker.distribution.manifest.v2+json",
                        "application/vnd.docker.distribution.manifest.v1+json"
                ))
                .exchangeToMono(headResp -> {
                    String digest = headResp.headers().asHttpHeaders().getFirst("Docker-Content-Digest");
                    if (digest == null) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return registryWebClient.delete()
                            .uri("/v2/" + name + "/manifests/" + digest)
                            .exchangeToMono(delResp -> Mono.just(ResponseEntity.status(delResp.statusCode()).build()));
                });
    }

    public Mono<ResponseEntity<String>> deleteAllTagsForImage(String name) {
        return registryWebClient.get()
                .uri("/v2/" + name + "/tags/list")
                .retrieve()
                .bodyToMono(TagListResponse.class)
                .flatMapMany(tagList -> {
                    if (tagList.getTags() == null || tagList.getTags().isEmpty()) {
                        return Flux.empty();
                    }

                    return Flux.fromIterable(tagList.getTags());
                })
                .flatMap(tag -> {
                    return registryWebClient.head()
                            .uri("/v2/" + name + "/manifests/" + tag)
                            .header("Accept", String.join(", ",
                                    "application/vnd.oci.image.manifest.v1+json",
                                    "application/vnd.docker.distribution.manifest.v2+json",
                                    "application/vnd.docker.distribution.manifest.v1+json"
                            ))
                            .exchangeToMono(headResp -> {
                                String digest = headResp.headers().asHttpHeaders().getFirst("Docker-Content-Digest");
                                if (digest == null) {
                                    return Mono.empty();  // digest 없으면 skip
                                }

                                return registryWebClient.delete()
                                        .uri("/v2/" + name + "/manifests/" + digest)
                                        .exchangeToMono(delResp -> {
                                            log.info("Deleted tag '{}' (digest={}) → status: {}", tag, digest, delResp.statusCode());
                                            return Mono.just(tag);
                                        });
                            });
                })
                .collectList()
                .map(deletedTags -> {
                    if (deletedTags.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok("Deleted tags: " + String.join(", ", deletedTags));
                });
    }
}
