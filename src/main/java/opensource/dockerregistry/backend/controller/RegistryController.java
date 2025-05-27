package opensource.dockerregistry.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/")
public class RegistryController {

    @GetMapping
    public ResponseEntity<Void> checkApiVersion() {
        return ResponseEntity.ok().build();
    }

}
