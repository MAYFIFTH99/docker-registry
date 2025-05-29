package opensource.dockerregistry.backend.controller;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/all")
    public Mono<ResponseEntity<Object>> getAllImages() {
        return imageService.fetchAllImages()
                .map(images -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(images));
    }

    @GetMapping("/")
    public Mono<ResponseEntity<Void>> checkV2() {
        return imageService.checkV2Support();
    }

    @GetMapping("/{name}/manifests/{reference}")
    public Mono<ResponseEntity<String>> getManifest(@PathVariable String name, @PathVariable String reference) {
        return imageService.getManifest(name, reference);
    }

    @GetMapping("/{name}/tags/list")
    public Mono<ResponseEntity<String>> listTags(@PathVariable String name) {
        return imageService.listTags(name);
    }

    @DeleteMapping("/{name}")
    public Mono<ResponseEntity<String>> deleteImageAndAllReferences(@PathVariable String name)
    {
        return imageService.deleteAllTagsForImage(name);
    }

    @DeleteMapping("/{name}/manifests/{reference}")
    public Mono<ResponseEntity<Void>> deleteImage(@PathVariable String name, @PathVariable String reference) {
        return imageService.deleteImage(name, reference);
    }
}
