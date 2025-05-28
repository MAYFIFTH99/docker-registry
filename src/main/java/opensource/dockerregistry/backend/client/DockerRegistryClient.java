package opensource.dockerregistry.backend.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DockerRegistryClient {

    private final WebClient registryWebClient;

    /**
     * Registry API v2 지원 여부 확인
     */
    public Mono<Boolean> ping() {
        return registryWebClient.get()
                .uri("/v2/")
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode() == HttpStatus.OK)
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(false));
    }

    /**
     * 특정 이미지의 manifest 조회
     */
    public Mono<String> getImageManifest(String imageName, String tag) {
        return registryWebClient.get()
                .uri("/v2/{image}/manifests/{tag}", imageName, tag)
                .header("Accept", "application/vnd.oci.image.manifest.v1+json,application/vnd.docker.distribution.manifest.v2+json")
                .retrieve()
                .bodyToMono(String.class);
    }


    /**
     * 특정 blob 다운로드
     */
    public Mono<byte[]> downloadBlob(String imageName, String digest) {
        return registryWebClient.get()
                .uri("/v2/{image}/blobs/{digest}", imageName, digest)
                .retrieve()
                .bodyToMono(byte[].class);
    }
}