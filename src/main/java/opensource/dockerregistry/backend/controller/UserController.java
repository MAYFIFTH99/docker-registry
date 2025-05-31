package opensource.dockerregistry.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestParam String username, @RequestParam String password) {
        userService.addUser(username, password);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> getUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}

