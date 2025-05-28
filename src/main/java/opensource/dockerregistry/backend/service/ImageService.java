package opensource.dockerregistry.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ImageService {

    private final WebClient registryWebClient;

    /**
     * Registry API v2 health check
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
     * 모든 이미지 목록 조회
     * // TODO : 필터링 추가
     */
    public Mono<Object> fetchAllImages() {
        return registryWebClient.get()
                .uri("/v2/_catalog")
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.just("Error fetching images: " + ex.getMessage()));
    }

    /**
     * PUSH
     */

    /**
     * PULL
     */

    /**
     * 이미지 삭제
     */

    /**
     * 특정 이미지 태그 조회
     */

    /**
     * 특정 이미지 태그 삭제
     */

    /**
     * 사용자별 활동 기록 조회
     */

    /**
     * 이미지별 활동 기록 조회
     */
}