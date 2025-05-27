package opensource.dockerregistry.backend.controller;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.service.RegistryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final RegistryService registryService;

    /**
     * Registry API v2 살아있는지 확인
     */
    @GetMapping("/ping")
    public Mono<ResponseEntity<String>> pingRegistry() {
        return registryService.isRegistryAlive()
                .map(alive -> alive ? ResponseEntity.ok("Registry is alive")
                        : ResponseEntity.status(503).body("Registry unavailable"));
    }

    /**
     * 이미지의 manifest JSON 반환
     */
    @GetMapping("/{image}/manifest/{tag}")
    public Mono<ResponseEntity<String>> getManifest(
            @PathVariable String image,
            @PathVariable String tag
    ) {
        return registryService.fetchManifest(image, tag)
                .map(manifest -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(manifest));
    }

    /**
     * 이미지 blob 파일 다운로드
     */
    @GetMapping("/{image}/blob/{digest}")
    public Mono<ResponseEntity<byte[]>> getBlob(
            @PathVariable String image,
            @PathVariable String digest
    ) {
        return registryService.fetchBlob(image, digest)
                .map(blob -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(blob));
    }
}
