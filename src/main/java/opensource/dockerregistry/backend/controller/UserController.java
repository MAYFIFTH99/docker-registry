package opensource.dockerregistry.backend.controller;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.dto.UserDto;
import opensource.dockerregistry.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<String>> getUsers() throws IOException {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody UserDto userDto) throws IOException {
        userService.createUser(userDto.getUsername(), userDto.getPassword());
        return ResponseEntity.ok("User added");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) throws IOException {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted");
    }
}
