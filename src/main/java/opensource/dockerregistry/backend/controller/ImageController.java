package opensource.dockerregistry.backend.controller;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/ping")
    public Mono<ResponseEntity<String>> pingRegistry() {
        return imageService.ping().map(alive -> alive ? ResponseEntity.ok("Registry is alive")
                : ResponseEntity.status(503).body("Registry unavailable"));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<Object>> getAllImages() {
        return imageService.fetchAllImages()
                .map(images -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(images));
    }


}
