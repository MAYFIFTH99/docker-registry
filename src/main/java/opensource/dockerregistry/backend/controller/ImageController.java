package opensource.dockerregistry.backend.controller;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/")
    public ResponseEntity<Void> checkV2() {
        return imageService.checkV2Support();
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllImages(@RequestParam(name = "filter", required = false) String filter) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(imageService.fetchAllImages(Optional.ofNullable(filter)));
    }

    @GetMapping("/{name}/manifests/{reference}")
    public ResponseEntity<String> getManifest(@PathVariable String name, @PathVariable String reference) {
        return imageService.getManifest(name, reference);
    }

    @GetMapping("/{name}/tags/list")
    public ResponseEntity<String> listTags(@PathVariable String name) {
        return imageService.listTags(name);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteImageAndAllReferences(@PathVariable String name) {
        return imageService.deleteAllTagsForImage(name);
    }

    @DeleteMapping("/{name}/manifests/{reference}")
    public ResponseEntity<Void> deleteImage(@PathVariable String name, @PathVariable String reference) {
        return imageService.deleteImage(name, reference);
    }
}
