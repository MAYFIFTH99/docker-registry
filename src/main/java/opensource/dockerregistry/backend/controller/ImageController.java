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

    @GetMapping
    public ResponseEntity<Object> getAllImages(@RequestParam(name = "filter", required = false) String filter) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(imageService.fetchAllImages(Optional.ofNullable(filter)));
    }

    @GetMapping("/{name}/tags")
    public ResponseEntity<String> listTags(@PathVariable String name) {
        return imageService.listTags(name);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteImageAndAllTags(@PathVariable String name) {
        return imageService.deleteAllTagsForImage(name);
    }

    @DeleteMapping("/{name}/tags/{tag}")
    public ResponseEntity<Void> deleteSpecificTag(@PathVariable String name, @PathVariable String tag) {
        return imageService.deleteTag(name, tag);
    }
}
